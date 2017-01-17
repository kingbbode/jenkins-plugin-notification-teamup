package org.jenkinsci.plugins.teamup;

public interface SlackService {
    boolean publish(String message);

    boolean publish(String message, String color);
}
