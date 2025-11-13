import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createShouldSaveUserWhenEmailIsUnique() {
        UserDto userDto = new UserDto(null, "Tester", "tester@example.com");
        User savedUser = new User(1L, "Tester", "tester@example.com");

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.create(userDto);

        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getName(), result.getName());
        assertEquals(savedUser.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createShouldThrowConflictExceptionWhenEmailAlreadyExists() {
        UserDto userDto = new UserDto(null, "Tester", "tester@example.com");

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(userDto));

        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateShouldModifyUserWhenFound() {
        Long userId = 1L;
        User existingUser = new User(userId, "Old Name", "old@example.com");
        UserDto updateDto = new UserDto(null, "New Name", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.update(userId, updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("old@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateShouldThrowNotFoundExceptionWhenNotFound() {
        Long userId = 1L;
        UserDto updateDto = new UserDto(null, "New Name", null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(userId, updateDto));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getByUserIdShouldReturnUserWhenFound() {
        Long userId = 1L;
        User user = new User(userId, "Tester", "tester@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.getByUserId(userId);

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getAllShouldReturnAllUsers() {
        User user1 = new User(1L, "Tester1", "tester1@example.com");
        User user2 = new User(2L, "Tester2", "tester2@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteShouldRemoveUserWhenFound() {
        Long userId = 1L;
        User user = new User(userId, "Tester", "tester@example.com");

        // Мокируем: findById() возвращает существующего пользователя
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Мокируем: удаление проходит без исключений
        doNothing().when(userRepository).deleteById(userId);

        // Выполняем удаление
        userService.delete(userId);

        // Проверяем, что:
        // - вызвали findById() один раз
        verify(userRepository, times(1)).findById(userId);
        // - вызвали deleteById() один раз
        verify(userRepository, times(1)).deleteById(userId);
    }


    @Test
    void deleteShouldThrowNotFoundExceptionWhenNotFound() {
        Long userId = 1L;

        // Мокируем: findById() не находит пользователя (возвращает Optional.empty())
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Проверяем, что при удалении бросается NotFoundException
        assertThrows(NotFoundException.class, () -> userService.delete(userId));

        // Проверяем, что:
        // - попытались найти пользователя (findById() вызван один раз)
        verify(userRepository, times(1)).findById(userId);
        // - удаление НЕ произошло (deleteById() не вызывался)
        verify(userRepository, never()).deleteById(userId);
    }


    @Test
    void existsByIdShouldReturnTrueWhenExists() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        boolean result = userService.existsById(userId);

        assertTrue(result);

        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    void existsByIdShouldReturnFalseWhenNotExists() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        boolean result = userService.existsById(userId);

        assertFalse(result);

        verify(userRepository, times(1)).existsById(userId);
    }
}