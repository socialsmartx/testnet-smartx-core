package com.smartx.core.ledger;

import com.smartx.message.Message;

public class PubSubEvent2 implements com.smartx.event.PubSubEvent {
    public Message message = null;
    public PubSubEvent2(String cmd) {
        message = new Message(cmd);
    }
    public String GetMessage() {
        return Message.ToJson(message);
    }
}
