package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private void validateEmailIsUnique(String email, Long userIdToIgnore) {
        if (userRepository.existsByEmail(email)) {
            User existingUser = findUserByEmail(email);
            if (userIdToIgnore == null || !existingUser.getId().equals(userIdToIgnore)) {
                throw new ConflictException("Email уже используется: " + email);
            }
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ConflictException("Email уже используется: " + email));
    }

    private User getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с айди " + id + " не найден"));
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateEmailIsUnique(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
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

        User updatedUser = userRepository.save(existing);
        return UserMapper.toUserDto(updatedUser);
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
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        getUserByIdOrThrow(id);
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}