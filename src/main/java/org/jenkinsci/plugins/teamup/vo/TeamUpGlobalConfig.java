package org.jenkinsci.plugins.teamup.vo;

import hudson.EnvVars;
import org.jenkinsci.plugins.teamup.CommitInfoChoice;
import org.jenkinsci.plugins.teamup.TeamUpNotifier;

/**
 * Created by YG on 2017-01-17.
 */
public class TeamUpGlobalConfig {
    private String clientId;
    private String clientSecret;
    private String userId;
    private String userPassword;

    public TeamUpGlobalConfig(String clientId, String clientSecret, String userId, String userPassword) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userId = userId;
        this.userPassword = userPassword;
    }
    
    public void expand(EnvVars envVars){
        this.clientId = envVars.expand(this.clientId);
        this.clientSecret = envVars.expand(this.clientSecret);
        this.userId = envVars.expand(this.userId);
        this.userPassword = envVars.expand(this.userPassword);   
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserPassword() {
        return userPassword;
    }
    
    public TeamUpGlobalConfig newInstanceForEnvExpand(EnvVars envVars){
        return new TeamUpGlobalConfig(envVars.expand(this.clientId), envVars.expand(this.clientSecret), envVars.expand(this.userId), envVars.expand(this.userPassword));
    }
}
