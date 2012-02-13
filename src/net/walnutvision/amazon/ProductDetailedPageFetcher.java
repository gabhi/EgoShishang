package net.walnutvision.amazon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;


public class ProductDetailedPageFetcher {
	protected String inputPath = null;
	protected String outputPath = null;
	protected BufferedReader inputReader = null;
	protected BufferedWriter outputWriter = null;
	protected SignedRequestsHelper ppaHelper = null;
	protected Map<String,String> params = null;
	public static class Response
	{
		public String detailedUrl = null;
		public long numResults = 0;
		public long numPage = 0;
		@Override
		public String toString()
		{
			return detailedUrl + "\t" + numResults + "\t" + numPage;
		}
	}
	
	public ProductDetailedPageFetcher(String inputPath, String outputPath)
	{
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		try {
			inputReader = new BufferedReader(new FileReader(this.inputPath));
			outputWriter = new BufferedWriter(new FileWriter(this.outputPath));
			 ppaHelper = AmazonPPA.getRequesetHelper();
		     params = new HashMap<String, String>();
		    AmazonPPA.setCommonParams(params);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Response querySingleCategory(long categoryId)
	{
		Response response = new Response();
		AmazonPPA.setSearchByBrowseNode(params, categoryId);
		try {
			Document doc = AmazonPPA.retrieveDocument(ppaHelper, params);
			response.detailedUrl = doc.getElementsByTagName("MoreSearchResultsUrl").item(0).getTextContent();
			response.numResults = Long.valueOf(doc.getElementsByTagName("TotalResults").item(0).getTextContent());
			response.numPage = response.numResults/12 + 1;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	public void runQuery()
	{
		String line;
		try {
			int i = 0;
			while( (line = inputReader.readLine()) != null)
			{
				String[] fields = line.split("\t");
				long cateId = Long.valueOf(fields[0]);
				Response response = querySingleCategory(cateId);
				outputWriter.append(cateId + "\t" + response + "\n");
				i++;
				if(i % 10 == 0)
				{
					outputWriter.flush();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void finalize()
	{
		try {
			inputReader.close();
			outputWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
//		String inputPath = args[0];
//		String outputPath = args[1];
		String inputPath = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/book_leaf";
		String outputPath = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/book_detail_page";
		ProductDetailedPageFetcher fetcher = new ProductDetailedPageFetcher(inputPath,outputPath);
		fetcher.runQuery();
	}
}
