package test.impl;

import test.UserDTO;
import test.UserEntity;
import test.UserMapper;

public class UserMapperImpl implements UserMapper {
    @Override
    public UserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The parameter('entity') in the method(UserMapper/toDTO) can't be null.");
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setName(entity.getName());
        userDTO.setId(entity.getId());
        return userDTO;
    }
}