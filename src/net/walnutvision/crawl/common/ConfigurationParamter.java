package net.walnutvision.crawl.common;

public class ConfigurationParamter {

	private static final String IMAGE_ROOT_PATH = "/export/html/image";
	protected ConfigurationParamter(){}
	protected static ConfigurationParamter INSTANCE = new ConfigurationParamter();
	public static ConfigurationParamter ref()
	{
		return INSTANCE;
	}
	public String getImageRootPath()
	{
		return IMAGE_ROOT_PATH;
	}
}
