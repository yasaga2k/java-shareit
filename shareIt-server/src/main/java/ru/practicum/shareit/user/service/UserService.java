package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    UserDto create(UserDto userDto);

    UserDto update(Long id, UserDto userDto);

    UserDto getByUserId(Long id);

    User getUserById(Long id);

    List<UserDto> getAll();

    void delete(Long id);

    boolean existsById(Long id);
}
