package net.walnutvision.mongodb;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CategoryPageTrackerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testUpdatePageIndex()
	{
		CategoryPageTracker cpt = CategoryPageTracker.getInstance();
		String categoryId = "23463";
		for(int i = 0; i < 10; i++)
		{
			int nextPageIndex = cpt.getPageIndex(categoryId);
			if(nextPageIndex == 1)
			{
				cpt.setMaxPage(categoryId, 200);
			}
			System.out.println(nextPageIndex);
		}
	}
	

}
