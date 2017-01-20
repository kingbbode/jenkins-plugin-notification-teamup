package org.jenkinsci.plugins.teamup;

public enum CommitInfoChoice {
    NONE("커밋 정보를 표기하지 않습니다.",                             false, false),
    AUTHORS("커밋 사용자를 표기합니다.",                  true,  false),
    AUTHORS_AND_TITLES("커밋 메시지와 사용자를 표기합니다.", true,  true);

    private final String displayName;
    private boolean showAuthor;
    private boolean showTitle;

    private CommitInfoChoice(String displayName, boolean showAuthor, boolean showTitle) {
        this.displayName = displayName;
        this.showAuthor = showAuthor;
        this.showTitle = showTitle;
    }

    public boolean showAuthor() {
        return this.showAuthor;
    }
    public boolean showTitle() {
        return this.showTitle;
    }
    public boolean showAnything() {
        return showAuthor() || showTitle();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static CommitInfoChoice forDisplayName(String displayName) {
        for (CommitInfoChoice commitInfoChoice : values()) {
            if (commitInfoChoice.getDisplayName().equals(displayName)) {
                return commitInfoChoice;
            }
        }
        return null;
    }
}
