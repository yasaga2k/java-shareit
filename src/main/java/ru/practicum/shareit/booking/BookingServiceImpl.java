package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ru.practicum.shareit.user.UserRepository userRepository;
    private final ru.practicum.shareit.item.ItemRepository itemRepository;

    @Override
    public BookingResponseDto create(Long userId, BookingDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new ValidationException("Некорректные даты бронирования");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("Только владелец может подтвердить бронирование");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.saveAndFlush(booking);

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("У пользователя нет доступа к этому бронированию");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByUser(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookings = bookingRepository.findAllByBooker(user);
        return filterAndSort(bookings, state).stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state) {
        List<Booking> all = bookingRepository.findAll().stream()
                .filter(b -> b.getItem() != null
                        && b.getItem().getOwner() != null
                        && b.getItem().getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());

        if (all.isEmpty()) {
            throw new UnauthorizedException("Пользователь не является владельцем вещей");
        }

        return filterAndSort(all, state).stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private List<Booking> filterAndSort(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(b -> switch (state.toUpperCase()) {
                    case "ALL" -> true;
                    case "CURRENT" -> !b.getStart().isAfter(now) && !b.getEnd().isBefore(now);
                    case "PAST" -> b.getEnd().isBefore(now);
                    case "FUTURE" -> b.getStart().isAfter(now);
                    case "WAITING" -> b.getStatus() == BookingStatus.WAITING;
                    case "REJECTED" -> b.getStatus() == BookingStatus.REJECTED;
                    default -> true;
                })
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .collect(Collectors.toList());
    }
}