package ru.practicum.shareit.dtos;

import jakarta.validation.constraints.Future;
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
    @NotNull(message = "Item ID cannot be null")
    Long itemId;

    @NotNull(message = "Start time cannot be null")
    @Future(message = "Start time must be in the future")
    LocalDateTime start;

    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    LocalDateTime end;
}