package com.erp.brazil.common.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Standard paginated response for REST APIs.
 * Compatible with Spring Data Page and custom pagination.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageableResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
    private SortInfo sort;

    /**
     * Creates an empty pageable response.
     *
     * @param pageNumber the page number (0-based)
     * @param pageSize the page size
     * @param <T> the content type
     * @return empty response
     */
    public static <T> PageableResponse<T> empty(int pageNumber, int pageSize) {
        return PageableResponse.<T>builder()
                .content(Collections.emptyList())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .empty(true)
                .build();
    }

    /**
     * Creates a pageable response from a list and total count.
     *
     * @param content the page content
     * @param pageNumber the page number (0-based)
     * @param pageSize the page size
     * @param totalElements the total number of elements
     * @param <T> the content type
     * @return pageable response
     */
    public static <T> PageableResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;
        return PageableResponse.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageNumber == 0)
                .last(pageNumber >= totalPages - 1)
                .empty(content == null || content.isEmpty())
                .build();
    }

    /**
     * Maps the content to a different type.
     *
     * @param mapper the mapping function
     * @param <R> the target type
     * @return new pageable response with mapped content
     */
    public <R> PageableResponse<R> map(Function<? super T, ? extends R> mapper) {
        List<R> newContent = content == null ? Collections.emptyList() :
                content.stream().map(mapper).collect(java.util.stream.Collectors.toList());
        return PageableResponse.<R>builder()
                .content(newContent)
                .pageNumber(this.pageNumber)
                .pageSize(this.pageSize)
                .totalElements(this.totalElements)
                .totalPages(this.totalPages)
                .first(this.first)
                .last(this.last)
                .empty(this.empty)
                .sort(this.sort)
                .build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean sorted;
        private boolean unsorted;
        private List<SortOrder> orders;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortOrder implements Serializable {
        private static final long serialVersionUID = 1L;
        private String property;
        private Direction direction;
        private boolean ignoreCase;
        private boolean nullHandling;

        public enum Direction {
            ASC, DESC
        }
    }
}