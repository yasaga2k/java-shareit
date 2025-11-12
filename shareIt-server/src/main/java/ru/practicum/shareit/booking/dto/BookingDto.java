package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {
    Long id;
    @NotNull(message = "itemId не должен быть пустым")
    Long itemId;
    @NotNull(message = "Start не должен быть пустым")
    LocalDateTime start;
    @NotNull(message = "End date не должен быть пустым")
    LocalDateTime end;
}