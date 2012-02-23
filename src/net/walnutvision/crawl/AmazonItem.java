package net.walnutvision.crawl;

import net.walnutvision.crawl.common.Item;
import net.walnutvision.util.MyBytes;

public class AmazonItem extends Item {
	public static final String AMAZON = "amz";
	public static final byte[] AMAZON_BYTE = MyBytes.toBytes(AMAZON);
	public static final String LIST_PRICE = "lp";
	public static final String ACTUAL_PRICE = "ap";
	
	@Override
	public String getMerchant() {
		return AMAZON;
	}

	@Override
	public Item getInstance() {
		return new AmazonItem();
	}
}
