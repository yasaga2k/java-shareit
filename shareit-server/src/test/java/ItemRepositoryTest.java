import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.ShareItServerApp;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ContextConfiguration(classes = ShareItServerApp.class)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveShouldPersistItem() {
        User owner = new User(null, "Owner", "owner@example.com");
        owner = userRepository.save(owner);

        Item item = new Item(null, "Drill", "Powerful drill", true, owner, null);

        Item saved = itemRepository.save(item);

        assertNotNull(saved.getId());
        assertEquals(item.getName(), saved.getName());
        assertEquals(item.getDescription(), saved.getDescription());
        assertEquals(item.getAvailable(), saved.getAvailable());
        assertEquals(owner.getId(), saved.getOwner().getId());
    }

    @Test
    void findByOwnerIdShouldReturnItems() {
        User owner = new User(null, "Owner", "owner@example.com");
        owner = userRepository.save(owner);

        Item item1 = new Item(null, "Drill", "Powerful drill", true, owner, null);
        Item item2 = new Item(null, "Screwdriver", "Precision screwdriver", true, owner, null);

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> items = itemRepository.findByOwnerId(owner.getId());

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Drill")));
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Screwdriver")));
    }
}