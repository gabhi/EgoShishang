package net.walnutvision.crawl.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import net.walnutvision.conf.GlobalConfiguration;
import net.walnutvision.data.ImageKeyGenerate;
import net.walnutvision.orm.RowSerializable;
import net.walnutvision.sys.HBaseImageIdAssigner;
import net.walnutvision.sys.ImageIdAssigner;
import net.walnutvision.util.CommonUtils;
import net.walnutvision.util.MyBytes;

public class ItemImage extends RowSerializable {

	///column name for item id(list)
	public static final String ITEM_ID = "ii";
	///image id, a long type, globally unique and never re-use
	public static final String IMAGE_ID = "id";
	protected byte[] imageData = null;
	protected String imageFileName = null;
	protected static ImageIdAssigner idAssigner = HBaseImageIdAssigner.getInstance();
	///compute the image hash key
	public  byte[] generateKey()
	{
		ImageKeyGenerate ikg = ImageKeyGenerate.getMD5SHA256();
		this.rowKey = MyBytes.toBytes(CommonUtils.byteArrayToHexString(ikg.generate(imageData)));
		return this.rowKey;
	}
	/**
	 * generate an unique image id for current image
	 * 
	 * @param idAssigner object responsile for producing image id
	 */
	public static void setIdAssigner(ImageIdAssigner idAssigner)
	{
		if(ItemImage.idAssigner == null)
			ItemImage.idAssigner = idAssigner;
	}
	/**
	 * generate long id
	 * 
	 * @throws NullPointerException
	 */
	public void generateId() throws NullPointerException
	{
		if(idAssigner == null)
			throw new NullPointerException("image id assigner not specified");
		if(!this.colIndexMap.containsKey(IMAGE_ID))
		{
			//add the id
			this.addColumn(IMAGE_ID, MyBytes.toBytes(idAssigner.nextId()));
		}
	}
	
	public String getImageFileName()
	{
		if(imageFileName == null)
		{
			imageFileName = (String)MyBytes.toObject(this.rowKey,MyBytes.getDummyObject(String.class));
			imageFileName = imageFileName + ".jpg";
		}
		return imageFileName;
	}
	
	public boolean saveImage() throws IOException
	{
		//define how to save image
		//simply save image to file system and keep the path
		///get the image root path
		Configuration config = GlobalConfiguration.CONFIG;
		String rootPath = config.get(GlobalConfiguration.CRAWL_IMAGE_ROOT, "/export/public_html/image");
		File imageDir = new File(rootPath);
		if(!imageDir.exists())
		{
			imageDir.mkdirs();
		}
		///image name is image data hash in hex format
		String fullPath = rootPath + "/" + this.getImageFileName();
		FileOutputStream fos = new FileOutputStream(fullPath);
		fos.write(this.getImageData());
		fos.close();
		return true;
	}
	
	public boolean removeImage()
	{
		//TODO: add code to remove image from gridfs
		///get the image root path
		String rootPath = ConfigurationParamter.ref().getImageRootPath();
		///image name is image data hash in hex format
		String fullPath = rootPath + "/" + this.getImageFileName();
		///remove it from file path
		File file = new File(fullPath);
		if(file.exists())
		{
			file.delete();
		}
		return true;
	}
	
	public void setImageData(byte[] imageData)
	{
		this.imageData = imageData;
	}
	
	public byte[] getImageData()
	{
		return this.imageData;
	}
	
	@Override
	public String getTableName() {
		return "image";
	}
}
