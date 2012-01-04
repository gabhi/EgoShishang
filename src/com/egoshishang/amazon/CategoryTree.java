package com.egoshishang.amazon;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CategoryTree {
	public  static final long BOOKNODE_ID = 658390051;
	protected CategoryNode rootNode = new CategoryNode();
	Map<Long,Boolean> visitedNode = new HashMap<Long,Boolean>();
	public static class CategoryNode
	{
		public long nodeId;
		public String nodeName;
		public boolean isCategoryRoot = false;
		public boolean isLeaf = false;
		public List<CategoryNode> children = new LinkedList<CategoryNode>();
		@Override
		public String toString()
		{
			return "" + nodeId + "\t" + nodeName + "\t" + isCategoryRoot + "\t" + isLeaf;
		}
	}
	
	public CategoryTree(long rootNodeId)
	{
		rootNode.nodeId = rootNodeId;
	}
	
	public void recursiveQuery(CategoryNode curNode)
	{
		if(!visitedNode.containsKey(curNode.nodeId))
		{
			querySingleNode(curNode);
			visitedNode.put(curNode.nodeId, true);
			for(CategoryNode child: curNode.children)
			{
				recursiveQuery(child);
			}			
		}
	}
	public void build()
	{
		
		recursiveQuery(this.rootNode);
	}
	
	public static String joinListElement(List<Long> path)
	{
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < path.size(); i++)
		{
			sb.append(i == 0 ? "" : "|");
			sb.append(path.get(i));
		}
		return sb.toString();
	}
	private void dumpLevel(BufferedWriter bw, CategoryNode node, Long parentId, List<Long> path)
	{
		try {
			bw.append(node.nodeId + "\t" + node.nodeName + "\t" + parentId + "\t" +  node.isCategoryRoot + "\t" + node.isLeaf + "\t" + 
					joinListElement(path));
			bw.append("\n");
			//handle each child nodes
			for(CategoryNode child: node.children)
			{
				path.add(node.nodeId);
				dumpLevel(bw, child, node.nodeId, path);
				path.remove(path.size() -1 );
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void dumpTree(String fileName)
	{
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			List<Long> path = new LinkedList<Long>();
			dumpLevel(bw, this.rootNode, (long) -1, path);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public CategoryNode querySingleNode(CategoryNode curNode)
	{
		
		//using Amazon api to do this
        //now parse the document
		Document responseDoc = getResponse(curNode.nodeId);
		responseDoc.normalize();
		Node browseNode = responseDoc.getElementsByTagName("BrowseNode").item(0);
		NodeList browseChild = browseNode.getChildNodes();
		for(int i = 0; i < browseChild.getLength(); i++)
		{
			Node tmpChild = browseChild.item(i);
			if(tmpChild.getNodeName().equals("Name"))
			{
				//this is the name of the query node
//				System.out.println("browse node name:" + tmpChild.getTextContent());
				curNode.nodeName = tmpChild.getTextContent();
			}
			if(tmpChild.getNodeName().equals("Children"))
			{
				NodeList children = tmpChild.getChildNodes();
				for(int j = 0; j < children.getLength(); j++)
				{
					Node singleChild = children.item(j);
					CategoryNode addChild = new CategoryNode();
					for(int k = 0; k < singleChild.getChildNodes().getLength(); k++)
					{
						Node attrNode = singleChild.getChildNodes().item(k);
						String nodeName = attrNode.getNodeName();
						if(nodeName.equals("BrowseNodeId"))
						{
							addChild.nodeId = Long.valueOf(attrNode.getTextContent());
						}
						if(nodeName.equals("Name"))
						{
							addChild.nodeName = attrNode.getTextContent();
						}
						if(nodeName.equals("IsCategoryRoot"))
						{
							addChild.isCategoryRoot = true;
						}
					}
					curNode.children.add(addChild);
				}
			}
		}
		if(curNode.children.size() == 0)
		{
			curNode.isLeaf = true;
		}
		return curNode;
	}

	public Document getResponse(long curNode)
	{
		SignedRequestsHelper helper = AmazonPPA.getRequesetHelper();
        Map<String, String> params = new HashMap<String, String>();
        AmazonPPA.setCommonParams(params);
        AmazonPPA.setBrowseNodeParams(params, curNode, "BrowseNodeInfo");
        Document responseDoc = null;
		try {
			responseDoc = AmazonPPA.retrieveDocument(helper, params);
			//be nice for the server
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return responseDoc;
	}
}
