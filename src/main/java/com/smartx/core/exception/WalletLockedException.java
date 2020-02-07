package com.smartx.core.exception;
public class WalletLockedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public WalletLockedException() {
        super("Wallet is locked");
    }
}
