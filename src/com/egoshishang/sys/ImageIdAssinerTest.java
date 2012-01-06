package com.egoshishang.sys;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ImageIdAssinerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLocalFileImageIdAssigner()
	{
		String idFp = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/image_id";
		ImageIdAssigner assigner = LocalFileImageIdAssigner.getInstance();
		assigner.setUp( idFp );
		for(int i = 0; i < 10; i++)
		{
			System.out.println( assigner.nextId());
		}
	}
}
