package me.itsghost.jdiscord.internal.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import me.itsghost.jdiscord.message.Message;
import me.itsghost.jdiscord.message.MessageHistory;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.Talkable;

import org.json.JSONArray;
import org.json.JSONObject;

public class GroupImpl implements Group, Talkable {
    @Getter @Setter private String cid;
    @Getter @Setter private String id;
    @Getter @Setter private String name;
    @Getter @Setter private String topic;
    @Getter @Setter private int position;
    private DiscordAPIImpl api;
    private Server server;

    public GroupImpl(String id, String cid, Server server, DiscordAPIImpl api) {
        this.api = api;
        this.id = id;
        this.name = id;
        this.cid = cid;
        this.server = server;
    }

    @Override
    public String toString() {
        return name;
    }


    @Override
    public Server getServer() {
        return server;
    }


    @Override
    public MessageHistory getMessageHistory() {
        return new MessageHistory(api, this);
    }

    @Override
    public Message sendMessage(String message) {
        return sendMessage(new MessageImpl(message, id, id, api));
    }

    @Override
    public Message sendMessage(Message messageInterface) {
        if (server == null)
            updateId();

        MessageImpl message = (MessageImpl) messageInterface;
        message.setId(String.valueOf(System.currentTimeMillis()));

        PacketBuilder pb = new PacketBuilder(api);
        pb.setType(RequestType.POST);
        pb.setData(new JSONObject().put("content", message.getMessage())
                .put("mentions", message.getMentions())
                .put("embeds", new JSONArray())
                .put("mention_everyone", false)
                .put("edited_timestamp", JSONObject.NULL)
                .put("tts", message.isTTS())
                .put("channel_id", id)
                .put("nonce", message.getId()).toString());
        pb.setUrl("https://discordapp.com/api/channels/" + id + "/messages");

        String a = pb.makeRequest();
        if (a != null)
            return new MessageImpl(message.getMessage(), new JSONObject(a).getString("id"), id, api);

        return message;
    }


    @Override
    public void typing() {
        if ((server == null) || (cid == null))
            updateId();

        PacketBuilder pb = new PacketBuilder(api);
        pb.setType(RequestType.POST);
        pb.setUrl("https://discordapp.com/api/channels/" + id + "/typing");
        pb.makeRequest();
    }

    @SuppressWarnings("unused")
	private JSONObject getLocalAuthor() {
        JSONObject a = new JSONObject()
                .put("username", api.getSelfInfo().getUsername())
                .put("discriminator", server.getGroupUserByUsername(api.getSelfInfo().getUsername()).getDiscriminator())
                .put("avatar", api.getSelfInfo().getAvatarId());
        return a;
    }

    public void rename(String name){
        PacketBuilder pb = new PacketBuilder(api);
        pb.setData(new JSONObject().put("name", name).put("position", position).put("topic", topic).toString());
        pb.setUrl("https://discordapp.com/api/channels/" + id);
        pb.setType(RequestType.PATCH);
        pb.makeRequest();
    }

    public void changeTopic(String topic){
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/channels/" + id);
        pb.setType(RequestType.PATCH);
        pb.setData(new JSONObject().put("name", name).put("position", position).put("topic", topic).toString());
        System.out.println(pb.getUrl() + " - " + pb.getData());
        pb.makeRequest();
    }

    @SuppressWarnings("unused")
	private String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "T" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + ".135000+00:00";
    }

    private void updateId() {
        if (id.equals(api.getSelfInfo().getId()))
            return;

        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/users/" + api.getSelfInfo().getId() + "/channels");
        pb.setType(RequestType.POST);
        pb.setData(new JSONObject().put("recipient_id", id).toString());
        String a = pb.makeRequest();

        if (a == null)
            return;

        id = new JSONObject(a).getString("id");
    }

    public GroupUser getGroupUserById(String id) {
        return getServer().getGroupUserById(id);
    }

    //TODO: fix
    public void mute() {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/users/@me/settings");
        pb.setType(RequestType.PATCH);
        pb.setData(new JSONObject().put("muted_channels", api.getMutedChannels()).toString());
        pb.makeRequest();
    }

    public void destroy() {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/channels/" + id);
        pb.setType(RequestType.DELETE);
        pb.makeRequest();
    }

    public String createInvite() {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/channels/" + id + "/invites");
        pb.setType(RequestType.POST);
        pb.setData(new JSONObject().put("validate", JSONObject.NULL).toString());
        String response = pb.makeRequest();
        if (response == null)
            return  "";
        String code = new JSONObject(response).getString("code");
        return "https://discord.gg/" + code;
    }
}
