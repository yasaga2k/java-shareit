package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UnauthorizedException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    private Item getItemByIdOrThrow(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с таким айди " + id + " не найдена"));
    }

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userService.getUserById(ownerId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        // Обработка requestId если он передан
        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found"));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long ownerId, Long id, ItemDto itemDto) {
        Item existing = getItemByIdOrThrow(id);

        if (!Objects.equals(existing.getOwner().getId(), ownerId)) {
            throw new UnauthorizedException("Только владелец может менять название товара");
        }

        if (itemDto.getName() != null) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existing);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = getItemByIdOrThrow(id);

        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(id);
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        ItemDto dto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .comments(commentDtos)
                .lastBooking(null)
                .nextBooking(null)
                .build();

        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                .map(item -> {
                    List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());
                    List<CommentDto> commentDtos = comments.stream()
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toList());

                    return new ItemDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getAvailable(),
                            item.getRequest() != null ? item.getRequest().getId() : null,
                            commentDtos,
                            null,
                            null
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lowerCaseText = text.toLowerCase();
        List<Item> items = itemRepository.searchByText(lowerCaseText);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, String text) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        User author = userService.getUserById(userId);

        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!hasBooking) {
            throw new ValidationException("Пользователь не брал вещь в аренду");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }
}