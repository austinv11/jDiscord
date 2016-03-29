package me.itsghost.jdiscord.internal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import me.itsghost.jdiscord.AccountManager;
import me.itsghost.jdiscord.DiscordAPI;
import me.itsghost.jdiscord.SelfData;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.event.EventManager;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import me.itsghost.jdiscord.internal.Login;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import me.itsghost.jdiscord.internal.request.RequestManager;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;

import org.json.JSONArray;
import org.json.JSONObject;

public class DiscordAPIImpl implements DiscordAPI {
    @Getter @Setter private Login loginTokens = new Login();
    @Getter private List<User> availableDms = new ArrayList<>();
    @Getter private List<Server> availableServers = new ArrayList<>();
    @Getter private Map<String, Group> userGroups = new HashMap<>();
    @Getter @Setter private boolean debugMode = false;
    @Getter @Setter private boolean allowLogMessages = true;
    @Getter private EventManager eventManager = new EventManager();
    @Getter @Setter private RequestManager requestManager;
    @Getter @Setter private SelfData selfInfo;
    @Getter @Setter private boolean loaded = false;
    @Getter @Setter private String as = "";
    @Getter @Setter private AccountManager accountManager = new AccountManagerImpl(this);
    @Getter private final Long startedTime = System.currentTimeMillis();
    @Getter @Setter private JSONArray mutedChannels;
    @Getter @Setter List<Group> mutedGroups = new ArrayList<>();
    @Getter @Setter private String inviteLink = "";
    @Getter @Setter private String email = "";


    public DiscordAPIImpl(String email, String password) {
        this.email = email;
        loginTokens.setUsername(email);
        loginTokens.setPassword(password);
    }

    public DiscordAPIImpl() {}

    public DiscordAPIImpl login(String email, String password) throws BadUsernamePasswordException, DiscordFailedToConnectException {
        loginTokens.setUsername(email);
        loginTokens.setPassword(password);
        try {
            login();
        } catch (NoLoginDetailsException e) {
        } catch (BadUsernamePasswordException | DiscordFailedToConnectException e) {
            throw e;
        }
        return this;
    }

    public DiscordAPIImpl login() throws NoLoginDetailsException, BadUsernamePasswordException, DiscordFailedToConnectException {
        if ((loginTokens.getUsername() == null) || (loginTokens.getPassword() == null))
            throw new NoLoginDetailsException();
        loginTokens.process(this);
        return this;
    }

    public void log(String log) {
        if (allowLogMessages)
            System.out.println("DiscordAPI: " + log);
    }

    public Group getGroupById(String id) {
        for (User channel : availableDms)
            if (channel.equals(id))
                return channel.getGroup();

        for (Server server : availableServers)
            for (Group channel : server.getGroups())
                if (channel.getId().equals(id))
                    return channel;
        return null;
    }

    public Server getServerById(String id) {
        for (Server server : availableServers)
            if (server.getId().equals(id))
                return server;
        return null;
    }

    public User getUserByUsername(String id) {
        for (User user : availableDms)
            if (user.getUsername().equals(id))
                return user;
        return null;
    }

    public User getUserById(String id) {
        for (User user : availableDms)
            if (user.equals(id))
                return user;
        for (Server server : availableServers)
            for (GroupUser user : server.getConnectedClients())
                if (user.getUser().equals(id))
                    return user.getUser();
        return null;
    }


    public VoiceGroupImpl getVoiceGroupById(String id){
        for (Server server : availableServers)
            for (VoiceGroupImpl channel : server.getVoiceGroups())
                if (channel.getId().equals(id))
                    return channel;
        return null;
    }

    public boolean isUserKnown(String id){
        return getUserById(id) != null;
    }

    public void stop() {
        log("Shutting down!");
        requestManager.getSocketClient().stop();
    }

    public void createServer(String name, Location location){
        PacketBuilder pb = new PacketBuilder(this);
        pb.setData(new JSONObject().put("name", name).put("region", location.toString()).put("icon", JSONObject.NULL).toString());
        pb.setType(RequestType.POST);
        pb.setUrl("https://discordapp.com/api/guilds");
        pb.makeRequest();
    }

    public void updateContact(User user) {
        ArrayList<User> users = new ArrayList<User>();
        for (User userA : availableDms)
            if (userA.getId().equals(user.getId()))
                users.add(userA);
        availableDms.removeAll(users);
        availableDms.add(user);
    }

    public Boolean joinInviteId(String id){
        PacketBuilder rb = new PacketBuilder(this);
        rb.setUrl("https://discordapp.com/api/invite/" + id);
        rb.setType(RequestType.POST);
        return rb.makeRequest() != null;
    }

    public User getBlankUser(){
        UserImpl user = new UserImpl(getSelfInfo().getUsername(), getSelfInfo().getId(), "Me", this);
        user.setAvatarId(getSelfInfo().getAvatarId());
        user.setAvatar(getSelfInfo().getAvatar());
        return user;
    }

    public enum Location {
        london
    }
}
