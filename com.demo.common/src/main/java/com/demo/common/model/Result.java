package com.demo.common.model;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * 接口返回的结果.
 */
public class Result implements Serializable {

	/**
	 * 错误代码.
	 */
	private int code;
	/**
	 * 错误描述.
	 */
	private String desc;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
