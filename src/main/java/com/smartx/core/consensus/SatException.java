package com.smartx.core.consensus;
public class SatException extends Exception {
    private static final long serialVersionUID = 1L;
    public SatException(int errorCode, String message) {
        res_info = message;
        errcode = errorCode;
        serrcode = "";
    }
    public String toString() {
        return errcode + " " + res_info;
    }
    public SatException(String errorCode, String message) {
        errcode = 0;
        res_info = message;
        serrcode = errorCode;
        errcode = Integer.parseInt(errorCode);
    }
    public int errcode;
    public String serrcode;
    public String res_info;
}
