package org.jenkinsci.plugins.teamup.enums;

/**
 * Created by YG on 2017-01-18.
 */
public enum Level {
    GOOD("정상"),
    DANGER("위험"),
    WARN("경고"),
    BEFORE_GOOD("마지막 빌드 : 정상"),
    BEFORE_DANGER("마지막 빌드 : 위험"),
    BEFORE_WARN("마지막 빌드 : 경고");

    private String message;

    Level(String message) {
        this.message = message;
    }
    
    

    public String getMessage() {
        return "[" + message + "] ";
    }
    
    public Level getBeforeLevel(){
        if(this == GOOD){
            return BEFORE_GOOD;
        }else if(this == DANGER){
            return BEFORE_DANGER;
        }else{
            return BEFORE_WARN;
        }
    }
}
