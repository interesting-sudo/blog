package com.example.dzl.feign;


import com.example.dzl.utils.Result;
import com.example.dzl.vo.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/user/info/{userId}")
    Result<UserInfoVO> getUserInfo(@PathVariable("userId") Long userId);
    /**
     * 获取用户信息
     */
    @GetMapping("/user/feign/{userId}")
    UserInfoVO getInfo(@PathVariable("userId") Long userId);
}
