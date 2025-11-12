package ru.practicum.shareit.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotBlank(message = "Comment text must not be blank")
    private String text;
}