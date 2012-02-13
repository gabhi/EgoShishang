package net.walnutvision.conf;

import java.util.HashMap;
import java.util.Map;

import net.walnutvision.sys.ImageIdAssigner;

import org.apache.hadoop.hbase.util.Bytes;


public abstract class TableConfiguration {
	public static final String MAP_TABLE_KEY = "MAP_TABLE_NAME";
	public static final String CONTENT_TABLE_KEY = "CONTENT_TABLE_NAME";
	public static final String INDEX_TABLE_KEY = "INDEX_TABLE_NAME";
	public static final String MAP_TABLE_CF = "MAP_TABLE_CF";
	public static final String CONTENT_TABLE_CF = "CONTENT_TABLE_CF";
	public static final String INDEX_TABLE_CF = "INDEX_TABLE_CF";
	
	public static final String CONTENT_IMAGE_DATA_QUALIFIER = "IMAGE_DATA_QUALIFIER";
	public static final String CONTENT_META_DATA_QUALIFIER = "META_DATA_QUALIFIER";
	public static final String CONTENT_ITEM_URL_QUALIFIER = "ITEM_URL_QUALIFIER";
	public static final String MAP_IMAGE_ID_QUALIFIER = "IMAGE_ID_QUALFIER";
	public static final String INDEX_SHORT_INDEX_QUALIFIER = "INDEX_SHORT_QUALIFIER";
	public static final String INDEX_LONG_INDEX_QUALIFIER = "INDEX_LONG_QUALIFIER";
	public static final String INDEX_SHORT_SCORE_QUALIFIER = "INDEX_SHORT_SCORE_QUALIFIER";
	public static final String INDEX_LONG_SCORE_QUALIFIER = "INDEX_LONG_SCORE_QUALIFIER";

	protected Map<String,byte[]> paramMap = null;

	protected static TableConfiguration config = null;

	public static TableConfiguration getHardcodeConfiguration()
	{
		return HardcodeTableConfiguration.getInstance();
	}
	
	private static class HardcodeTableConfiguration extends TableConfiguration
	{
		private HardcodeTableConfiguration(){}
		public static TableConfiguration getInstance()
		{
			if(config == null)
			{
				config = new HardcodeTableConfiguration();
			}
			return config;
		}
		
		@Override
		public Map<String, byte[]> setConfigParams() {
			if(paramMap == null)
			{
				paramMap = new HashMap<String,byte[]>();
				//set up the parameters
				paramMap.put(MAP_TABLE_KEY, Bytes.toBytes("egoshishang_map"));
				paramMap.put(CONTENT_TABLE_KEY, Bytes.toBytes("egoshishang_content"));
				paramMap.put(INDEX_TABLE_KEY, Bytes.toBytes("egoshishang_index"));
				paramMap.put(MAP_TABLE_CF,  Bytes.toBytes("ent"));
				paramMap.put(INDEX_TABLE_CF, Bytes.toBytes("ent"));
				paramMap.put(CONTENT_TABLE_CF, Bytes.toBytes("ent"));
				
				paramMap.put(CONTENT_IMAGE_DATA_QUALIFIER, Bytes.toBytes("imd"));
				paramMap.put(CONTENT_META_DATA_QUALIFIER,Bytes.toBytes("imf"));
				paramMap.put(CONTENT_ITEM_URL_QUALIFIER, Bytes.toBytes("iu"));
				paramMap.put(MAP_IMAGE_ID_QUALIFIER, Bytes.toBytes("id"));
				paramMap.put(INDEX_SHORT_INDEX_QUALIFIER, Bytes.toBytes("s"));
				paramMap.put(INDEX_LONG_INDEX_QUALIFIER, Bytes.toBytes("l"));
				paramMap.put(INDEX_SHORT_SCORE_QUALIFIER, Bytes.toBytes("sc"));
				paramMap.put(INDEX_LONG_SCORE_QUALIFIER, Bytes.toBytes("lc"));
			}
			return paramMap;
		}
		
	}
	public abstract Map<String, byte[]> setConfigParams();
	
	public byte[] getParam(String paramKey)
	{
		if(paramMap == null)
		{
			setConfigParams();
		}
		if(paramMap.containsKey(paramKey))
		{
			return paramMap.get(paramKey);
		}
		else
			return null;
	}
}
