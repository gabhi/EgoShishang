package com.egoshishang.mongodb;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class SystemConfigurationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMongoConnection()
	{
		MongoInstance.initMongoInst("zixuanpc.stanford.edu", -1);
		SystemConfiguration sc = SystemConfiguration.getInstance();
		for(int i = 0; i < 10; i++)
		{
			System.out.println(sc.nextImageId());
		}
	}
	
	@Test
	public void testUpdateTime()
	{
		MongoInstance.initMongoInst("zixuanpc.stanford.edu", -1);
		SystemConfiguration sc = SystemConfiguration.getInstance();
		for(int i = 0; i < 10; i++)
		{
			System.out.println(sc.nextUpdateTime());
		}
	}
}
