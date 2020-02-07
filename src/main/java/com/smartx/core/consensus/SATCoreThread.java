package com.smartx.core.consensus;
public class SATCoreThread extends Thread {
    private GeneralMine service;
    public void setSatService(GeneralMine service) {
        this.service = service;
    }
    @Override
    public void run() {
        service.run();
    }
}
