package com.myapps.ws.mobilespringappws.service;

import java.util.List;

import com.myapps.ws.mobilespringappws.shared.dto.UserDTO;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserDTO createUser(UserDTO user);

    UserDTO getUser(String email);

    UserDTO getUserByUserId(String userId);

    UserDTO updateUser(String userId, UserDTO user);

    void deleteUser(String userId);

    List<UserDTO> getUsers(int page, int limit);

    boolean verifyEmailToken(String token);
}
