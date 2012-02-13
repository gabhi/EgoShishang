package net.walnutvision.mongodb;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;

public class MongoInstance {
	private static Mongo mongoInst = null;
	private static GridFS imageFS = null;
	public static void initMongoInst(String host, int port)
	{
		if(mongoInst == null)
		{
			try {
				if(port == -1)
				mongoInst = new Mongo(host);
				else
					mongoInst = new Mongo(host,port);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MongoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static Mongo getMongo()
	{
		return mongoInst;
	}
	
	public static void shutDown()
	{
		mongoInst.close();
	}
	
	public static DB getDB(String dbName)
	{
		if(mongoInst == null)
		{
			MongoInstance.initMongoInst(SystemConfiguration.MONGO_HOST, -1);
		}
		return mongoInst.getDB(dbName);
	}
	
	public static GridFS getImageFS()
	{
		if(imageFS == null)
		{
			DB imageDb = MongoInstance.getDB(SystemConfiguration.IMAGE_DB_NAME);
			imageFS = new GridFS(imageDb);
			
		}
		return imageFS;
	}
	
}
