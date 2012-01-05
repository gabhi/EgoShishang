package com.egoshishang.util;

import org.apache.commons.cli.Option;

public class MapRedUtility {
	public static Option constructOption(String optShort, String optName, boolean isRequired, String description)
	{
		Option opt = new Option(optShort, optName, isRequired, description);
		return opt;
	}
	

}
