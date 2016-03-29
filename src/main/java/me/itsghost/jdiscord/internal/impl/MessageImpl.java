package me.itsghost.jdiscord.internal.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import me.itsghost.jdiscord.message.Message;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MessageImpl implements Message {
    @Getter @Setter private String message;
    @Getter @Setter private String id;
    @Getter @Setter private User sender;
    @Getter @Setter private String groupId;
    @Getter @Setter private JSONArray mentions = new JSONArray();
    @Getter @Setter private boolean edited = false;
    @Getter @Setter private boolean TTS;
    @Setter private Date timestamp;
    @Setter private Date editedTimestamp;
    private DiscordAPIImpl api;

    public MessageImpl(String message, String id, String groupId, DiscordAPIImpl api) {
        this.message = message;
        this.id = id;
        this.groupId = groupId;
        this.api = api;
    }

    @Deprecated
    public MessageImpl(String message) {
        this.message = message;
    }

    public MessageImpl(DiscordAPI api, String message) {
        this.message = message;
        this.api = (DiscordAPIImpl)api;
    }

    @Override
    public String toString() {
        return message;
    }

    public void editMessage(String edit) {
        edited = true;
        message = edit;
        PacketBuilder pb = new PacketBuilder(api);
        pb.setType(RequestType.PATCH);
        pb.setData(new JSONObject().put("content", StringEscapeUtils.escapeJson(edit))
                .put("mentions", mentions).toString());
        pb.setUrl("https://discordapp.com/api/channels/" + groupId + "/messages/" + id);
        pb.makeRequest();
    }

    public void deleteMessage() {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setType(RequestType.DELETE);
        pb.setUrl("https://discordapp.com/api/channels/" + groupId + "/messages/" + id);
        pb.makeRequest();
    }

    public List<GroupUser> getUserMentions(){
        List<GroupUser> users = new ArrayList<>();
        for (int i = 0; i < mentions.length(); i++){
            JSONObject obj = (JSONObject)mentions.get(i);
            users.add(api.getGroupById(groupId).getGroupUserById(obj.getString("id")));
        }
        return users;
    }

    public void applyUserTag(String username, Group server) {
        GroupUser gp = server.getServer().getGroupUserByUsername(username);
        if (gp == null)
            return;
        //message = message.replace("@" + username, "<@" + gp.getUser().getId() + ">");
        mentions.put(gp.getUser().getId());
    }

    @Override
    public Date getTimestamp() {
        return timestamp == null ? null : new Date(timestamp.getTime());
    }

    @Override
    public Date getEditedTimestamp() {
        return editedTimestamp == null ? null : new Date(editedTimestamp.getTime());
    }
}
