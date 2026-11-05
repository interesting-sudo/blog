package com.example.dzl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dzl.enums.CommonErrorEnum;
import com.example.dzl.enums.RoleEnum;
import com.example.dzl.enums.SendCodeTypeEnum;
import com.example.dzl.enums.UserErrorEnum;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.feign.DocumentServiceClient;
import com.example.dzl.mapper.RoleMapper;
import com.example.dzl.mapstruct.UserConvert;
import com.example.dzl.model.dto.RegisterUserDTO;
import com.example.dzl.model.dto.UserInfoDTO;
import com.example.dzl.model.dto.UserRoleDTO;
import com.example.dzl.model.entity.Role;
import com.example.dzl.model.entity.User;
import com.example.dzl.mapper.UserMapper;
import com.example.dzl.model.mq.UserProfileChangedEvent;
import com.example.dzl.model.request.*;
import com.example.dzl.model.vo.UserAdminVO;
import com.example.dzl.mq.UserProfileChangedProducer;
import com.example.dzl.service.MailService;
import com.example.dzl.service.UserService;
import com.example.dzl.utils.RedisCache;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private DocumentServiceClient documentServiceClient;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private MailService mailService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserConvert userConvert;

    @Autowired
    private UserProfileChangedProducer userProfileChangedProducer;

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserRoleDTO login(LoginUserReq req) {
        log.info("用户登录：{}", req);
        // 1.判断用户名是否存在
        User user = this.lambdaQuery().eq(User::getUsername, req.getUsername()).one();
        if (user == null) {
            throw new BusinessException(UserErrorEnum.USER_NOT_EXIST);
        }

        // 2.密码校验
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorEnum.USER_PASSWORD_ERROR);
        }
        // 3.判断用户状态
        if (user.getStatus() == 0) {
            // 账号已被禁用
            throw new BusinessException(UserErrorEnum.USER_DISABLED);
        }
        log.info("用户登录成功：{}", user);
        // 4.返回用户信息
        return userConvert.UserToDTO(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterUserReq req) {
        RegisterUserDTO registerUserDTO = userConvert.reqToDTO(req);
        log.info("用户注册：{}", registerUserDTO);

        // 1.用户昵称是否已经存在
        boolean exists1 = this.lambdaQuery().eq(User::getNickname, registerUserDTO.getNickname()).exists();
        if (exists1) {
            throw new BusinessException(UserErrorEnum.USER_NICKNAME_REPEAT);
        }
        // 2. 密码加密（建议校验非空）
        if (registerUserDTO.getPassword() == null || registerUserDTO.getPassword().isBlank()) {
            throw new BusinessException(CommonErrorEnum.PARAM_ERROR);
        }
        // 密码加密
        registerUserDTO.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));

        // 给新用户分配 USER 角色
        Role userRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getName, RoleEnum.USER)
        );
        if (userRole == null) {
            throw new BusinessException(UserErrorEnum.ROLE_NOT_EXIST);
        }
        log.info("角色ID:{}", userRole.getId());
        registerUserDTO.setRoleId(userRole.getId());
        log.info("DTO:{}", registerUserDTO);

        // 注册用户信息，（数据库唯一索引兜底 + 捕获重复）
        try {
            userMapper.insert(userConvert.DTOtoUser(registerUserDTO));
            log.info("用户注册成功,{}", userConvert.DTOtoUser(registerUserDTO));
        } catch (DuplicateKeyException e) {
            // 并发情况下仍可能触发唯一索引冲突
            throw new BusinessException(UserErrorEnum.USER_ALREADY_EXIST);
        }

    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = this.getById(userId);

        log.info("获取的用户信息：{}", userConvert.UserToInfoDTO(user));
        return userConvert.UserToInfoDTO(user);
    }

    @Override
    public void sendVerificationCode(String email, Integer type) {
        // 1. 检查邮箱是否已被注册
        boolean exists = this.lambdaQuery().eq(User::getUsername, email).exists();

        if (SendCodeTypeEnum.REGISTER.getCode().equals(type)) {
            // 检查邮箱是否已被注册
            if (exists) {
                throw new BusinessException(UserErrorEnum.EMAIL_ALREADY_REGISTERED);
            }
        } else if (SendCodeTypeEnum.RESET_PASSWORD.getCode().equals(type)) {
            // 重置密码场景，邮箱必须已注册
            if (!exists) {
                throw new BusinessException(UserErrorEnum.USER_NOT_EXIST);
            }
        }

        // 生成6位随机验证码
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        // 存入redis
        redisCache.setCacheString("verify_code:" + email, code, 60);

        // 构建HTML内容
        String subject = "【IBlog】验证码";
        String htmlContent = "";

        if (SendCodeTypeEnum.REGISTER.getCode().equals(type)) {
            subject = "【IBlog 注册验证码】请查收您的验证码";
            htmlContent = String.format(
                    "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9;\">" +
                            "<h2 style=\"color: #333;\">欢迎注册！</h2>" +
                            "<p style=\"font-size: 16px; color: #555;\">您的验证码是：</p>" +
                            "<h1 style=\"color: #007BFF; font-size: 36px; margin: 10px 0;\">%s</h1>" +
                            "<p style=\"font-size: 14px; color: #888;\">有效期1分钟，请勿泄露给他人。</p>" +
                            "<hr style=\"border: 0; border-top: 1px solid #ccc; margin: 20px 0;\">" +
                            "<div style=\"text-align: center;\">" +
                            "<p style=\"font-size: 16px; color: #333; font-weight: bold;\">👇 既然你诚心诚意地注册了，就给你看个宝贝 👇</p>" +
                            "<img src=\"https://blog-documennt.oss-cn-hangzhou.aliyuncs.com/images/2026/02/8bd97bbcbaaa4b13a3407b2518f60e94.jpg\" style=\"width: 300px; max-width: 100%%; border-radius: 8px; margin-bottom: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);\">" +
                            "<p style=\"font-style: italic; color: #666; font-size: 14px;\">“摊牌了，不装了！其实写代码只是我的副业，这幅字才是本体。请务必细细品鉴这股‘狂草’中蕴含的... 逻辑闭环！这波啊，这波是跨界降维打击！(战术后仰)”</p>" +
                            "</div>" +
                            "</div>",
                    code
            );
        } else {
            subject = "【IBlog 重置密码验证码】请查收您的验证码";
            htmlContent = String.format(
                    "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 10px; background-color: #f9f9f9;\">" +
                            "<h2 style=\"color: #333;\">重置密码</h2>" +
                            "<p style=\"font-size: 16px; color: #555;\">您正在进行密码重置操作，验证码是：</p>" +
                            "<h1 style=\"color: #007BFF; font-size: 36px; margin: 10px 0;\">%s</h1>" +
                            "<p style=\"font-size: 14px; color: #888;\">有效期1分钟，请勿泄露给他人。</p>" +
                            "</div>",
                    code
            );
        }

        // 发送邮件
        mailService.sendHtmlMail(email, subject, htmlContent);
    }

    @Override
    public String verifyCode(String email, String code, Integer type) {
        String cacheCode = redisCache.getCacheString("verify_code:" + email);
        if (cacheCode == null || !cacheCode.equals(code)) {
            throw new BusinessException(UserErrorEnum.VERIFICATION_CODE_ERROR);
        }
        // 验证通过后，可以删除验证码，或者留给注册接口再次校验（如果需要）
        redisCache.deleteString("verify_code:" + email);
        if (SendCodeTypeEnum.RESET_PASSWORD.getCode().equals(type)) {
            // 生成一个临时的重置密码token，有效期5分钟
            String resetToken = UUID.randomUUID().toString().replace("-", "");
            redisCache.setCacheString("reset_token:" + email, resetToken, 300);
            return resetToken;
        }
        return null;
    }

    @Override
    public void resetPassword(ResetPasswordReq req) {
        log.info("Reset password request: {}", req);
        // 检查验证码
        String cacheToken = redisCache.getCacheString("reset_token:" + req.getUsername());
        if (cacheToken == null || !cacheToken.equals(req.getResetToken())) {
            throw new BusinessException(UserErrorEnum.VERIFICATION_CODE_ERROR);
        }
        redisCache.deleteString("reset_token:" + req.getUsername());

        // 检查用户是否存在
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException(UserErrorEnum.USER_NOT_EXIST);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        this.updateById(user);
    }

    @Override
    public void updatePassword(UpdatePasswordReq req) {
        // 1. 校验参数
        log.info("Update password request: {}", req);
        if (req.getId() == null) {
            throw new BusinessException(CommonErrorEnum.PARAM_ERROR);
        }
        if (req.getOldPassword() == null || req.getOldPassword().isEmpty()) {
            throw new BusinessException(CommonErrorEnum.PARAM_ERROR);
        }
        if (req.getNewPassword() == null || req.getNewPassword().isEmpty()) {
            throw new BusinessException(CommonErrorEnum.PARAM_ERROR);
        }

        // 2. 查询用户
        User user = this.getById(req.getId());
        if (user == null) {
            throw new BusinessException(UserErrorEnum.USER_NOT_EXIST);
        }

        // 3. 校验旧密码
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException(UserErrorEnum.USER_OLD_PASSWORD_ERROR);
        }

        // 4. 更新密码
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        this.updateById(user);
        log.info("用户修改密码成功，userId: {}", req.getId());
    }

    /**
     * 更新用户信息
     *
     * @param req        用户信息
     * @param avatarFile 头像文件（可选）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UpdateUserReq req, MultipartFile avatarFile) {
        log.info("Update user info request: {}", req);
        User user = this.getById(req.getId());
        if (user == null) {
            throw new BusinessException(UserErrorEnum.USER_NOT_EXIST);
        }

        // 处理头像上传
        boolean profileChanged = false;
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                Result<String> uploadResult = documentServiceClient.uploadImage(avatarFile);
                if (uploadResult != null && uploadResult.getCode() == 200 && uploadResult.getData() != null) {
                    String newAvatarUrl = uploadResult.getData();
                    if (!newAvatarUrl.equals(user.getAvatarUrl())) {
                        user.setAvatarUrl(newAvatarUrl);
                        profileChanged = true;
                    }
                    log.info("头像上传成功: URL={}", user.getAvatarUrl());
                } else {
                    log.warn("头像上传失败: {}", uploadResult != null ? uploadResult.getMsg() : "未知错误");
                    throw new BusinessException(CommonErrorEnum.INTERNAL_ERROR);
                }
            } catch (Exception e) {
                log.error("上传头像时发生异常", e);
                throw new BusinessException(CommonErrorEnum.INTERNAL_ERROR);
            }
        }
        else if (req.getAvatarUrl() != null && !req.getAvatarUrl().isEmpty()) {
            String newAvatarUrl = req.getAvatarUrl();
            if (!newAvatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(newAvatarUrl);
                profileChanged = true;
            }

        }
        // 更新其他字段
        if (req.getNickname() != null && !req.getNickname().isEmpty()) {
            // 检查昵称是否重复 (如果昵称改变了)
            String newNickname = req.getNickname();
            if (!newNickname.equals(user.getNickname())) {
                user.setNickname(newNickname);
                profileChanged = true;
            }
        }

        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }

        if (req.getMotto() != null) {
            user.setMotto(req.getMotto());
        }

        this.updateById(user);
        log.info("用户信息更新成功: {}", user);

        // 发送用户资料变更事件
        if (profileChanged) {
            UserProfileChangedEvent event = new UserProfileChangedEvent(user.getId(), user.getNickname(), user.getAvatarUrl());
            userProfileChangedProducer.send(event);
        }

    }

    @Override
    public Page<UserAdminVO> pageUsers(UserPageReq req) {
        Page<User> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(req.getUsername())) {
            wrapper.like(User::getUsername, req.getUsername());
        }
        if (StringUtils.hasText(req.getNickname())) {
            wrapper.like(User::getNickname, req.getNickname());
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageResult = this.page(pageParam, wrapper);

        Page<UserAdminVO> resultPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<UserAdminVO> dtoList = pageResult.getRecords().stream()
                .map(userConvert::UserToAdminVO)
                .collect(Collectors.toList());
        resultPage.setRecords(dtoList);
        return resultPage;
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorEnum.USER_NOT_EXIST);
        }
        user.setStatus(status);
        this.updateById(user);
        log.info("管理员更新用户状态成功，userId: {}, status: {}", userId, status);
    }

    @Override
    public void adminResetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorEnum.USER_NOT_EXIST);
        }
        // 重置为默认密码 123456
        user.setPassword(passwordEncoder.encode("123456"));
        this.updateById(user);
        log.info("管理员重置用户密码成功，userId: {}", userId);
    }
}
