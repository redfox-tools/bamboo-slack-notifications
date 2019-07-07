package tools.redfox.bamboo.notifications.slack.transport;

import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationRecipient;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.redfox.bamboo.notifications.slack.builder.NotificationBuilder;
import tools.redfox.bamboo.notifications.slack.recipient.SlackRecipient;

import java.io.IOException;

public class SlackTransport implements NotificationTransport {
    private static Logger logger = LoggerFactory.getLogger(SlackTransport.class);

    @Override
    public void sendNotification(@NotNull Notification notification) {
        String message = NotificationBuilder.messageFor(notification);
        Slack slack = Slack.getInstance();

        for (NotificationRecipient recipient : notification.getNotificationRecipients()) {
            try {
                logger.info("Sending notification '{}' to slack about {}", message, notification.getClass().getSimpleName());
                WebhookResponse response = slack.send(((SlackRecipient) recipient).getSettings().get("notificationSlackUrl"), message);
                if (response.getCode() != 200) {
                    logger.error("Failed to send slack notification with {}({}) - {}", response.getMessage(), response.getCode(), response.getBody());
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
