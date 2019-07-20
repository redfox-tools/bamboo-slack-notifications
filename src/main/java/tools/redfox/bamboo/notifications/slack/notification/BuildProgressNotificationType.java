package tools.redfox.bamboo.notifications.slack.notification;

import com.atlassian.bamboo.notification.AbstractNotificationType;
import com.atlassian.bamboo.notification.NotificationType;

public class BuildProgressNotificationType extends AbstractNotificationType implements NotificationType {
    public static final String KEY = "tools.redfox.bamboo.slack-notifications:tools.redfox.slack.notification.build";
}
