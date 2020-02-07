package com.smartx.util.exception;
public class LauncherException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public LauncherException() {
    }
    public LauncherException(String message) {
        super(message);
    }
    public LauncherException(String message, Throwable cause) {
        super(message, cause);
    }
    public LauncherException(Throwable cause) {
        super(cause);
    }
    public LauncherException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
