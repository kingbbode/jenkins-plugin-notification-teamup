package org.jenkinsci.plugins.teamup;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.teamup.enums.Level;
import org.jenkinsci.plugins.teamup.vo.TeamUpConfig;
import org.jenkinsci.plugins.teamup.vo.TeamUpGlobalConfig;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

/**
 * Created by YG on 2017-01-18.
 */

public class TeamUpNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(TeamUpNotifier.class.getName());

    private TeamUpConfig teamUpConfig;

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @DataBoundConstructor
    public TeamUpNotifier(final String room,
                          final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                          final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyBackToNormal,
                          final boolean notifyRepeatedFailure, final boolean includeTestSummary, CommitInfoChoice commitInfoChoice,
                          boolean includeCustomMessage, String customMessage) {
        super();
        this.teamUpConfig = new TeamUpConfig(room, startNotification, notifySuccess, notifyAborted, notifyNotBuilt, notifyUnstable, notifyFailure, notifyBackToNormal, notifyRepeatedFailure, includeTestSummary, commitInfoChoice, includeCustomMessage, customMessage);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public TeamUpService getTeamUpService(AbstractBuild r, BuildListener listener) {
        EnvVars env = null;
        try {
            env = r.getEnvironment(listener);
        } catch (Exception e) {
            listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
            env = new EnvVars();
        }

        return getDescriptor().teamUpService.getInstance(env);
    }
    
    public TeamUpConfig getConfig(){
        return this.teamUpConfig;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        if (teamUpConfig.isStartNotification()) {
            Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
            for (Publisher publisher : map.values()) {
                if (publisher instanceof TeamUpNotifier) {
                    logger.info("Invoking Started...");
                    new ActiveNotifier((TeamUpNotifier) publisher, listener).started(build);
                }
            }
        }
        return super.prebuild(build, listener);
    }


    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private TeamUpService teamUpService;
        private TeamUpGlobalConfig teamUpGlobalConfig;

        public static final CommitInfoChoice[] COMMIT_INFO_CHOICES = CommitInfoChoice.values();

        public DescriptorImpl() {
            load();
        }
        
        public TeamUpGlobalConfig getConfig(){
            return this.teamUpGlobalConfig;
        }

        public ListBoxModel doFillTokenCredentialIdItems() {
            if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                return new ListBoxModel();
            }
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withAll(lookupCredentials(
                            StringCredentials.class,
                            Jenkins.getInstance(),
                            ACL.SYSTEM,
                            new HostnameRequirement("*.teamup.com"))
                    );
        }

        //WARN users that they should not use the plain/exposed token, but rather the token credential id
        public FormValidation doCheckToken(@QueryParameter String value) {
            //always show the warning - TODO investigate if there is a better way to handle this
            return FormValidation.warning("Exposing your Integration Token is a security risk. Please use the Integration Token Credential ID");
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public TeamUpNotifier newInstance(StaplerRequest sr, JSONObject json) {
            String room = sr.getParameter("teamupRoom");
            boolean startNotification = "true".equals(sr.getParameter("teamupStartNotification"));
            boolean notifySuccess = "true".equals(sr.getParameter("teamupNotifySuccess"));
            boolean notifyAborted = "true".equals(sr.getParameter("teamupNotifyAborted"));
            boolean notifyNotBuilt = "true".equals(sr.getParameter("teamupNotifyNotBuilt"));
            boolean notifyUnstable = "true".equals(sr.getParameter("teamupNotifyUnstable"));
            boolean notifyFailure = "true".equals(sr.getParameter("teamupNotifyFailure"));
            boolean notifyBackToNormal = "true".equals(sr.getParameter("teamupNotifyBackToNormal"));
            boolean notifyRepeatedFailure = "true".equals(sr.getParameter("teamupNotifyRepeatedFailure"));
            boolean includeTestSummary = "true".equals(sr.getParameter("includeTestSummary"));
            CommitInfoChoice commitInfoChoice = CommitInfoChoice.forDisplayName(sr.getParameter("teamupCommitInfoChoice"));
            boolean includeCustomMessage = "on".equals(sr.getParameter("includeCustomMessage"));
            String customMessage = sr.getParameter("customMessage");
            return new TeamUpNotifier(room, startNotification, notifyAborted,
                    notifyFailure, notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
                    includeTestSummary, commitInfoChoice, includeCustomMessage, customMessage);
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            String clientId = sr.getParameter("teamupClientId");
            String clientSecret = sr.getParameter("teamupClientSecret");
            String userId = sr.getParameter("teamupUserId");
            String userPassword = sr.getParameter("teamupUserPassword");
            this.teamUpGlobalConfig = new TeamUpGlobalConfig(clientId, clientSecret, userId, userPassword);
            this.teamUpService = new TeamUpService(this.teamUpGlobalConfig);
            save();
            return super.configure(sr, formData);
        }

        @Override
        public String getDisplayName() {
            return "TeamUp Notifications";
        }

        public FormValidation doGlobalTestConnection(@QueryParameter("teamupClientId") final String teamupClientId,
                                                     @QueryParameter("teamupClientSecret") final String teamupClientSecret,
                                                     @QueryParameter("teamupUserId") final String teamupUserId,
                                                     @QueryParameter("teamupUserPassword") final String teamupUserPassword,
                                                     @QueryParameter("teamupTestRoom") final String testRoom) throws FormException {
            try {
                if (StringUtils.isEmpty(teamupClientId)) {
                    return FormValidation.error("ClientId is null");
                }
                if (StringUtils.isEmpty(teamupClientSecret)) {
                    return FormValidation.error("ClientSecret is null");
                }
                if (StringUtils.isEmpty(teamupUserId)) {
                    return FormValidation.error("User Id is null");
                }
                if (StringUtils.isEmpty(teamupUserPassword)) {
                    return FormValidation.error("User Password is null");
                }
                TeamUpService testTeamUpServiceService = new TeamUpService(new TeamUpGlobalConfig(teamupClientId, teamupClientSecret, teamupUserId, teamupUserPassword));
                String message = "TeamUP/Jenkins plugin: you're all set on " + DisplayURLProvider.get().getRoot();
                boolean success = testTeamUpServiceService.send(testRoom, message, Level.GOOD);
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }

        public FormValidation doTestConnection(@QueryParameter("teamupRoom") final String teamupRoom) throws FormException {
            try {
                if (StringUtils.isEmpty(teamupRoom)) {
                    return FormValidation.error("room is null");
                }
                String message = "TeamUP/Jenkins plugin: you're all set on " + DisplayURLProvider.get().getRoot();
                boolean success = this.teamUpService.send(teamupRoom, message, Level.GOOD);
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }
    }
}
