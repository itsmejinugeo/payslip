package com.costco.eeterm.successfactors.pojo;

/**
 * This POJO is for Attachment
 */
public class Attachment{

	private String attachmentId;
	private String uri;
	private String fileSize;
	private String fileExtension;
	private byte[] fileContent;
	private String filename;
	private String mimeType;
	
	public String getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	public String getFileExtension() {
		return fileExtension;
	}
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public byte[] getFileContent() {
		byte[] ret = new byte[0];
		if(fileContent == null){
			return ret;
		}
		ret = new byte[fileContent.length];
		System.arraycopy(fileContent, 0, ret, 0, ret.length);
		return ret;
	}
	
	public void setFileContent(byte[] fileContent) {
		byte[] ret = new byte[fileContent.length];
		System.arraycopy(fileContent, 0, ret, 0, ret.length);
		this.fileContent = ret;
	}



  
	
}
