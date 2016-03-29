package me.itsghost.jdiscord.internal.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.itsghost.jdiscord.Role;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import me.itsghost.jdiscord.internal.request.poll.UserUpdatePoll;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServerImpl implements Server {
    @Getter @Setter private String id;
    @Getter @Setter private String name;
    @Getter @Setter private String location;
    @Getter @Setter private String creatorId;
    @Getter @Setter private String avatar;
    @Getter @Setter private String token;
    @Getter @Setter private String server;
    @Getter @Setter private List<GroupUser> connectedClients = new ArrayList<>();
    @Getter @Setter private List<Group> groups = new ArrayList<>();
    @Getter @Setter private List<VoiceGroupImpl> voiceGroups = new ArrayList<>();
    @Getter @Setter private JSONArray roleMeta;
    @Getter @Setter private List<Role> roles;

    private DiscordAPIImpl api;

    public ServerImpl(String id, DiscordAPIImpl api) {
        this.api = api;
        this.id = id;
    }

    public String toString() {
        return id;
    }

    @Override
    public GroupUser getGroupUserById(String id) {
        for (GroupUser user : connectedClients)
            if (user.getUser().equals(id))
                return user;
        return null;
    }

    public boolean createChannel(String name){
        PacketBuilder pb = new PacketBuilder(api);
        pb.setData(new JSONObject()
            .put("type", "text")
            .put("name", name).toString());
        pb.setType(RequestType.POST);
        pb.setUrl(String.format("https://discordapp.com/api/guilds/%s/channels", getId()));
        return pb.makeRequest() != null;
    }

    @Override
    public List<Role> getAllRoles() {
        return UserUpdatePoll.getRoles(this.getRoleMeta());
    }

    @Override
    public Group getGroupById(String id) {
        for (Group group : getGroups())
            if (group.getId().equals(id))
                return group;
        return null;
    }

    @Override
    public GroupUser getGroupUserByUsername(String id) {
        for (GroupUser user : connectedClients)
            if (user.getUser().getUsername().equals(id))
                return user;
        return null;

    }

    @Override
    public void bc(String message) {
        for (Group group : getGroups())
            group.sendMessage(message);
    }

    @Override
    public void kick(String user) {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/guilds/" + id + "/members/" + getGroupUserByUsername(user).getUser().getId());
        pb.setType(RequestType.DELETE);
        pb.makeRequest();
    }

    @Override
    public void ban(String user) {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/guilds/" + id + "/bans/" + getGroupUserByUsername(user).getUser().getId() + "?delete-message-days=0");
        pb.setType(RequestType.PUT);
        pb.makeRequest();
    }

    @Override
    public void kick(GroupUser user) {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/guilds/" + id + "/members/" + user.getUser().getId());
        pb.setType(RequestType.DELETE);
        pb.makeRequest();
    }

    @Override
    public void ban(GroupUser user) {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/guilds/" + id + "/bans/" + user.getUser().getId() + "?delete-message-days=0");
        pb.setType(RequestType.PUT);
        pb.makeRequest();
    }

    @Override
    public void leave() {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/guilds/" + id);
        pb.setType(RequestType.DELETE);
        pb.makeRequest();
    }

    @Override
    public String createInvite() {
        return groups.get(0).createInvite();
    }

    @Override
    public boolean canTalk() {
        return this.getGroupUserById(api.getSelfInfo().getId()).hasPerm(GroupUser.Permissions.SEND_MESSAGES);
    }

    public void updateUser(GroupUser user) {
        ArrayList<GroupUser> users = new ArrayList<>();
        for (GroupUser userA : connectedClients)
            if (userA.getUser().getId().equals(user.getUser().getId()))
                users.add(userA);
        connectedClients.removeAll(users);
        connectedClients.add(user);
    }
}
