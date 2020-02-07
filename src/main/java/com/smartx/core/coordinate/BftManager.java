package com.smartx.core.coordinate;

import com.smartx.net.Channel;
import com.smartx.net.msg.Message;

public interface BftManager {
    /**
     Starts bft manager.
     */
    void start();
    /**
     Stops bft manager.
     */
    void stop();
    /**
     Returns if the bft manager is running.

     @return
     */
    boolean isRunning();
    /**
     Callback when a message is received from network.

     @param channel the channel where the message is coming from
     @param msg     the message
     */
    void onMessage(Channel channel, Message msg);
}
