package org.myapplication.exceptions;

import org.myapplication.tools.ColoredOutput;

public class QueryException extends RuntimeException {
    public QueryException(String message) {
        super(ColoredOutput.yellow(message));
    }

    public QueryException(String message, Throwable cause) {
        super(ColoredOutput.yellow(message), cause);
    }
}
