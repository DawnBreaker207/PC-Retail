package com.dawn.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class ResponsePage<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<T> content;

    Pagination pagination;


    public static <T> ResponsePage<T> of(Page<T> page) {
        return new ResponsePage<>(page);
    }

    public static <T> ResponsePage<T> of(List<T> content, int page, int size, long totalElements) {
        return new ResponsePage<>(content, page, size, totalElements);
    }

    public ResponsePage(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.pagination = new Pagination(
                page,
                size,
                totalElements,
                size > 0 ? (int) ((totalElements + size - 1) / size) : 0
        );
    }

    public ResponsePage(Page<T> page) {
        this.content = page.getContent();
        this.pagination = new Pagination(
                page.getPageable().getPageNumber(),
                page.getPageable().getPageSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination {
        Integer pageNumber;
        Integer pageSize;
        Long totalElements;
        Integer totalPages;
    }
}