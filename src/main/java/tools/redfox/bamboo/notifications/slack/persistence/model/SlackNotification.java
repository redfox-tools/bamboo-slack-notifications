package tools.redfox.bamboo.notifications.slack.persistence.model;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

@Table(value = "slack_notifications")
public interface SlackNotification extends Entity {
    String getMessageTs();
    void setMessageTs(String messageTs);

    long getEntityKey();
    void setEntityKey(long entityId);

    String getRelEntityType();
    void setRelEntityType(String entityType);
}
