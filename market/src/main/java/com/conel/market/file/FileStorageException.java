package com.conel.market.file;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
/*
catching/handling RuntimeException everywhere is a code smell — you
can't distinguish "file too big" from "npe somewhere else." A dedicated
exception type lets your @ControllerAdvice map it to a clean 400/413 response instead of a generic 500*/
