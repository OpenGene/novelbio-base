package com.novelbio.base.dataStructure;

import java.util.List;

/**
 * 前端tree类型组建数据类<br>
 * 
 * @author novelbio liqi
 * @date 2019年1月14日 上午10:58:30
 */
public class TreeNodeView {
	/** 节点id */
	private String id;
	/** 节点title */
	private String title;
	/** 父id */
	private String pid;
	/** 从根到当前节点路径的id列表 */
	private List<String> lsPath;
	/** 子节点 */
	private List<TreeNodeView> children;

	/**
	 * 仅供aop，json序列号等代码调用，开发者调用请使用带参数的构造方法，避免必填字段为空
	 */
	public TreeNodeView() {
	}

	/**
	 * 初始化对象
	 * 
	 * @param id
	 *            id
	 * @param pid
	 *            父id
	 * @param title
	 *            值
	 */
	public TreeNodeView(String id, String pid, String title) {
		this.id = id;
		this.pid = pid;
		this.setTitle(title);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public List<String> getLsPath() {
		return lsPath;
	}

	public void setLsPath(List<String> lsPath) {
		this.lsPath = lsPath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<TreeNodeView> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNodeView> children) {
		this.children = children;
	}

}
