package tools.redfox.bamboo.notifications.slack.listener;

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.plugins.PostChainAction;
import com.atlassian.bamboo.v2.build.BuildContext;
import org.jetbrains.annotations.NotNull;

public class PostBuildAction extends AbstractActionListener implements PostChainAction {
    @Override
    public void execute(@NotNull Chain chain, @NotNull ChainResultsSummary chainResultsSummary, @NotNull ChainExecution chainExecution) throws InterruptedException, Exception {
        @NotNull BuildContext context = (BuildContext) chainExecution.getBuildIdentifier();
        sendNotification(context, chainResultsSummary, Notification.FINISHED);
    }
}
