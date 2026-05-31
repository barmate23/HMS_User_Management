package com.hotelerp.userservice.service;

import com.hotelerp.userservice.common.StandardResponse;
import com.hotelerp.userservice.dto.UserRequest;

public interface UserService {

    StandardResponse<?> createUser(UserRequest request);

    StandardResponse<?> updateUser(Long id, UserRequest request);

    StandardResponse<?> getUserById(Long id);

    StandardResponse<?> getAllUsers(String searchText, String department, String role, int page, int size);

    StandardResponse<?> deleteUser(Long id);

    StandardResponse<?> changeStatus(Long id, String status);
}
