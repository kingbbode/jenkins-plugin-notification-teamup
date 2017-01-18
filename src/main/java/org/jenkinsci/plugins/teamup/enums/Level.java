package org.jenkinsci.plugins.teamup.enums;

/**
 * Created by YG on 2017-01-18.
 */
public enum Level {
    GOOD("정상"),
    DANGER("위험"),
    WARN("경고");

    private String level;

    Level(String level) {
        this.level = level;
    }
}
