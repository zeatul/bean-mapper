package test.impl;

import glz.hawk.bean.mapper.annotation.MappingTarget;
import test.UserDTO;
import test.UserEntity;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;

@Generated("glz.hawk.bean.mapper.processor.BeanMapperAnnotationProcessor")
public class UserMapperImpl implements test.UserMapper {

    @Override
    public UserDTO convert(UserEntity source) {
        if (source == null) {
            throw new IllegalArgumentException("The parameter('source') in the method(test.UserMapper/convert) can't be null.");
        }
        UserDTO dto = new UserDTO();
        dto.setId(source.getId());
        dto.setName(source.getName());
        return dto;
    }

    @Override
    public void copy(UserEntity source, UserDTO target) {
        if (target == null) {
            throw new IllegalArgumentException("The parameter('target') in the method(test.UserMapper/copy) can't be null.");
        }
        if (source == null) {
            throw new IllegalArgumentException("The parameter('source') in the method(test.UserMapper/copy) can't be null.");
        }
        target.setId(source.getId());
        target.setName(source.getName());
    }
}