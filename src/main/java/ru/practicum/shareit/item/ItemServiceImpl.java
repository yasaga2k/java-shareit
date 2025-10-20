package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UnauthorizedException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final Map<Long, Item> items = new HashMap<>();
    private final UserService userService;
    private long nextId = 1;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    private void validateOwnerExistsOrThrow(Long ownerId) {
        if (!userService.existsById(ownerId)) {
            throw new NotFoundException("Owner not found with id: " + ownerId);
        }
    }

    private Item getItemByIdOrThrow(Long id) {
        Item item = items.get(id);
        if (item == null) {
            throw new NotFoundException("Вещь с таким айди " + id + " не найдена");
        }
        return item;
    }

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        validateOwnerExistsOrThrow(ownerId);

        Item item = ItemMapper.toItem(itemDto);
        Long id = nextId++;
        item.setId(id);

        User owner = userService.getUserById(ownerId);
        item.setOwner(owner);

        items.put(id, item);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long ownerId, Long id, ItemDto itemDto) {
        Item existing = getItemByIdOrThrow(id);

        if (!existing.getOwner().getId().equals(ownerId)) {
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
        return ItemMapper.toItemDto(existing);
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = getItemByIdOrThrow(id);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerCase = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable())
                .filter(item -> item.getName().toLowerCase().contains(lowerCase) ||
                        item.getDescription().toLowerCase().contains(lowerCase))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
