package com.smartx.core.blockchain;

import org.springframework.stereotype.Component;

import com.smartx.core.SmartxCore;

@Component
public class SmartxFactory {
    public static SmartxCore smartx = new SmartxCore();
    public SmartxFactory() {
        smartx.start();
    }
    public static SmartxCore CreateSmartx() {
        return smartx;
    }
}
