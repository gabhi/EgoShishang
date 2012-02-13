package net.walnutvision.util;

public class SimpleTimer {
	protected long startMilli = -1;
	protected long endMilli = -1;
	public SimpleTimer(){}
	public void start() { startMilli = System.currentTimeMillis();}
	public void end() { endMilli = System.currentTimeMillis();}
	public long elapse()
	{
		return endMilli - startMilli;
	}
}
