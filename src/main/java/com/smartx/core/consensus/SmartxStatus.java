package com.smartx.core.consensus;
public class SmartxStatus {
    public enum STATUS {
        SMARTX_STATUS_INIT, SMARTX_STATUS_LOADDB, SMARTX_STATUS_NORMAL, SMARTX_STATUS_MINING, SMARTX_STATUS_MINESTOP, SMARTX_STATUS_NETSYNC,
    }
    public SmartxStatus.STATUS Status = SmartxStatus.STATUS.SMARTX_STATUS_INIT;
    public SmartxStatus() {
        Status = STATUS.SMARTX_STATUS_INIT;
    }
    public STATUS GetSmartxStatus() {
        return Status;
    }
    public STATUS GetStatus() {
        return Status;
    }
    public void SetStatus(SmartxStatus.STATUS sts) {
        Status = sts;
    }
}
