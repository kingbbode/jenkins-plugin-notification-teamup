package org.jenkinsci.plugins.teamup.vo;
/**
 * Created by YG on 2017-01-18.
 */
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
