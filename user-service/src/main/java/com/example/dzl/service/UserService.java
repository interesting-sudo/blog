package com.example.dzl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dzl.model.dto.UserInfoDTO;
import com.example.dzl.model.dto.UserRoleDTO;
import com.example.dzl.model.entity.User;
import com.example.dzl.model.request.*;
import com.example.dzl.model.vo.UserAdminVO;
import org.springframework.web.multipart.MultipartFile;


public interface UserService extends IService<User> {

    // 登录
    UserRoleDTO login(LoginUserReq req);

    void register(RegisterUserReq req);

    // 获取用户信息
    UserInfoDTO getUserInfo(Long userId);

    /**
     * 发送验证码
     * @param email 邮箱
     * @param type 类型 1-注册 2-重置密码
     */
    void sendVerificationCode(String email, Integer type);

    /**
     * 校验验证码
     * @param email
     * @param code
     * @param type 类型 1-注册 2-重置密码
     * @return 重置密码令牌（仅重置密码场景返回；其他场景返回null）
     */
    String verifyCode(String email, String code, Integer type);

    /**
     * 重置密码
     * @param req
     */
    void resetPassword(ResetPasswordReq req);

    /**
     * 更新用户信息
     * @param req 用户信息
     * @param avatarFile 头像文件（可选）
     */
    void updateUserInfo(UpdateUserReq req, MultipartFile avatarFile);

    /**
     * 更新用户密码
     * @param req 密码更新请求
     */
    void updatePassword(UpdatePasswordReq req);

    /**
     * 分页查询用户列表（管理端）
     */
    Page<UserAdminVO> pageUsers(UserPageReq req);

    /**
     * 更新用户状态（管理端）
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 重置用户密码（管理端）
     */
    void adminResetPassword(Long userId);

}
