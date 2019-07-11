package tools.redfox.bamboo.notifications.slack.utils;

import com.atlassian.bamboo.commit.CommitContext;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.bamboo.v2.build.trigger.TriggerReason;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.plugin.spring.scanner.annotation.component.BambooComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
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
                ":male-technologist: *%s at <!date^%s^{date_long_pretty} {time}|%s>*",
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
        String status = MessageFormat.format("{0} *{1}* | {2}", EmojiResolver.emoji(result), stage, result.getPlanName());
        if (result.getTestResultsSummary().getTotalTestCaseCount() == 0) {
            return status;
        }

        return status + MessageFormat.format(
                "{0} in {1}",
                result.getTestSummary(),
                result.getTestResultsSummary().getTotalTestDuration()
        );
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
}
