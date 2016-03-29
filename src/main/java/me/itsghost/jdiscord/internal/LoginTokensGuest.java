package me.itsghost.jdiscord.internal;

import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.request.RequestManager;
import me.itsghost.jdiscord.internal.request.WebSocketClient;

import org.json.JSONObject;

/**
 * Created by Ghost on 09/01/2016.
 */
public class LoginTokensGuest extends Login {
    @Override
    public void process(DiscordAPIImpl api) throws BadUsernamePasswordException, DiscordFailedToConnectException {
        api.log("Attempting to login as a guest!");

        token = getToken(api);

        api.log("Logged in and starting session!");
        api.log("Token: " + token);

        api.setRequestManager(new RequestManager(api));
        WebSocketClient socket = api.getRequestManager().getSocketClient();

        while (!socket.loaded) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
        }
        socket.send(getPCMetadata());
    }

    public String getToken(DiscordAPIImpl api) {
        String inviteid = api.getInviteLink();
        if (inviteid.contains("/"))
            inviteid = inviteid.split("/")[inviteid.split("/").length - 1];
        PacketBuilder pb = new PacketBuilder(api);
        pb.setUrl("https://discordapp.com/api/auth/register");
        pb.setType(RequestType.POST);
        pb.setData(new JSONObject().put("fingerprint", JSONObject.NULL).put("username", api.getEmail()).put("invite", inviteid).toString());
        return new JSONObject(pb.makeRequest()).getString("token");
    }

}
