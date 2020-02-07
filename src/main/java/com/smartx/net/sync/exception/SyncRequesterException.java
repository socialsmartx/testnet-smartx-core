package com.smartx.net.sync.exception;
public class SyncRequesterException extends Exception {
    private static final long serialVersionUID = 1L;
    public SyncRequesterException(String s) {
        super(s);
    }
    public SyncRequesterException(String message, Throwable cause) {
        super(message, cause);
    }
    public SyncRequesterException(Throwable cause) {
        super(cause);
    }
}
