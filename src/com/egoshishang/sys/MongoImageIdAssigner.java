package com.egoshishang.sys;

import com.egoshishang.mongodb.SystemConfiguration;

public class MongoImageIdAssigner extends ImageIdAssigner {

	private static ImageIdAssigner assigner = null;
	protected MongoImageIdAssigner()
	{
		
	}
	public static ImageIdAssigner getInstance()
	{
		if(assigner == null)
		{
			assigner = new MongoImageIdAssigner();
		}
		return assigner;
	}
	
	@Override
	public void setUp(String configFilePath) {
		// TODO Auto-generated method stub

	}

	@Override
	public void readId() {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeId() {
		// TODO Auto-generated method stub
	}

	@Override
	public long nextId() {
		SystemConfiguration sc = SystemConfiguration.getInstance();
		return sc.nextImageId();
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub

	}

}
