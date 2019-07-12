package tools.redfox.bamboo.notifications.slack.utils;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.deployments.results.DeploymentResult;
import com.atlassian.bamboo.resultsummary.BuildResultsSummary;

public class EmojiResolver {
    public static String emoji(BuildResultsSummary result) {
        if (result.isSuccessful()) {
            return ":white_check_mark:";
        } else if (result.isFailed()) {
            return ":x:";
        } else if (result.isInProgress()) {
            return ":arrows_counterclockwise:";
        } else {
            return ":clock1:";
        }
    }

    public static String emoji(DeploymentResult result) {
        if (result == null) {
            return ":clock1:";
        }
        else if (result.getDeploymentState() == BuildState.SUCCESS) {
            return ":white_check_mark:";
        } else if (result.getDeploymentState() == BuildState.FAILED) {
            return ":x:";
        }
        return ":clock1:";
    }
}
