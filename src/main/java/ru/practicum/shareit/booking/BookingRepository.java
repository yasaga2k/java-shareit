package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBooker(User booker);

    List<Booking> findAllByItem(Item item);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, BookingStatus status, LocalDateTime end);
}