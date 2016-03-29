package me.itsghost.jdiscord;

import java.util.List;

import me.itsghost.jdiscord.internal.impl.VoiceGroupImpl;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;

public interface Server {
    String getId();

    String getName();

    String getLocation();

    String getCreatorId();

    String getAvatar();

    GroupUser getGroupUserById(String id);

    GroupUser getGroupUserByUsername(String username);

    List<GroupUser> getConnectedClients();

    List<Group> getGroups();

    List<VoiceGroupImpl> getVoiceGroups();

    void kick(String user);

    void ban(String user);

    void kick(GroupUser user);

    void ban(GroupUser user);

    void bc(String message);

    Group getGroupById(String id);

    boolean canTalk();

    String createInvite();

    boolean createChannel(String name);

    List<Role> getAllRoles();

    void leave();
}
