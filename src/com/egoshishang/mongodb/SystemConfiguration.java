package com.egoshishang.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SystemConfiguration {
	protected DB configDb = null;
	protected DBCollection configColl = null;
	public static final String CONFIG_DB_NAME  = "system_config_db";
	public static final String COLLECTION_NAME = "system_config";

	public static final String CRAWL_TIME_KEY = "crawl_time";
	public static final String IMAGE_ID_KEY = "image_id";
	public static final String ROW_KEY = "rowkey";
	public static final String ROW_VALUE = "value";
	public static final String IMAGE_DB_NAME = "image";
	public static final String MONGO_HOST = "zixuanpc.stanford.edu";

	private static SystemConfiguration config = new SystemConfiguration();
	protected SystemConfiguration()
	{
		configDb = MongoInstance.getDB(CONFIG_DB_NAME);
		configColl = configDb.getCollection(COLLECTION_NAME);
	}
	public static SystemConfiguration getInstance()
	{
		return config;
	}
	
	public long nextImageId()
	{
		
		BasicDBObject query = new BasicDBObject();
		query.put(ROW_KEY, IMAGE_ID_KEY);
		DBCursor dbCur = configColl.find(query);
		long nextId = 0;
		DBObject res = new BasicDBObject();
		if(dbCur.hasNext())
		{
			res = dbCur.next();
			nextId = (Long)res.get(ROW_VALUE);
		}
		
		else
		{
			res.put(ROW_KEY, IMAGE_ID_KEY);
		}
		res.put(ROW_VALUE, nextId+1);
		configColl.save(res);
		return nextId;
	}
	
	public long nextUpdateTime()
	{
		
		DBObject obj = new BasicDBObject();
		obj.put(ROW_KEY, CRAWL_TIME_KEY);
		DBCursor cursor = configColl.find(obj);
		DBObject res = new BasicDBObject();
		res.put(ROW_KEY, CRAWL_TIME_KEY);
		if(cursor.hasNext())
		{
			res = cursor.next();
		}
		long ut = System.currentTimeMillis();
		res.put(ROW_VALUE, ut);
		configColl.save(res);
		return ut;
	}
}
