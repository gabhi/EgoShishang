package com.egoshishang.amazon;


import org.apache.log4j.*;

import junit.framework.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.egoshishang.amazon.CategoryTree.CategoryNode;

public class CategoryTreeTest {
	
	protected CategoryTree treeBuilder = null;
	private static Logger logger = Logger.getLogger(CategoryTreeTest.class);
	@Before
	public void createTree()
	{
		treeBuilder = new CategoryTree(CategoryTree.BOOKNODE_ID);
		logger.setLevel(Level.DEBUG);
	}
//	@Test
	public void testResponse()
	{
		CategoryNode rootNode = new CategoryNode();
		rootNode.nodeId = CategoryTree.BOOKNODE_ID;
		treeBuilder.querySingleNode(rootNode);
		System.out.println(rootNode);
		System.out.println("child nodes:");
		for(CategoryNode child: rootNode.children)
		{
			System.out.println(child);
		}
	}
	@Test
	public void testBuild()
	{
		String fileName = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/amazon_category_tree";
		treeBuilder.build();
		treeBuilder.dumpTree(fileName);
	}
}
