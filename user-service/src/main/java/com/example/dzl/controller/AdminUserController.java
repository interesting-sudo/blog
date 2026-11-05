package com.example.dzl.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.exception.BusinessException;
import com.example.dzl.enums.UserErrorEnum;
import com.example.dzl.model.request.UserPageReq;
import com.example.dzl.model.vo.UserAdminVO;
import com.example.dzl.service.UserService;
import com.example.dzl.utils.JwtUtil;
import com.example.dzl.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端：用户管理
 * 仅限管理员角色访问，通过网关进行权限拦截
 */
@RestController
@RequestMapping("/admin/user")
@Slf4j
public class AdminUserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户列表（分页，支持按用户名/昵称搜索）
     */
    @PostMapping("/list")
    public Result<Page<UserAdminVO>> list(@RequestBody UserPageReq req) {
        log.info("管理员操作：获取用户列表, req: {}", req);
        return Result.success(userService.pageUsers(req));
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/status/{userId}")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestParam Integer status, HttpServletRequest request) {
        log.info("管理员操作：更新用户 {} 状态为 {}", userId, status);

        // 校验：管理员不能修改自己的状态
        checkSelfOperation(userId, request);
        userService.updateStatus(userId, status);
        return Result.success();
    }

    /**
     * 重置用户密码（管理员强制重置）
     */
    @PutMapping("/password/reset/{userId}")
    public Result<Void> adminResetPassword(@PathVariable Long userId,HttpServletRequest request) {
        log.info("管理员操作：重置用户 {} 密码", userId);

        // 校验：管理员不能修改自己的状态
        checkSelfOperation(userId, request);
         userService.adminResetPassword(userId);
        return Result.success();
    }

    /**
     * 校验是否为自我操作
     */
    private void checkSelfOperation(Long targetUserId, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long currentUserId;
            try {
                currentUserId = jwtUtil.getUserId(jwtUtil.verifyAccessToken(token));
            } catch (Exception e) {
                log.error("解析 Token 失败: {}", e.getMessage());
                return; // 如果解析失败，可能是网关已经校验过或者其他原因，暂时放行或按需处理
            }

            if (targetUserId.equals(currentUserId)) {
                log.warn("检测到管理员尝试操作自身账号，userId: {}", targetUserId);
                throw new BusinessException(UserErrorEnum.ADMIN_CANNOT_MODIFY_SELF);
            }
        }
    }
}

