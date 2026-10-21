package test.impl;

import glz.hawk.bean.mapper.annotation.MappingTarget;
import test.OtherMapper;
import test.UserDTO;
import test.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import javax.annotation.processing.Generated;

@Generated("glz.hawk.bean.mapper.processor.BeanMapperAnnotationProcessor")
@Component
public class UserMapperImpl implements test.UserMapper {

    @Autowired
    @Qualifier("other1")
    private test.OtherMapper otherMapper;

    @Override
    public UserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The parameter('entity') in the method(test.UserMapper/toDTO) can't be null.");
        }
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}