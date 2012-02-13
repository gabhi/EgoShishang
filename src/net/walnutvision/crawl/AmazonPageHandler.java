package net.walnutvision.crawl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import net.walnutvision.nutch.PageHandler;
import net.walnutvision.util.WebFile;

public class AmazonPageHandler extends PageHandler {

	@Override
	public boolean accept() {
		return true;
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
	}
	public void crawlPage(String url)
	{
		try {
			WebFile wf = new WebFile(url);
			String content = (String)wf.getContent();
			System.out.println(content);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
