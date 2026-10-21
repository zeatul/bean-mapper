package test.impl;

import glz.hawk.bean.mapper.annotation.MappingTarget;
import test.UserDTO;
import test.UserEntity;
import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;

@Generated("glz.hawk.bean.mapper.processor.BeanMapperAnnotationProcessor")
public class UserMapperImpl implements test.UserMapper {

    @Override
    public UserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The parameter('entity') in the method(test.UserMapper/toDTO) can't be null.");
        }
        UserDTO dto = new UserDTO();
        dto.setName(entity.getName());
        // id is ignored
        return dto;
    }
}