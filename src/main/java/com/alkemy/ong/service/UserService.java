package com.alkemy.ong.service;

import com.alkemy.ong.domain.User;
import com.alkemy.ong.dto.UserDTO;
import com.alkemy.ong.mapper.RoleMapper;
import com.alkemy.ong.mapper.UserMapper;
import com.alkemy.ong.repository.RoleRepository;
import com.alkemy.ong.repository.UserRepository;
import com.alkemy.ong.repository.model.RoleModel;
import com.alkemy.ong.repository.model.UserModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO registerUser(User user) {
        RoleModel roleModel = roleRepository.findByName("USER");
        user.setRole(RoleMapper.mapModelToDomain(roleModel));
        UserModel userModel = UserMapper.mapDomainToModel(user);
        userModel.setPassword(encryptPassword(user));
        UserModel save = userRepository.save(userModel);
        return UserMapper.mapModelToDto(save);
    }

    private String encryptPassword(User user){
        String password = user.getPassword();
        String encryptedPassword = passwordEncoder.encode(password);
        return encryptedPassword;
    }
}