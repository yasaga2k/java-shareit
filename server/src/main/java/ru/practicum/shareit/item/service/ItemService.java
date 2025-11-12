package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long ownerId, ItemDto itemDto);

    ItemDto update(Long ownerId, Long id, ItemDto itemDto);

    ItemDto getItemById(Long id);

    List<ItemDto> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Long userId, Long itemId, String text);

}
