package com.erp.brazil.common.application.usecase;

import com.erp.brazil.common.application.response.PageableResponse;
import com.erp.brazil.common.application.response.CursorPage;

import java.io.Serializable;

/**
 * Marker interface for use cases (commands and queries).
 * Follows the CQRS pattern - commands modify state, queries return data.
 */
public interface UseCase<IN, OUT> extends Serializable {

    /**
     * Executes the use case with the given input.
     *
     * @param input the input parameters
     * @return the output result
     */
    OUT execute(IN input);

    /**
     * Base interface for command inputs (write operations).
     */
    interface Command<IN> extends UseCase<IN, Void> {
    }

    /**
     * Base interface for query inputs (read operations).
     */
    interface Query<IN, OUT> extends UseCase<IN, OUT> {
    }

    /**
     * Base interface for paginated queries.
     */
    interface PagedQuery<IN, OUT> extends Query<IN, PageableResponse<OUT>> {
    }

    /**
     * Base interface for cursor-based paginated queries.
     */
    interface CursorQuery<IN, OUT> extends Query<IN, CursorPage<OUT>> {
    }
}