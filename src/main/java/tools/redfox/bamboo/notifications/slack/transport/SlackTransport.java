package tools.redfox.bamboo.notifications.slack.transport;

import com.atlassian.bamboo.notification.Notification;
import com.atlassian.bamboo.notification.NotificationRecipient;
import com.atlassian.bamboo.notification.NotificationTransport;
import com.github.seratch.jslack.api.methods.SlackApiException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.redfox.bamboo.notifications.slack.recipient.SlackRecipient;
import tools.redfox.bamboo.notifications.slack.slack.SlackService;

import java.io.IOException;

public class SlackTransport implements NotificationTransport {
    private static Logger logger = LoggerFactory.getLogger(SlackTransport.class);
    private SlackService slackService;

    public SlackTransport(SlackService slackService) {
        this.slackService = slackService;
    }

    @Override
    public void sendNotification(@NotNull Notification notification) {
        for (NotificationRecipient recipient : notification.getNotificationRecipients()) {
            try {
                logger.info("Sending notification '{}'", notification.getClass().getSimpleName());
                slackService.send(req ->
                        req
                                .channel(slackService.getChannelId(((SlackRecipient) recipient).getSettings().get("notificationSlackChannel")))
                                .text(notification.getIMContent())

                );
            } catch (IOException | SlackApiException e) {
                logger.error("Failed to send slack notification with {}", e.toString());
            }
        }
    }
}
