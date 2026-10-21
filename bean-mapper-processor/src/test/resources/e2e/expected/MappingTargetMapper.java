package test.impl;

import test.UserDTO;
import test.UserEntity;
import test.UserMapper;

public class UserMapperImpl implements UserMapper {
    @Override
    public void copy(UserEntity source, UserDTO target) {
        if (target == null) {
            throw new IllegalArgumentException("The parameter('target') in the method(UserMapper/copy) can't be null.");
        }
        if (source == null) {
            throw new IllegalArgumentException("The parameter('source') in the method(UserMapper/copy) can't be null.");
        }
        target.setName(source.getName());
        target.setId(source.getId());
    }
}