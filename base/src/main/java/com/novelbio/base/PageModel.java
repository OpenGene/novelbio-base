package com.novelbio.base;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

public class PageModel {
	/** 当前页 */
	private int page;
	/** 每页条数 */
	private int rows;
	/** 按什么属性排序 */
	private String sort;
	/** 排序方式 */
	private String order;
	public int getPage() {
		return page;
		
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	
	/**
	 * 生成一个Pageable对象供mongoDB的分页查询使用
	 */
	public Pageable bePageable() {
		Pageable pageable = new PageRequest(this.page-1, this.rows, Direction.fromString(this.order), this.sort);
		return pageable;
	}

}
