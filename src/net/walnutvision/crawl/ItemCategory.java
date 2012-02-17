package net.walnutvision.crawl;

import net.walnutvision.orm.RowSerializable;

public class ItemCategory extends RowSerializable{
	public static final String AMAZON = "amz";
	public static final String DANGDANG = "dd";
	public static final String TAOBAO = "tb";
	public static final String JINGDONG = "jd";
	
	public static String MERCHANT = "m";
	public static String NAME = "n";
	public static String ID = "id";
	public static String URL  = "u";
	public static String DESC = "d";
	public static String PARENT_CATEGORY_ID = "pci";
	public static String PATH_TO_ROOT = "ptr";
	public static String CHILD_NODE = "cn";
	public static String IS_LEAF = "il";
	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
