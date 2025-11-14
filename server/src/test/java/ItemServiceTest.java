import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userService, bookingRepository, commentRepository, requestRepository);
    }

    @Test
    void create_ShouldSaveItem_WhenOwnerExists() {
        Long ownerId = 1L;
        User owner = new User(ownerId, "Owner", "owner@example.com");

        ItemDto dto = new ItemDto();
        dto.setId(null);
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        dto.setRequestId(null);

        when(userService.getUserById(ownerId)).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.create(ownerId, dto);

        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getAvailable(), result.getAvailable());

        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenOwnerNotFound() {
        Long ownerId = 1L;

        ItemDto dto = new ItemDto();
        dto.setId(null);
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        dto.setRequestId(null);

        when(userService.getUserById(ownerId)).thenThrow(new NotFoundException("User not found"));

        assertThrows(NotFoundException.class, () -> itemService.create(ownerId, dto));

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void getItemById_ShouldReturnItem_WhenFound() {
        Long itemId = 1L;
        Item item = new Item(itemId, "Drill", "Powerful drill", true, new User(), null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        ItemDto result = itemService.getItemById(itemId);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());

        verify(itemRepository, times(1)).findById(itemId);
    }

    @Test
    void getItemsByOwner_ShouldReturnItems() {
        Long ownerId = 1L;
        User owner = new User(ownerId, "Owner", "owner@example.com");
        Item item = new Item(1L, "Drill", "Powerful drill", true, owner, null);

        when(itemRepository.findByOwnerId(ownerId)).thenReturn(List.of(item));

        List<ItemDto> result = itemService.getItemsByOwner(ownerId);

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());

        verify(itemRepository, times(1)).findByOwnerId(ownerId);
    }
}