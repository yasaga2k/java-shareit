package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Transactional
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

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("У пользователя нет доступа к этому бронированию");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByUser(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerOrderByStartDesc(user);
            case "CURRENT" -> bookingRepository.findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now());
            case "PAST" -> bookingRepository.findByBookerAndEndIsBeforeOrderByStartDesc(user, LocalDateTime.now());
            case "FUTURE" -> bookingRepository.findByBookerAndStartIsAfterOrderByStartDesc(user, LocalDateTime.now());
            case "WAITING" -> bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED);
            default -> throw new IllegalArgumentException("Неизвестный статус: " + state);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByItemOwnerOrderByStartDesc(owner);
            case "CURRENT" -> bookingRepository.findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(owner, LocalDateTime.now(), LocalDateTime.now());
            case "PAST" -> bookingRepository.findByItemOwnerAndEndIsBeforeOrderByStartDesc(owner, LocalDateTime.now());
            case "FUTURE" -> bookingRepository.findByItemOwnerAndStartIsAfterOrderByStartDesc(owner, LocalDateTime.now());
            case "WAITING" -> bookingRepository.findByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findByItemOwnerAndStatusOrderByStartDesc(owner, BookingStatus.REJECTED);
            default -> throw new IllegalArgumentException("Неизвестный статус: " + state);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}