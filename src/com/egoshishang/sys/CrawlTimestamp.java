package com.egoshishang.sys;

import com.egoshishang.mongodb.SystemConfiguration;

public class CrawlTimestamp {
protected long curTimestamp = 0;

protected static CrawlTimestamp ct = new CrawlTimestamp();
protected CrawlTimestamp()
{
	readTimestamp();
}

public static CrawlTimestamp getInstance()
{
	return ct;
}

///crawl timestamp will be saved in the mongo database
public void generateTimestamp()
{

	this.curTimestamp = SystemConfiguration.getInstance().nextUpdateTime();
}

public long getTimestamp()
{
	return curTimestamp;
}

protected void readTimestamp()
{
	//TODO: read current timestamp
}
protected void writeTimestamp()
{
	//TODO: write back the new time stamp
}
}
