package net.walnutvision.crawl;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.walnutvision.nutch.PageOutlinkCustomizer;

import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.Parse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmazonProductPagingCustomizer extends PageOutlinkCustomizer {

	private static final Logger LOG = LoggerFactory.getLogger(AmazonProductPagingCustomizer.class);
	protected static final int MAX_PAGE_NUM = 400;
	@Override
	public void custermize(Parse parseResult) {
		///suppress urls with page number greater than 400
		//http://www.amazon.cn/gp/search?ie=UTF8&rh=n%3A658496051&page=9&rd=1
		Pattern pat = Pattern.compile("gp/search\\?ie=UTF8&rh=n%[\\d\\w]+&page=(\\d+)");
		Outlink[] outlinks = parseResult.getData().getOutlinks();
		List<Outlink> customizedLinks = new LinkedList<Outlink>();
		LOG.info("filtered url pattern:" + "gp/search\\?ie=UTF8&rh=n%[\\d\\w]+&page=(\\d+)");
		for(Outlink link : outlinks)
		{
			String url = link.getToUrl();
			Matcher mat = pat.matcher(url);
			if(mat.find())
			{
				int pageNum = Integer.valueOf(mat.group(1));
				///only retain those pages less than MAX_PAGE_NUM
				if(pageNum <= MAX_PAGE_NUM)
				{
					customizedLinks.add(link);
				}
			}
			else
			{
				customizedLinks.add(link);
			}
		}
		Outlink[] newOutinks = customizedLinks.toArray(new Outlink[customizedLinks.size()]);
		///change the out links
		parseResult.getData().setOutlinks(newOutinks);
	}

}
