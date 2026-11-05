package com.example.dzl.mapstruct;


import com.example.dzl.model.dto.RegisterUserDTO;
import com.example.dzl.model.dto.UserInfoDTO;
import com.example.dzl.model.dto.UserRoleDTO;
import com.example.dzl.model.entity.User;
import com.example.dzl.model.request.RegisterUserReq;
import com.example.dzl.model.vo.UserAdminVO;
import com.example.dzl.vo.UserInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConvert {

    RegisterUserDTO reqToDTO(RegisterUserReq req);

    @Mapping(target = "userId", source = "id")
    UserRoleDTO UserToDTO(User user);

    User DTOtoUser(RegisterUserDTO dto);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    UserInfoDTO UserToInfoDTO(User user);

    UserInfoVO UserInfoDTOToVO(UserInfoDTO dto);
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    UserAdminVO UserToAdminVO(User user);
}
