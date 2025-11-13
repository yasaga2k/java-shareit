import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.ShareItServerApp;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ContextConfiguration(classes = ShareItServerApp.class)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void saveShouldPersistBooking() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        Item item = new Item(null, "Drill", "Powerful drill", true, owner, null);

        owner = userRepository.save(owner);
        booker = userRepository.save(booker);
        item = itemRepository.save(item);

        Booking booking = new Booking(null, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), item, booker, BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);

        assertNotNull(saved.getId());
        assertEquals(booking.getStart(), saved.getStart());
        assertEquals(booking.getEnd(), saved.getEnd());
        assertEquals(booking.getItem().getId(), saved.getItem().getId());
        assertEquals(booking.getBooker().getId(), saved.getBooker().getId());
        assertEquals(booking.getStatus(), saved.getStatus());
    }

    @Test
    void findByBookerOrderByStartDescShouldReturnBookings() {
        User owner = new User(null, "Owner", "owner@example.com");
        User booker = new User(null, "Booker", "booker@example.com");
        Item item = new Item(null, "Drill", "Powerful drill", true, owner, null);

        owner = userRepository.save(owner);
        booker = userRepository.save(booker);
        item = itemRepository.save(item);

        Booking b1 = new Booking(null, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), item, booker, BookingStatus.WAITING);
        Booking b2 = new Booking(null, LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(4), item, booker, BookingStatus.APPROVED);

        bookingRepository.save(b1);
        bookingRepository.save(b2);

        List<Booking> bookings = bookingRepository.findByBookerOrderByStartDesc(booker);

        assertEquals(2, bookings.size());
        assertEquals(b2.getStart(), bookings.getFirst().getStart());
    }
}