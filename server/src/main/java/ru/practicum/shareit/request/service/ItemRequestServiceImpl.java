package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto requestDto) {
        User requester = userService.getUserById(userId);

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);

        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getAllByRequester(Long userId) {

        userService.getUserById(userId);

        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNot(userId, pageable).getContent();

        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }



    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        userService.getUserById(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<Item> items = itemRepository.findByRequestId(requestId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(request);
        dto.setItems(itemDtos);

        return dto;
    }
}