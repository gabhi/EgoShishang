package net.walnutvision.mongodb;


import net.walnutvision.orm.HBaseObject.ItemImage;
import net.walnutvision.util.CommonUtils;
import net.walnutvision.util.MyBytes;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

public class MongoUtils {

	public static boolean saveItemImage(ItemImage image)
	{
		boolean saveSuc = true;
		try{
			GridFS imageFS = MongoInstance.getImageFS();
			String hexImageFileName = MyBytes.toObject((image.getRowKey()),MyBytes.getDummyObject(String.class)) + ".jpg";
//			System.out.println(hexImageFileName);
			GridFSInputFile imageFile = imageFS.createFile(image.getImageData());
			imageFile.setFilename(hexImageFileName);
			imageFile.save();	
		}
		catch(Exception e)
		{
			e.printStackTrace();
			saveSuc = false;
		}
		return saveSuc;
	}
	
	public static boolean removeItemImage(String fileName)
	{
		boolean rmSuc = true;
		try
		{
			GridFS imageFS = MongoInstance.getImageFS();
			imageFS.remove(fileName);			
		}catch(Exception e)
		{
			e.printStackTrace();
			rmSuc = false;
		}
		return rmSuc;
	}
}
