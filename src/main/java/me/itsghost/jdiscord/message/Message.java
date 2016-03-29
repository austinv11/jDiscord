package me.itsghost.jdiscord.message;

import java.util.Date;
import java.util.List;

import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

public interface Message {
    String getMessage();

    void setMessage(String message);

    User getSender();

    String getId();

    String getGroupId();

    boolean isEdited();

    void setTTS(boolean tts);

    boolean isTTS();

    void setTimestamp(Date timestamp);

    Date getTimestamp();

    void setEditedTimestamp(Date editedTimestamp);

    Date getEditedTimestamp();

    void applyUserTag(String username, Group server);

    List<GroupUser> getUserMentions();

    void deleteMessage();

    void editMessage(String message);
}
