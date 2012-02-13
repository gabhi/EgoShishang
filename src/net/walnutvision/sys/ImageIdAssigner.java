package net.walnutvision.sys;

public abstract class  ImageIdAssigner {
	protected long id = 0;
	protected String configFilePath = null;
	protected static ImageIdAssigner assignerInst = null;
	protected ImageIdAssigner(){}
	public abstract void setUp(String configFilePath);
	public abstract void readId();
	public abstract void writeId();
	public abstract long nextId();
	public abstract void tearDown();
}
