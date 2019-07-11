package tools.redfox.bamboo.notifications.slack.listener;

import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.plugins.PreChainAction;
import com.atlassian.bamboo.v2.build.BuildContext;
import org.jetbrains.annotations.NotNull;

public class PreBuildAction extends AbstractActionListener implements PreChainAction {
    @Override
    public void execute(@NotNull Chain chain, @NotNull ChainExecution chainExecution) throws InterruptedException, Exception {
        @NotNull BuildContext context = (BuildContext) chainExecution.getBuildIdentifier();
        sendNotification(context, null, Notification.STARTED);
    }
}
