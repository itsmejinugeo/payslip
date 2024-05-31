package com.costco.eeterm.odata.odatav2.pojo;

/**
 * This POJO is for ODATA POST error
 */
public class PostErrorDetail {

	private String code;
	private PostErrorMessage message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public PostErrorMessage getMessage() {
		return message;
	}

	public void setMessage(PostErrorMessage message) {
		this.message = message;
	}

}
