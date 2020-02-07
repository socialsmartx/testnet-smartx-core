package com.smartx.api;
public class NetMessageMrg {
    public NetMessageMrg() {
    }
    //	public SmartXV2 smartXV2;
    //
    //	public interface CallBack {
    //		public void  Run(SmartXMessage msg);
    //	}
    //	public class Caller {
    //		public SmartXMessageCodes op;
    //		public CallBack callInterface;
    //
    //		public Caller(SmartXMessageCodes op,CallBack callInterface) {
    //			this.callInterface = callInterface;
    //			this.op = op;
    //		}
    //		public void call(SmartXMessage msg) {
    //			callInterface.Run(msg);
    //		}
    //	}
    //	public List<Caller> callbacks = new ArrayList<Caller>();
    //	public boolean RegMessage(SmartXMessageCodes op,CallBack callback)
    //	{
    //		for(int i = 0;i<callbacks.size();i++)
    //		{
    //			Caller caller = callbacks.get(i);
    //			if ( caller.op == op && caller.callInterface == callback)
    //			{
    //				return true;
    //			}
    //		}
    //
    //		Caller call = new Caller(op,callback);
    //		callbacks.add(call);
    //		return true;
    //	}
    //	public boolean UnRegMessage(SmartXMessageCodes op,CallBack callback)
    //	{
    //		for(int i = 0;i<callbacks.size();i++)
    //		{
    //			Caller caller = callbacks.get(i);
    //			if ( caller.op == op && caller.callInterface == callback)
    //			{
    //				callbacks.remove(i);
    //				return true;
    //			}
    //		}
    //		return false;
    //	}
    //	public void process(SmartXMessageCodes op, SmartXMessage msg)
    //	{
    //		for(int i = 0;i<callbacks.size();i++)
    //		{
    //			Caller caller = callbacks.get(i);
    //			if ( caller.op == op ) {
    //				callbacks.get(i).call(msg);
    //			}
    //		}
    //	}
    //	public void sendMessage(SmartXMessage msg)
    //	{
    //		//if(smartXV2!=null)
    //			smartXV2.sendMessage(msg);
    //	}
}
