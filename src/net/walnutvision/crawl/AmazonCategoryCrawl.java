package net.walnutvision.crawl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.walnutvision.util.MyBytes;
import net.walnutvision.util.WebFile;

public class AmazonCategoryCrawl {
	protected ItemCategory rootCat = null;
	protected String rootCatId = "658390051";
	protected String rootCatName = "图书";
	protected String rootCatPath = "";
	protected String merchant = "AMZ";
	protected String rootCatUrl = "http://www.amazon.cn/s?ie=UTF8&rh=n%3A658390051&page=1";
	public AmazonCategoryCrawl()
	{
		rootCat = new ItemCategory();

		rootCat.addColumn(ItemCategory.ID, MyBytes.toBytes(rootCatId));
		rootCat.addColumn(ItemCategory.NAME, MyBytes.toBytes(rootCatName));
		rootCat.addColumn(ItemCategory.PATH_TO_ROOT, MyBytes.toBytes(""));
		//specify root category
		rootCat.addColumn(ItemCategory.PARENT_CATEGORY_ID, MyBytes.toBytes("0"));
		rootCat.addColumn(ItemCategory.URL, MyBytes.toBytes(rootCatUrl));
		rootCat.addColumn(ItemCategory.MERCHANT, MyBytes.toBytes(merchant));
	}
	
	protected List<ItemCategory> parseCategory(ItemCategory parentCat) throws MalformedURLException, UnknownHostException, FileNotFoundException, IOException
	{
		///get the category url
		String parentUrl = (String)MyBytes.toObject(parentCat.getColumnFirst(ItemCategory.URL),MyBytes.getDummyObject(String.class));
//		System.out.println("Parent Category Url:" + parentUrl);
		byte[] parentIdByte = parentCat.getColumnFirst(ItemCategory.ID);
		String parentId = (String)MyBytes.toObject(parentIdByte, MyBytes.getDummyObject(String.class));
		String parentName = (String)MyBytes.toObject(parentCat.getColumnFirst(ItemCategory.NAME), MyBytes.getDummyObject(String.class));
		String parentPath = (String)MyBytes.toObject(parentCat.getColumnFirst(ItemCategory.PATH_TO_ROOT), MyBytes.getDummyObject(String.class));
		///download the html page of parent category
		WebFile wf = new WebFile(parentUrl);
		///parse the html
		String content = (String)wf.getContent();
//		System.out.println(content);
		Document html = Jsoup.parse(content);
		//get the navigation list
		Element navEle = html.getElementById("refinements");
		//get the menu items
		Element ul = navEle.getElementsByTag("ul").get(0);
		
		///returned child list
		List<ItemCategory> subCatList = new LinkedList<ItemCategory>();

		Elements subCats = ul.getElementsByTag("li");
		boolean isParentNode = true;
		
		for(int i = 0; i < subCats.size(); i++)
		{
			Element tmpSubCat = subCats.get(i);
			///try to get the url 
			Elements subCatLink = tmpSubCat.getElementsByTag("a");
			if(subCatLink.isEmpty())
			{
				isParentNode = false;
				continue;
			}
			else
			{
				if(isParentNode)
					continue;
				//extract the link, name, id for current sub category
				ItemCategory subCat = new ItemCategory();
				Element linkEle = subCatLink.get(0);
				String href = linkEle.attr("href");
				Pattern pat1 = Pattern.compile("rh=n%3A(\\d+)");
//				System.out.println(href);
				Matcher mat1 = pat1.matcher(href);
				String id = null;
				if(mat1.find())
				{
					id = mat1.group(1);
				}
				///extract category name
				Element nameEle = linkEle.getElementsByTag("span").get(0);
				String name = nameEle.text();
				subCat.addColumn(ItemCategory.NAME, MyBytes.toBytes(name));
				subCat.addColumn(ItemCategory.ID,MyBytes.toBytes(id));
				subCat.addColumn(ItemCategory.URL,MyBytes.toBytes(href));
				///none leaf node
				subCat.addColumn(ItemCategory.IS_LEAF,MyBytes.toBytes(false));
				///add reference to parent node
				subCat.addColumn(ItemCategory.MERCHANT, MyBytes.toBytes(ItemCategory.AMAZON));
				subCat.addColumn(ItemCategory.PARENT_CATEGORY_ID, parentIdByte);
				String subPathToRoot = parentPath + "|" + parentId + "_" + parentName;
				subCat.addColumn(ItemCategory.PATH_TO_ROOT, MyBytes.toBytes(subPathToRoot));
				///append current sub category as child to parent category node
				parentCat.addColumn(ItemCategory.CHILD_NODE, MyBytes.toBytes(id));
				subCatList.add(subCat);
			}
		}
		return subCatList;
		
	}
	
	public void Crawl()
	{
		try {
			
			List<ItemCategory> subCatList = parseCategory(rootCat);
			for(ItemCategory cat : subCatList)
			{
				System.out.println(cat);
			}
			System.out.println(rootCat);
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
