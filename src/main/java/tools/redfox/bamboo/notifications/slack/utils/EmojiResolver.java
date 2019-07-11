package tools.redfox.bamboo.notifications.slack.utils;

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
}
