package tools.redfox.bamboo.notifications.slack.builder;

import com.atlassian.bamboo.deployments.notification.DeploymentFinishedNotification;
import com.atlassian.bamboo.deployments.notification.DeploymentStartedNotification;
import com.atlassian.bamboo.notification.Notification;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationBuilder {
    private static Payload messageFor(DeploymentStartedNotification notification) {
        SectionBlock header = new SectionBlock();
        header.setText(
                new MarkdownTextObject(
                        "Starting deployment of *<fakeLink.toUserProfiles.com|Iris / Zelda 1-1>* to *<ffakeLink.toUserProfiles.com|environment>*",
                        true
                )
        );
//        notification.getContext();
        return getBuilder(notification).build();
    }

    private static Payload messageFor(DeploymentFinishedNotification notification) {
        return getBuilder(notification).build();
    }

    public static String messageFor(Notification notification) {
        Payload message;

        if (notification instanceof DeploymentStartedNotification) {
            message = messageFor((DeploymentStartedNotification) notification);
        } else if (notification instanceof DeploymentFinishedNotification) {
            message = messageFor((DeploymentFinishedNotification) notification);
        } else {
            message = getBuilder(notification).build();
        }

        message.setText("Test message");

        Gson gson = GsonFactory.createSnakeCase();
        return gson.toJson(message);
    }

    protected static Payload.PayloadBuilder getBuilder(Notification notification) {
        return getBuilder(notification, new LinkedList<>());
    }

    protected static Payload.PayloadBuilder getBuilder(Notification notification, List<LayoutBlock> blocks) {
        Payload.PayloadBuilder builder = Payload.builder();

//        blocks.add(0, header(notification));
        blocks.add(1, new DividerBlock());
        builder.blocks(blocks);

        return builder;
    }

    protected static LayoutBlock header(Notification notification) {
        Map<Class, String> headers = new HashMap<Class, String>() {{
            put(DeploymentStartedNotification.class, "Deployment started");
            put(DeploymentFinishedNotification.class, "Deployment finished");
        }};

        SectionBlock header = new SectionBlock();


        return header;
    }
}
