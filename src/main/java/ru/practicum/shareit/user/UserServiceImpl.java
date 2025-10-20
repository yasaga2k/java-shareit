package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    private void validateEmailIsUnique(String email, Long userIdToIgnore) {
        for (User u : users.values()) {
            if (u.getEmail().equals(email) && !u.getId().equals(userIdToIgnore)) {
                throw new ConflictException("Email уже используется: " + email);
            }
        }
    }

    private User getUserByIdOrThrow(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с айди " + id + " не найден");
        }
        return user;
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateEmailIsUnique(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        Long id = nextId++;
        user.setId(id);
        users.put(id, user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = getUserByIdOrThrow(id);

        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            validateEmailIsUnique(userDto.getEmail(), id);
            existing.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(existing);
    }

    @Override
    public UserDto getByUserId(Long id) {
        User user = getUserByIdOrThrow(id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public User getUserById(Long id) {
        return getUserByIdOrThrow(id);
    }

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        getUserByIdOrThrow(id);
        users.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}
