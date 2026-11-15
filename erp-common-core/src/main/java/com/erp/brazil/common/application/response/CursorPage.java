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
 * Cursor-based pagination response for efficient large dataset navigation.
 * Uses opaque cursors instead of page numbers for better performance.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> content;
    private String nextCursor;
    private String previousCursor;
    private boolean hasNext;
    private boolean hasPrevious;
    private int pageSize;
    private long totalElements; // Optional, may not be available for cursor pagination

    /**
     * Creates an empty cursor page.
     *
     * @param pageSize the requested page size
     * @param <T> the content type
     * @return empty cursor page
     */
    public static <T> CursorPage<T> empty(int pageSize) {
        return CursorPage.<T>builder()
                .content(Collections.emptyList())
                .nextCursor(null)
                .previousCursor(null)
                .hasNext(false)
                .hasPrevious(false)
                .pageSize(pageSize)
                .totalElements(0)
                .build();
    }

    /**
     * Creates a cursor page from content and cursors.
     *
     * @param content the page content (size should be pageSize + 1 to detect hasNext)
     * @param pageSize the requested page size
     * @param cursorEncoder function to encode cursor from entity
     * @param <T> the content type
     * @return cursor page
     */
    public static <T> CursorPage<T> of(List<T> content, int pageSize,
                                        Function<T, String> cursorEncoder) {
        boolean hasNext = content.size() > pageSize;
        List<T> pageContent = hasNext ? content.subList(0, pageSize) : content;

        String nextCursor = null;
        if (hasNext) {
            T lastElement = pageContent.get(pageContent.size() - 1);
            nextCursor = cursorEncoder.apply(lastElement);
        }

        return CursorPage.<T>builder()
                .content(pageContent)
                .nextCursor(nextCursor)
                .previousCursor(null) // Previous cursor would need to be passed in
                .hasNext(hasNext)
                .hasPrevious(false) // Would need previous cursor passed in
                .pageSize(pageSize)
                .totalElements(-1) // Unknown in cursor pagination
                .build();
    }

    /**
     * Creates a cursor page with explicit cursors.
     *
     * @param content the page content
     * @param nextCursor the next page cursor
     * @param previousCursor the previous page cursor
     * @param pageSize the page size
     * @param <T> the content type
     * @return cursor page
     */
    public static <T> CursorPage<T> of(List<T> content, String nextCursor,
                                        String previousCursor, int pageSize) {
        return CursorPage.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .previousCursor(previousCursor)
                .hasNext(nextCursor != null)
                .hasPrevious(previousCursor != null)
                .pageSize(pageSize)
                .totalElements(-1)
                .build();
    }

    /**
     * Maps the content to a different type.
     *
     * @param mapper the mapping function
     * @param <R> the target type
     * @return new cursor page with mapped content
     */
    public <R> CursorPage<R> map(Function<? super T, ? extends R> mapper) {
        List<R> newContent = content == null ? Collections.emptyList() :
                content.stream().map(mapper).toList();
        return CursorPage.<R>builder()
                .content(newContent)
                .nextCursor(this.nextCursor)
                .previousCursor(this.previousCursor)
                .hasNext(this.hasNext)
                .hasPrevious(this.hasPrevious)
                .pageSize(this.pageSize)
                .totalElements(this.totalElements)
                .build();
    }
}