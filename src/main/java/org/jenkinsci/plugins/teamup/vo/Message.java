package org.jenkinsci.plugins.teamup.vo;

public class Message {
	public Message(String content){
		this.content = content;
	}
	
	
	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}	
}
