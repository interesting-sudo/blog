package com.example.dzl.controller;

import com.example.dzl.enums.SendCodeTypeEnum;
import com.example.dzl.mapstruct.UserConvert;
import com.example.dzl.model.dto.UserRoleDTO;
import com.example.dzl.model.request.*;
import com.example.dzl.model.vo.AccessTokenVO;
import com.example.dzl.vo.UserInfoVO;
import com.example.dzl.service.RefreshTokenService;
import com.example.dzl.service.UserService;
import com.example.dzl.utils.JwtUtil;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${jwt.access-token.expire}")
    private Long accessExpireSeconds;

    @Autowired
    private UserConvert userConvert;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<AccessTokenVO> login(@RequestBody LoginUserReq req,HttpServletResponse  response) {
       // 用户校验登录信息
       UserRoleDTO userRoleDTO = userService.login(req);
       log.info("用户登录成功：{}", userRoleDTO);
       // 生成 accessToken
       String accessToken = jwtUtil.generateAccessToken(userRoleDTO.getUserId(), userRoleDTO.getRoleId());

       // 生成 refreshToken，并通过HttpOnly Cookie 存储
       String refreshToken = refreshTokenService.generateRefreshToken(userRoleDTO.getUserId(), response);

       return Result.success(new AccessTokenVO(accessToken, accessExpireSeconds,userRoleDTO.getRoleId()));
    }

    /**
     * 发送验证码
     */
    @PostMapping("/register/email-code")
    public Result<Void> sendCode(@RequestParam String email) {
        log.info("进入该接口：{}", email);
        userService.sendVerificationCode(email, SendCodeTypeEnum.REGISTER.getCode());
        return Result.success();
    }

    /**
     * 发送重置密码验证码
     */
    @PostMapping("/password/email-code")
    public Result<Void> sendResetPasswordCode(@RequestParam String email) {
        log.info("发送重置密码验证码：{}", email);
        userService.sendVerificationCode(email, SendCodeTypeEnum.RESET_PASSWORD.getCode());
        return Result.success();
    }

    /**
     * 校验验证码
     */
    @PostMapping("/email-code/check")
    public Result<Void> checkCode(@RequestBody VerifyCodeReq req) {
        userService.verifyCode(req.getEmail(), req.getCode(), SendCodeTypeEnum.REGISTER.getCode());
        return Result.success();
    }

    /**
     * 重置密码（忘记密码）
     */
    @PutMapping("/password/reset")
    public Result<Void> resetPassword(@RequestBody ResetPasswordReq req) {
        userService.resetPassword(req);
        return Result.success();
    }


    @PostMapping("/password/check")
    public Result<String> checkPassword(@RequestBody CheckPasswordReq req) {
        String token = userService.verifyCode(req.getEmail(), req.getCode(), SendCodeTypeEnum.RESET_PASSWORD.getCode());
        return Result.success(token);
    }

    /**
     * 修改密码
     */
    @PutMapping("/update/password")
    public Result<Void> updatePassword(@RequestBody UpdatePasswordReq req) {
        log.info("修改密码请求，userId: {}", req.getId());
        userService.updatePassword(req);
        return Result.success();
    }



    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterUserReq req) {
        userService.register(req);
        return Result.success();
    }


    /**
     * 获取用户信息
     */
    @GetMapping("/info/{userId}")
    public Result<UserInfoVO> getUserInfo(@PathVariable Long userId) {
        return Result.success( userConvert.UserInfoDTOToVO(userService.getUserInfo(userId)));
    }

    /**
     * 修改用户信息
     */
    @PutMapping(value = "/update", consumes = "multipart/form-data")
    public Result<Void> update(@RequestPart("user") UpdateUserReq req,
                               @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        log.info("修改用户信息请求: {}", req);
        userService.updateUserInfo(req, avatar);
        return Result.success();
    }


    /**
     * 刷新令牌
     */
    @PostMapping("/auth/refresh")
    public Result<AccessTokenVO> refresh(HttpServletRequest request, HttpServletResponse response) {
        log.info("刷新令牌请求，request: {}", request);

        Long userId = refreshTokenService.verifyRefreshToken(request);

        // refresh 成功后：续期/轮换（取决于你的配置）
        refreshTokenService.onRefreshSuccess(userId, request, response);

        // 重新签发 accessToken（这里示例只带 roleId）
        int roleId = userService.getById(userId).getRoleId();
        String accessToken = jwtUtil.generateAccessToken(userId, roleId);

        return Result.success(new AccessTokenVO(accessToken, accessExpireSeconds));
    }
    /**
     * 登出
     */
    @PostMapping("/auth/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenService.revokeRefreshToken(request, response);
        return Result.success();
    }
}
