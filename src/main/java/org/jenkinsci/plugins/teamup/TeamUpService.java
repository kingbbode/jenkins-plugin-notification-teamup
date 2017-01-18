package org.jenkinsci.plugins.teamup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hudson.EnvVars;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.teamup.enums.Level;
import org.jenkinsci.plugins.teamup.vo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by YG on 2017-01-17.
 */
public class TeamUpService {
    static final String GRANT_REFRESH = "refresh_token";
    static final String GRANT_PASSWORD = "password";
    static final String MESSAGE_URL = "https://edge.tmup.com/v3/message/";
    static final String OAUTH2_URL = "https://auth.tmup.com/oauth2/token";

    private OAuth2Token oauth2Token;
    private TeamUpGlobalConfig teamUpGlobalConfig;
    private boolean isEnvExpand;

    public TeamUpService(TeamUpGlobalConfig teamUpGlobalConfig) {
        this.teamUpGlobalConfig = teamUpGlobalConfig;
        this.oauth2Token = getOauth2Token();
    }

    public TeamUpService getInstance(EnvVars envVars) {
        if (!isEnvExpand && envVars != null) {
            this.teamUpGlobalConfig.expand(envVars);
            isEnvExpand = true;
        }
        return this;
    }


    public boolean send(String room, String contents, Level level) {
        try {
            Message message = new Message(level.getMessage() + contents);
            String param = new Gson().toJson(message);
            HttpPost post = new HttpPost(MESSAGE_URL + room);
            String accessToken = getAccessToken();
            if (accessToken == null) {
                return false;
            }
            post.addHeader("Authorization", accessToken);
            post.addHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(param));

            CloseableHttpClient client = HttpClientBuilder.create().build();

            // send teamup message
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return false;
            }
        } catch (RuntimeException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private String getAccessToken() {
        oauth2Token = getOauth2Token();
        return oauth2Token != null ? oauth2Token.getAccessToken() : null;
    }

    private OAuth2Token getOauth2Token() {

        OAuth2Token resultToken = null;
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            if (oauth2Token != null) {
                if (oauth2Token.isExpired()) {
                    HttpGet get = new HttpGet(OAUTH2_URL + "?grant_type=" + GRANT_REFRESH + "&refresh_token=" + oauth2Token.getRefreshToken());
                    HttpResponse response = client.execute(get);
                    resultToken = new Gson().fromJson(EntityUtils.toString(response.getEntity()), OAuth2Token.class);
                } else {
                    resultToken = oauth2Token;
                }
            } else {
                TeamUpAuth auth = new TeamUpAuth(GRANT_PASSWORD, teamUpGlobalConfig);
                HttpPost post = new HttpPost(OAUTH2_URL);
                post.addHeader("Content-Type", "application/x-www-form-urlencoded");

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                JsonElement elm = new Gson().toJsonTree(auth);
                JsonObject jsonObj = elm.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().getAsString()));
                }
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = client.execute(post);
                resultToken = new Gson().fromJson(EntityUtils.toString(response.getEntity()), OAuth2Token.class);
            }
        } catch (RuntimeException e) {
            
        } catch (Exception e) {

        }

        return resultToken;
    }
}
