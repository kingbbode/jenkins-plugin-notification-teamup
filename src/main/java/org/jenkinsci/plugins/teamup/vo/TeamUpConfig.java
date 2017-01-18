package org.jenkinsci.plugins.teamup.vo;

import org.jenkinsci.plugins.teamup.CommitInfoChoice;

/**
 * Created by YG on 2017-01-17.
 */
public class TeamUpConfig {
    private String room;
    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;
    private boolean includeTestSummary;
    private CommitInfoChoice commitInfoChoice;
    private boolean includeCustomMessage;
    private String customMessage;

    public TeamUpConfig(String room) {
        this.room = room;
    }


    public TeamUpConfig(String room, boolean startNotification, boolean notifySuccess, boolean notifyAborted, boolean notifyNotBuilt, boolean notifyUnstable, boolean notifyFailure, boolean notifyBackToNormal, boolean notifyRepeatedFailure, boolean includeTestSummary, CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage) {
        this.room = room;
        this.startNotification = startNotification;
        this.notifySuccess = notifySuccess;
        this.notifyAborted = notifyAborted;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifyUnstable = notifyUnstable;
        this.notifyFailure = notifyFailure;
        this.notifyBackToNormal = notifyBackToNormal;
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        this.includeTestSummary = includeTestSummary;
        this.commitInfoChoice = commitInfoChoice;
        this.includeCustomMessage = includeCustomMessage;
        this.customMessage = customMessage;
    }

    public String getRoom() {
        return room;
    }

    public boolean isStartNotification() {
        return startNotification;
    }

    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public boolean isNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    public boolean isIncludeTestSummary() {
        return includeTestSummary;
    }

    public CommitInfoChoice getCommitInfoChoice() {
        return commitInfoChoice;
    }

    public boolean isIncludeCustomMessage() {
        return includeCustomMessage;
    }

    public String getCustomMessage() {
        return customMessage;
    }
}
