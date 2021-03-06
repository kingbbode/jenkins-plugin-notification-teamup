package org.jenkinsci.plugins.teamup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.triggers.SCMTrigger;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.teamup.enums.Level;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

@SuppressFBWarnings({"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "DLS_DEAD_LOCAL_STORE"})
@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {

    private static final Logger logger = Logger.getLogger(TeamUpListener.class.getName());

    TeamUpNotifier notifier;
    BuildListener listener;

    public ActiveNotifier(TeamUpNotifier notifier, BuildListener listener) {
        super();
        this.notifier = notifier;
        this.listener = listener;
    }

    private TeamUpService getTeamUpService(AbstractBuild r) {
        return notifier.getTeamUpService(r, listener);
    }

    public void deleted(AbstractBuild r) {
    }

    public void started(AbstractBuild build) {
        CauseAction causeAction = build.getAction(CauseAction.class);
        if (causeAction != null) {
            Cause scmCause = causeAction.findCause(SCMTrigger.SCMTriggerCause.class);
            if (scmCause == null) {
                MessageBuilder message = new MessageBuilder(notifier, build);
                for(Cause cause : causeAction.getCauses()){
                    message.append(cause.getShortDescription() + ". ");
                }
                notifyStart(build, message.appendOpenLink().toString());
                // Cause was found, exit early to prevent double-message
                return;
            }
        }

        String message = getChanges(build, notifier.getConfig().isIncludeCustomMessage());
        if(message == null){
            message = getBuildStatusMessage(build, false, notifier.getConfig().isIncludeCustomMessage());
        }
        notifyStart(build, message);        
    }

    private void notifyStart(AbstractBuild build, String message) {
        EnvVars envVars = new EnvVars();
        try {
            envVars = build.getEnvironment(new LogTaskListener(logger, INFO));
        } catch (IOException e) {
            logger.log(SEVERE, e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.log(SEVERE, e.getMessage(), e);
        }
        AbstractProject<?, ?> project = build.getProject();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild().getPreviousCompletedBuild();
        Level level = null;
        if (previousBuild != null) {
            level = getBuildLevel(previousBuild).getBeforeLevel();
        }
        
        getTeamUpService(build).send(envVars.expand(notifier.getConfig().getRoom()), message, level);       
    }

    public void finalized(AbstractBuild r) {
    }

    public void completed(AbstractBuild r) {
        EnvVars envVars = new EnvVars();
        try {
            envVars = r.getEnvironment(new LogTaskListener(logger, INFO));
        } catch (IOException e) {
            logger.log(SEVERE, e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.log(SEVERE, e.getMessage(), e);
        }
        AbstractProject<?, ?> project = r.getProject();
        Result result = r.getResult();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild();
        do {
            previousBuild = previousBuild.getPreviousCompletedBuild();
        } while (previousBuild != null && previousBuild.getResult() == Result.ABORTED);
        Result previousResult = (previousBuild != null) ? previousBuild.getResult() : Result.SUCCESS;
        if ((result == Result.ABORTED && notifier.getConfig().isNotifyAborted())
                || (result == Result.FAILURE //notify only on single failed build
                    && previousResult != Result.FAILURE
                    && notifier.getConfig().isNotifyFailure())
                || (result == Result.FAILURE //notify only on repeated failures
                    && previousResult == Result.FAILURE
                    && notifier.getConfig().isNotifyRepeatedFailure())
                || (result == Result.NOT_BUILT && notifier.getConfig().isNotifyNotBuilt())
                || (result == Result.SUCCESS
                    && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)
                    && notifier.getConfig().isNotifyBackToNormal())
                || (result == Result.SUCCESS && notifier.getConfig().isNotifySuccess())
                || (result == Result.UNSTABLE && notifier.getConfig().isNotifyUnstable())) {
            getTeamUpService(r).send(envVars.expand(notifier.getConfig().getRoom()), getBuildStatusMessage(r, notifier.getConfig().isIncludeTestSummary(),
                    notifier.getConfig().isIncludeCustomMessage()), getBuildLevel(r));
            if (notifier.getConfig().getCommitInfoChoice().showAnything()) {
                getTeamUpService(r).send(envVars.expand(notifier.getConfig().getRoom()), getCommitList(r), getBuildLevel(r));
            }
        }
    }

    String getChanges(AbstractBuild r, boolean includeCustomMessage) {
        if (!r.hasChangeSetComputed()) {
            logger.info("No change set computed...");
            return null;
        }
        ChangeLogSet changeSet = r.getChangeSet();
        List<Entry> entries = new LinkedList<Entry>();
        Set<AffectedFile> files = new HashSet<AffectedFile>();
        for (Object o : changeSet.getItems()) {
            Entry entry = (Entry) o;
            logger.info("Entry " + o);
            entries.add(entry);
            files.addAll(entry.getAffectedFiles());
        }
        if (entries.isEmpty()) {
            logger.info("Empty change...");
            return null;
        }
        Set<String> authors = new HashSet<String>();
        for (Entry entry : entries) {
            authors.add(entry.getAuthor().getDisplayName());
        }
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.append("\n");
        message.append(StringUtils.join(authors, ", "));
        message.append("님의 변동사항이 빌드 시작되었습니다.");
        message.append("\n(");
        message.append(files.size());
        message.append(" 개의 파일 수정)");
        message.append("\n");
        message.appendOpenLink();
        if (includeCustomMessage) {
            message.append("\n");
            message.appendCustomMessage();
        }
        return message.toString();
    }

    String getCommitList(AbstractBuild r) {
        ChangeLogSet changeSet = r.getChangeSet();
        List<Entry> entries = new LinkedList<Entry>();
        for (Object o : changeSet.getItems()) {
            Entry entry = (Entry) o;
            logger.info("Entry " + o);
            entries.add(entry);
        }
        if (entries.isEmpty()) {
            logger.info("Empty change...");
            Cause.UpstreamCause c = (Cause.UpstreamCause)r.getCause(Cause.UpstreamCause.class);
            if (c == null) {
                return "변경내역이 없습니다.";
            }
            String upProjectName = c.getUpstreamProject();
            int buildNumber = c.getUpstreamBuild();
            AbstractProject project = Jenkins.getInstance().getItemByFullName(upProjectName, AbstractProject.class);
            AbstractBuild upBuild = project.getBuildByNumber(buildNumber);
            return getCommitList(upBuild);
        }
        Set<String> commits = new HashSet<String>();
        for (Entry entry : entries) {
            StringBuffer commit = new StringBuffer();
            CommitInfoChoice commitInfoChoice = notifier.getConfig().getCommitInfoChoice();
            if (commitInfoChoice.showTitle()) {
                commit.append(entry.getMsg());
            }
            if (commitInfoChoice.showAuthor()) {
                commit.append(" [").append(entry.getAuthor().getDisplayName()).append("]");
            }
            commits.add(commit.toString());
        }
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.append("\n커밋 정보\n- ");
        message.append(StringUtils.join(commits, "\n- "));
        return message.toString();
    }

    static Level getBuildLevel(AbstractBuild r) {
        Result result = r.getResult();
        if (result == Result.SUCCESS) {
            return Level.GOOD;
        } else if (result == Result.FAILURE) {
            return Level.DANGER;
        } else {
            return Level.WARN;
        }
    }

    String getBuildStatusMessage(AbstractBuild r, boolean includeTestSummary, boolean includeCustomMessage) {
        MessageBuilder message = new MessageBuilder(notifier, r);
        message.append("\n");
        message.appendStatusMessage();
        message.append("\n");
        message.appendDuration();
        message.append("\n");
        message.appendOpenLink();
        if (includeTestSummary) {
            message.append("\n");
            message.appendTestSummary();
        }
        if (includeCustomMessage) {
            message.append("\n");
            message.appendCustomMessage();
        }
        return message.toString();
    }

    public static class MessageBuilder {

        private static final Pattern aTag = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
        private static final Pattern href = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
        private static final String STARTING_STATUS_MESSAGE = "빌드가 시작되었습니다.",
                                    BACK_TO_NORMAL_STATUS_MESSAGE = "이전에 실패했던 빌드가 정상적으로 빌드 되었습니다.",
                                    STILL_FAILING_STATUS_MESSAGE = "여전히 빌드가 실패하고 있습니다.",
                                    SUCCESS_STATUS_MESSAGE = "빌드가 성공했습니다.",
                                    FAILURE_STATUS_MESSAGE = "빌드가 실패했습니다.",
                                    ABORTED_STATUS_MESSAGE = "Aborted",
                                    NOT_BUILT_STATUS_MESSAGE = "Not built",
                                    UNSTABLE_STATUS_MESSAGE = "Unstable",
                                    UNKNOWN_STATUS_MESSAGE = "Unknown";
        
        private StringBuffer message;
        private TeamUpNotifier notifier;
        private AbstractBuild build;

        public MessageBuilder(TeamUpNotifier notifier, AbstractBuild build) {
            this.notifier = notifier;
            this.message = new StringBuffer();
            this.build = build;
            startMessage();
        }

        public MessageBuilder appendStatusMessage() {
            message.append(this.escape(getStatusMessage(build)));
            return this;
        }

        static String getStatusMessage(AbstractBuild r) {
            if (r.isBuilding()) {
                return STARTING_STATUS_MESSAGE;
            }
            Result result = r.getResult();
            Result previousResult;
            Run previousBuild = r.getProject().getLastBuild().getPreviousBuild();
            Run previousSuccessfulBuild = r.getPreviousSuccessfulBuild();
            boolean buildHasSucceededBefore = previousSuccessfulBuild != null;
            
            /*
             * If the last build was aborted, go back to find the last non-aborted build.
             * This is so that aborted builds do not affect build transitions.
             * I.e. if build 1 was failure, build 2 was aborted and build 3 was a success the transition
             * should be failure -> success (and therefore back to normal) not aborted -> success. 
             */
            Run lastNonAbortedBuild = previousBuild;
            while(lastNonAbortedBuild != null && lastNonAbortedBuild.getResult() == Result.ABORTED) {
                lastNonAbortedBuild = lastNonAbortedBuild.getPreviousBuild();
            }
            
            
            /* If all previous builds have been aborted, then use 
             * SUCCESS as a default status so an aborted message is sent
             */
            if(lastNonAbortedBuild == null) {
                previousResult = Result.SUCCESS;
            } else {
                previousResult = lastNonAbortedBuild.getResult();
            }
            
            /* Back to normal should only be shown if the build has actually succeeded at some point.
             * Also, if a build was previously unstable and has now succeeded the status should be 
             * "Back to normal"
             */
            if (result == Result.SUCCESS
                    && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE) 
                    && buildHasSucceededBefore) {
                return BACK_TO_NORMAL_STATUS_MESSAGE;
            }
            if (result == Result.FAILURE && previousResult == Result.FAILURE) {
                return STILL_FAILING_STATUS_MESSAGE;
            }
            if (result == Result.SUCCESS) {
                return SUCCESS_STATUS_MESSAGE;
            }
            if (result == Result.FAILURE) {
                return FAILURE_STATUS_MESSAGE;
            }
            if (result == Result.ABORTED) {
                return ABORTED_STATUS_MESSAGE;
            }
            if (result == Result.NOT_BUILT) {
                return NOT_BUILT_STATUS_MESSAGE;
            }
            if (result == Result.UNSTABLE) {
                return UNSTABLE_STATUS_MESSAGE;
            }
            return UNKNOWN_STATUS_MESSAGE;
        }

        public MessageBuilder append(String string) {
            message.append(this.escape(string));
            return this;
        }

        public MessageBuilder append(Object string) {
            message.append(this.escape(string.toString()));
            return this;
        }

        private MessageBuilder startMessage() {
            message.append(this.escape(build.getProject().getFullDisplayName()));
            message.append(" - ");
            message.append(this.escape(build.getDisplayName()));
            message.append(" ");
            return this;
        }

        public MessageBuilder appendOpenLink() {
            String url = DisplayURLProvider.get().getRunURL(build);
            message.append("(").append(url).append(")");
            return this;
        }

        public MessageBuilder appendDuration() {
            message.append("(소요시간 : ");
            String durationString;
            if(message.toString().contains(BACK_TO_NORMAL_STATUS_MESSAGE)){
                durationString = createBackToNormalDurationString();
            } else {
                durationString = build.getDurationString();
            }
            message.append(durationString);
            message.append(" )");
            return this;
        }

        public MessageBuilder appendTestSummary() {
            AbstractTestResultAction<?> action = this.build
                    .getAction(AbstractTestResultAction.class);
            if (action != null) {
                int total = action.getTotalCount();
                int failed = action.getFailCount();
                int skipped = action.getSkipCount();
                message.append("\n테스트 결과\n");
                message.append("\nPassed: " + (total - failed - skipped));
                message.append("\nFailed: " + failed);
                message.append("\nSkipped: " + skipped);
            } else {
                message.append("\n테스트가 없습니다.");
            }
            return this;
        }

        public MessageBuilder appendCustomMessage() {
            String customMessage = notifier.getConfig().getCustomMessage();
            EnvVars envVars = new EnvVars();
            try {
                envVars = build.getEnvironment(new LogTaskListener(logger, INFO));
            } catch (IOException e) {
                logger.log(SEVERE, e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.log(SEVERE, e.getMessage(), e);
            }
            message.append("\n");
            message.append(envVars.expand(customMessage));
            return this;
        }
        
        private String createBackToNormalDurationString(){
            Run previousSuccessfulBuild = build.getPreviousSuccessfulBuild();
            long previousSuccessStartTime = previousSuccessfulBuild.getStartTimeInMillis();
            long previousSuccessDuration = previousSuccessfulBuild.getDuration();
            long previousSuccessEndTime = previousSuccessStartTime + previousSuccessDuration;
            long buildStartTime = build.getStartTimeInMillis();
            long buildDuration = build.getDuration();
            long buildEndTime = buildStartTime + buildDuration;
            long backToNormalDuration = buildEndTime - previousSuccessEndTime;
            return Util.getTimeSpanString(backToNormalDuration);
        }

        private String escapeCharacters(String string) {
            string = string.replace("&", "&amp;");
            string = string.replace("<", "&lt;");
            string = string.replace(">", "&gt;");

            return string;
        }

        private String[] extractReplaceLinks(Matcher aTag, StringBuffer sb) {
            int size = 0;
            List<String> links = new ArrayList<String>();
            while (aTag.find()) {
                Matcher url = href.matcher(aTag.group(1));
                if (url.find()) {
                    aTag.appendReplacement(sb,String.format("{%s}", size++));
                    links.add(String.format("<%s|%s>", url.group(1).replaceAll("\"", ""), aTag.group(2)));
                }
            }
            aTag.appendTail(sb);
            return links.toArray(new String[size]);
        }

        public String escape(String string) {
            StringBuffer pattern = new StringBuffer();
            String[] links = extractReplaceLinks(aTag.matcher(string), pattern);
            return MessageFormat.format(escapeCharacters(pattern.toString()), links);
        }

        public String toString() {
            return message.toString();
        }
    }
}
