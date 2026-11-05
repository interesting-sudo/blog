package com.example.dzl;

import com.example.dzl.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserApplicationTest {
    @Autowired
    private JwtUtil jwtUtil;

//    @Test
//    public void testJwt() {
//        String accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwianRpIjoiODczMDM1MmYtMGJkZC00NzA1LTg0OTMtYTFmZTdkODk1MzEzIiwicm9sZUlkIjoyLCJpYXQiOjE3Njc0MzQwNTIsImV4cCI6MTc2NzQ0MTI1Mn0.sUH3Vts4CLwQqSndSho1neXYlLYYSZdPXNJ_A-iqOqk";
//
//        Long userId = jwtUtil.getUserId(jwtUtil.verifyAccessToken(accessToken));
//        System.out.println(userId);
//        System.out.println(jwtUtil.getRoleId(jwtUtil.verifyAccessToken(accessToken)));
//    }

}
