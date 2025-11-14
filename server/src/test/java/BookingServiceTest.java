import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }

    @Test
    void createShouldSaveBookingWhenValid() {
        Long userId = 1L; // booker
        Long itemId = 1L;
        User booker = new User(userId, "Booker", "booker@example.com");
        User owner = new User(2L, "Owner", "owner@example.com");
        Item item = new Item(itemId, "Drill", "Powerful drill", true, owner, null);
        BookingDto dto = new BookingDto(null, itemId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(java.util.Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto result = bookingService.create(userId, dto);

        assertEquals(BookingStatus.WAITING, result.getStatus());

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createShouldThrowNotFoundExceptionWhenItemNotFound() {
        Long userId = 1L;
        Long itemId = 1L;
        User booker = new User(userId, "Booker", "booker@example.com");
        BookingDto dto = new BookingDto(null, itemId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(userId, dto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createShouldThrowValidationExceptionWhenItemNotAvailable() {
        Long userId = 1L;
        Long itemId = 1L;
        User booker = new User(userId, "Booker", "booker@example.com");
        Item item = new Item(itemId, "Drill", "Powerful drill", false, booker, null); // not available
        BookingDto dto = new BookingDto(null, itemId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2));

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(java.util.Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(userId, dto));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getAllByUserShouldReturnBookings() {
        Long userId = 1L;
        User booker = new User(userId, "Booker", "booker@example.com");
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), new Item(), booker, BookingStatus.WAITING);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(booker));
        when(bookingRepository.findByBookerOrderByStartDesc(booker)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByUser(userId, "ALL");

        assertEquals(1, result.size());

        verify(bookingRepository, times(1)).findByBookerOrderByStartDesc(booker);
    }
}