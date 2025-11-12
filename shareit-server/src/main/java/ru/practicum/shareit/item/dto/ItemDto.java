package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;
    String name;
    String description;
    Boolean available;
    Long requestId;
    List<CommentDto> comments;
    BookingDto lastBooking;
    BookingDto nextBooking;

    public ItemDto(Long id, String name, String description, Boolean available,
                   Long requestId, List<CommentDto> comments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.requestId = requestId;
        this.comments = comments != null ? comments : List.of();
        this.lastBooking = null;
        this.nextBooking = null;
    }
}