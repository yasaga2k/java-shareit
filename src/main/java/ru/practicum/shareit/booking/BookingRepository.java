package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Для пользователя (booker)
    List<Booking> findByBookerOrderByStartDesc(User booker);

    List<Booking> findByBookerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(User booker, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerAndEndIsBeforeOrderByStartDesc(User booker, LocalDateTime end);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, BookingStatus status, LocalDateTime end);

    List<Booking> findByBookerAndStartIsAfterOrderByStartDesc(User booker, LocalDateTime start);

    List<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    // Для владельца (owner)
    List<Booking> findByItemOwnerOrderByStartDesc(User owner);

    List<Booking> findByItemOwnerAndStartIsBeforeAndEndIsAfterOrderByStartDesc(User owner, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemOwnerAndEndIsBeforeOrderByStartDesc(User owner, LocalDateTime end);

    List<Booking> findByItemOwnerAndStartIsAfterOrderByStartDesc(User owner, LocalDateTime start);

    List<Booking> findByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status);
}