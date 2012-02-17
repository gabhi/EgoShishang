package net.walnutvision.crawl.common;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import net.walnutvision.crawl.AmazonItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClassIntegerMapperTest {

	protected ClassIntegerMapper cim  = ClassIntegerMapper.ref();
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIntegerToObject() {
		try {
			Object obj = cim.integerToObject(0);
			System.out.println(obj.getClass().getName());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testObjectToInteger() {
		int objId = cim.objectToInteger(new AmazonItem());
		System.out.println(objId);
	}

	@Test
	public void testClassToInteger() {
		fail("Not yet implemented");
	}

}
