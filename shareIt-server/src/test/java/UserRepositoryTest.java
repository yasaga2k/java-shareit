import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.ShareItServerApp;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ContextConfiguration(classes = ShareItServerApp.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveShouldPersistUser() {
        User user = new User(null, "Test User", "test@example.com");

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals(user.getName(), saved.getName());
        assertEquals(user.getEmail(), saved.getEmail());
    }

    @Test
    void findAllShouldReturnAllUsers() {
        User user1 = new User(null, "User 1", "user1@example.com");
        User user2 = new User(null, "User 2", "user2@example.com");

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user1@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user2@example.com")));
    }

    @Test
    void existsByEmailShouldReturnTrueWhenEmailExists() {
        User user = new User(null, "Test User", "test@example.com");
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmailShouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }
}