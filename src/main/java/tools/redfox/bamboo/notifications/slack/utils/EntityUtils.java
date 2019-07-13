package tools.redfox.bamboo.notifications.slack.utils;

import com.atlassian.bamboo.commit.CommitContext;
import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.bamboo.v2.build.trigger.TriggerReason;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

@BambooComponent
public class EntityUtils {
    private UrlProvider urlProvider;

    @Autowired
    public EntityUtils(UrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    public String triggerReason(TriggerReason reason) {
        StringBuilder author = new StringBuilder();
        author.append(String.format(
                ":male-technologist: *%s at <!date^%s^{date_long} {time}|%s>*",
                reason.getName(),
                new Date().getTime() / 1000,
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())
        ));

        if (reason instanceof ManualBuildTriggerReason) {
            author.append(
                    MessageFormat.format(
                            " * by <{0}|{1}>*",
                            urlProvider.userProfile(((ManualBuildTriggerReason) reason).getUserName()),
                            ((ManualBuildTriggerReason) reason).getUserName()
                    )
            );
        }

        return author.toString();
    }

    public String jobResult(String stage, BuildResultsSummary result) {
        StringBuffer status = new StringBuffer(MessageFormat.format("{0} *{1}* | {2}", EmojiResolver.emoji(result), stage, result.getPlanName()));
        if (result.getTestResultsSummary().getTotalTestCaseCount() > 0) {
            status.append(MessageFormat.format(
                    " {0} in {1}",
                    result.getTestSummary(),
                    LocalTime.MIN.plusSeconds(result.getTestResultsSummary().getTotalTestDuration() / 1000).toString()
            ));
        }

        if (result.isFinished()) {
            status.append(MessageFormat.format(" (<{0}|results>)", urlProvider.job(result)));
        }

        return status.toString();
    }

    public String commit(CommitContext commitContext, PlanRepositoryDefinition repositoryData) {
        return MessageFormat.format(
                "<{0}|{1}> - {2} by {3}",
                urlProvider.commit(commitContext, repositoryData),
                commitContext.getChangeSetId().substring(0, 8),
                commitContext.getComment(),
                commitContext.getAuthorContext().getName()
        );
    }

    public String environment(Environment environment) {
        return MessageFormat.format(
                "<{0}|{1}>",
                urlProvider.environment(environment.getId()),
                environment.getName()
        );
    }

    public String deployment(DeploymentResult result) {
        if (result == null) {
            return "";
        }

        return MessageFormat.format(
                "<{0}|results>",
                urlProvider.deployment(result.getId())
        );
    }
}
