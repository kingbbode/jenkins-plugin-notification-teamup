package org.jenkinsci.plugins.teamup.enums;

/**
 * Created by YG on 2017-01-18.
 */
public enum Level {
    GOOD("정상"),
    DANGER("위험"),
    WARN("경고");

    private String message;

    Level(String message) {
        this.message = message;
    }

    public String getMessage() {
        return "[" + message + "]\n";
    }
}
