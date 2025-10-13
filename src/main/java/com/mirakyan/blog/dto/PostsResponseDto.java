package com.mirakyan.blog.dto;

import com.mirakyan.blog.dto.PostDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostsResponseDto {
    private List<PostDto> posts;
    private boolean hasPrev;
    private boolean hasNext;
    private int lastPage;
}
