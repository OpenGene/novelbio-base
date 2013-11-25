package com.novelbio.base;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class PageModel {
	/** 当前页 */
	private int page = 1;
	/** 每页条数 */
	private int rows = 20;
	/** 按什么属性排序 */
	private String sort;
	/** 排序方式 */
	private String order = Sort.Direction.ASC.toString();
	
	public int getPage() {
		return page;
	}
	/** 默认为1 */
	public void setPage(int page) {
		this.page = page;
	}
	public void addPage() {
		page++;
	}
	/** 每条页数，默认为20 */
	public int getRows() {
		return rows;
	}
	/** 每条页数，默认为20 */
	public void setRows(int rows) {
		this.rows = rows;
	}
	/** 按照什么属性排序，默认不排序 */
	public String getSort() {
		return sort;
	}
	/**
	 * 输入{@linkplain Sort.Direction#ASC}等
	 * @param sort
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getOrder() {
		return order;
	}
	/**
	 * @param order sort.desc
	 */
	public void setOrder(String order) {
		this.order = order;
	}
	
	/**
	 * 生成一个Pageable对象供mongoDB的分页查询使用
	 */
	public Pageable bePageable() {
		Pageable pageable = null;
		if (sort != null && order != null) {
			pageable = new PageRequest(page - 1, rows, Direction.fromString(this.order), this.sort);
		} else {
			pageable = new PageRequest(page - 1, rows);
		}
		return pageable;
	}

}
