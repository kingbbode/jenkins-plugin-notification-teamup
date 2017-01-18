package org.jenkinsci.plugins.teamup.vo;
/**
 * Created by YG on 2017-01-18.
 */
public class TeamUpAuth {
	
	public TeamUpAuth(String grant_type, TeamUpGlobalConfig teamUpGlobalConfig){
		this.grant_type = grant_type;
		this.client_id = teamUpGlobalConfig.getClientId();
		this.client_secret = teamUpGlobalConfig.getClientSecret();
		this.username = teamUpGlobalConfig.getUserId();
		this.password = teamUpGlobalConfig.getUserPassword();
	}
	
	private String grant_type;
	private String client_id;
	private String client_secret;
	private String username;
	private String password;
	
	
	public String getGrant_type() {
		return grant_type;
	}
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getClient_secret() {
		return client_secret;
	}
	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}	
}
