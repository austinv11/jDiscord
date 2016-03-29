package me.itsghost.jdiscord.internal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.internal.request.RequestManager;
import me.itsghost.jdiscord.internal.request.WebSocketClient;

import org.json.JSONObject;

@Data
public class Login {
    private String username;
    private String password;
    @Getter @Setter protected String token;

    public void process(DiscordAPIImpl api) throws BadUsernamePasswordException, DiscordFailedToConnectException {
        api.log("Attempting to login!");

        JSONObject outJson = getTokens(api);

        if (outJson.isNull("token"))
            throw new BadUsernamePasswordException();

        token = outJson.getString("token");

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

    public JSONObject getTokens(DiscordAPIImpl api) throws DiscordFailedToConnectException {
        PacketBuilder pb = new PacketBuilder(api);
        pb.setSendLoginHeaders(false);
        pb.setData(new JSONObject().put("email", username).put("password", password).toString());
        pb.setUrl("https://discordapp.com/api/auth/login");
        pb.setType(RequestType.POST);

        final String out = pb.makeRequest();

        if (out == null)
            throw new DiscordFailedToConnectException();

        return new JSONObject(out);
    }

    public String getPCMetadata() {
        JSONObject login = new JSONObject();
        login.put("op", 2)
                .put("d", new JSONObject()
                        .put("token", token)
                        .put("properties", new JSONObject()
                                .put("$os", "BOT")
                                .put("$browser", "jDiscord by GitHub user NotGGhost")
                                .put("$device", "")
                                .put("$referring_domain", "t.co")
                                .put("$referrer", "")
                        )
                        .put("v", 3)
                );
        return login.toString();
    }
}
