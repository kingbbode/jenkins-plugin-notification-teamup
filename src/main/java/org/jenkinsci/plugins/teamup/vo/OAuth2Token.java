package org.jenkinsci.plugins.teamup.vo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;
/**
 * Created by YG on 2017-01-18.
 */
@SuppressFBWarnings("ICAST_INT_2_LONG_AS_INSTANT")
public class OAuth2Token {
	
	public OAuth2Token(String access_token, long expires_in, String token_type, String refresh_token){
		this.access_token = access_token;
		this.expires_in = expires_in;
		this.token_type = token_type;
		this.refresh_token = refresh_token;
	}
	
	private String access_token;
	
	private long expires_in;
	
	private String token_type;
	
	private String refresh_token;
	
	
	
	public String getAccessToken() {
		return token_type + " " + access_token;
	}

	public String getRefreshToken() {
		return refresh_token;
	}

	public boolean isExpired(){
		return new Date(this.expires_in).before(new Date());
	}
	
}
