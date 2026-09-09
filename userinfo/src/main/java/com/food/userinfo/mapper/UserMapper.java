package com.food.userinfo.mapper;

import com.food.userinfo.dto.AddressDTO;
import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.entity.Address;
import com.food.userinfo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "address", target = "address")
    User mapUserDTOToUser(UserDTO userDTO);

    @Mapping(source = "address", target = "address")
    UserDTO mapUserToUserDTO(User user);

    // Address mapping
    Address mapAddressDTOToAddress(AddressDTO addressDTO);
    AddressDTO mapAddressToAddressDTO(Address address);
}
