package com.egoshishang.sys;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MongoImageIdAssignerTest {

	ImageIdAssigner miia = MongoImageIdAssigner.getInstance();
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNextId() {
		for(int i = 0; i < 10; i++)
		{
			System.out.println(miia.nextId());
		}
	}

}
