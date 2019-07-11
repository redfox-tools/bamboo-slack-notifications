package tools.redfox.bamboo.notifications.slack.listener;

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;
import com.atlassian.bamboo.v2.build.BuildContext;
import org.jetbrains.annotations.NotNull;

public class PostJobAction extends AbstractActionListener implements com.atlassian.bamboo.chains.plugins.PostJobAction {
    @Override
    public void execute(@NotNull StageExecution stageExecution, @NotNull Job job, @NotNull BuildResultsSummary buildResultsSummary) {
        @NotNull BuildContext context = (BuildContext) stageExecution.getChainExecution().getBuildIdentifier();
        sendNotification(context, buildResultsSummary.getChainResultsSummary(), Notification.JOB);
    }
}
