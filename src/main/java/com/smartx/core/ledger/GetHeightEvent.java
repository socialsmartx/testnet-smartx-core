package com.smartx.core.ledger;

import com.smartx.event.PubSubEvent;
import com.smartx.message.Message;

public class GetHeightEvent implements PubSubEvent {
    public Message message = null;
    public GetHeightEvent(String cmd) {
        message = new Message(cmd);
    }
    public String GetMessage() {
        return Message.ToJson(message);
    }
}
