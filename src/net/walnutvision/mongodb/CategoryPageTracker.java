package net.walnutvision.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class CategoryPageTracker {
	protected DB configDb = null;
	protected DBCollection pageColl = null;
	public static final String pageCollName = "category_page";
	public static final String CATEGORY_KEY = "cid";
	public static final String PAGE_INDEX = "pi";
	public static final String MAX_PAGE_INDEX = "mpi";
	public static final String PAGE_URL = "pu";
	
	protected static CategoryPageTracker cpt = new CategoryPageTracker();
	protected CategoryPageTracker()
	{
		configDb = MongoInstance.getDB(SystemConfiguration.CONFIG_DB_NAME);
		pageColl = configDb.getCollection(pageCollName);
	}
	public static CategoryPageTracker getInstance()
	{
		return cpt;
	}
	protected DBObject findObject(String categoryId)
	{
		DBObject queryObj = new BasicDBObject();
		queryObj.put(CATEGORY_KEY,categoryId);
		DBCursor cursor = pageColl.find(queryObj);
		DBObject resObj = new BasicDBObject();
		
		if(cursor.hasNext())
		{
			resObj = cursor.next();
		}
		else
		{
			resObj.put(CATEGORY_KEY, categoryId);
			resObj.put(PAGE_INDEX, 1);
			resObj.put(MAX_PAGE_INDEX, 400);
			pageColl.save(resObj);
		}
		return resObj;		
	}
	public int getMaxPage(String categoryId)
	{
		DBObject resObj = findObject(categoryId);
		return (Integer)resObj.get(MAX_PAGE_INDEX);
	}
	
	public void setMaxPage(String categoryId, int maxPage)
	{
		DBObject resObj = findObject(categoryId);
		resObj.put(MAX_PAGE_INDEX, maxPage);
		pageColl.save(resObj);
	}

	public String getPageUrl(String categoryId)
	{
		DBObject resObj = findObject(categoryId);
		if(resObj.containsField(PAGE_URL))
		{
			return (String)resObj.get(PAGE_URL);
		}
		else
			return null;
	}
	
	public void setNextPageUrl(String categoryId, String url)
	{
		DBObject resObj = findObject(categoryId);
		resObj.put(PAGE_URL, url);
		//advanced the page index
		Integer curIndex = (Integer)resObj.get(PAGE_INDEX);
		resObj.put(PAGE_INDEX, curIndex + 1);
		pageColl.save(resObj);
	}
	
	public int  getPageIndex(String categoryId)
	{
		DBObject resObj = findObject(categoryId);
		Integer curIndex = (Integer)resObj.get(PAGE_INDEX);
		return curIndex;
	}
	
}
