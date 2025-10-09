package com.mirakyan.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;
    @NotBlank(message = "title не должен быть пустым")
    private String title;
    @NotBlank(message = "text не должен быть пустым")
    private String text;
    @NotNull(message = "tags не должны быть null, используйте []")
    private List<String> tags;
    private Integer likesCount;
    private Integer commentsCount;
}
