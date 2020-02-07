package com.smartx.net;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartx.net.msg.Message;
import com.smartx.net.msg.MessageCode;
import com.smartx.net.msg.SmartXMessage;

// import com.smartx.netface.handler.SmartXV2;
// import com.smartx.netface.message.SmartXMessage;
// import com.smartx.netface.message.SmartXMessageCodes;
// import org.springframework.stereotype.Component;
public class NetMessageMrg {
    public NetMessageMrg() {
    }
    public class Caller {
        public String op;
        public Object obj;
        public String methodName;
        public Method method;
        public Caller(String op, Object obj, String methodName) throws Exception {
            this.method = obj.getClass().getMethod(methodName, Channel.class, Message.class);
            this.op = op;
            this.obj = obj;
            this.methodName = methodName;
        }
        public void call(Channel channel, Message msg) throws Exception {
            method.invoke(obj, channel, msg);
        }
    }
    public Map<String, List<Caller>> hashMap = new HashMap<>();
    public boolean RegMessage(String op, Object obj, String methodName) {
        synchronized (this) {
            List<Caller> callbacks = GetCallbacks(op);
            for (Caller caller : callbacks) {
                if (caller.op.equals(op) && caller.obj.equals(obj) && caller.methodName.equals(methodName)) {
                    return true;
                }
            }
            Caller call = null;
            try {
                call = new Caller(op, obj, methodName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            callbacks.add(call);
        }
        return true;
    }
    List<Caller> GetCallbacks(String op) {
        List<Caller> callbacks = null;
        if (hashMap.containsKey(op)) {
            callbacks = hashMap.get(op);
        } else {
            callbacks = new ArrayList<>();
            hashMap.put(op, callbacks);
            callbacks = hashMap.get(op);
        }
        return callbacks;
    }
    public boolean UnRegMessage(String op, Object obj, String methodName) {
        synchronized (this) {
            List<Caller> callbacks = GetCallbacks(op);
            for (Caller caller : callbacks) {
                if (caller.op.equals(op) && caller.obj.equals(obj) && caller.methodName.equals(methodName)) {
                    callbacks.remove(caller);
                    return true;
                }
            }
        }
        return false;
    }
    public void process(Channel channel, Message msg) {
        if (msg.getCode() != MessageCode.CORE) return;
        SmartXMessage message = (SmartXMessage) msg;
        if (message == null) return;
        synchronized (this) {
            String op = message.msg.args.get("command");
            List<Caller> callbacks = GetCallbacks(op);
            for (Caller caller : callbacks) {
                if (caller.op.equals(op)) {
                    try {
                        caller.call(channel, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
