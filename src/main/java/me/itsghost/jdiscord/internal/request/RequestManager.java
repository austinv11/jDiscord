package me.itsghost.jdiscord.internal.request;

import java.net.URI;

import lombok.Getter;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.internal.httprequestbuilders.PacketBuilder;
import me.itsghost.jdiscord.internal.httprequestbuilders.RequestType;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;

import org.json.JSONObject;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

public class RequestManager {
    @Getter private WebSocketClient socketClient;
    @Getter private URI uri;
    @Getter private WebSocket webSocket;

    public RequestManager(DiscordAPIImpl api) throws DiscordFailedToConnectException {

        try {
            PacketBuilder pb = new PacketBuilder(api);
            pb.setType(RequestType.GET);
            pb.setUrl("https://discordapp.com/api/gateway");
            String response = pb.makeRequest();

            if (response == null)
                throw new DiscordFailedToConnectException();

            String url = new JSONObject(response).getString("url");//.replace("ws", "ws");
            uri = URI.create(url);

            WebSocketFactory factory = new WebSocketFactory();

            if (url.contains("wss")){
                //SSLContext context = NaiveSSLContext.getInstance("TLS");
               // factory.setSSLContext(context);
            }

            webSocket = factory.createSocket(url);

            socketClient = new WebSocketClient(api, webSocket);

            webSocket.addListener(socketClient);

            webSocket.setPingInterval(5 * 1000);

            webSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DiscordFailedToConnectException();
        }
    }

}
