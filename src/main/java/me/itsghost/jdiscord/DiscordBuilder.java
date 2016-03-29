package me.itsghost.jdiscord;

import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import me.itsghost.jdiscord.internal.LoginTokensGuest;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;

public class DiscordBuilder {
     private String email;
     private String password;

    public DiscordBuilder(String email, String password){
        this.email = email;
        this.password = password;
    }

    public DiscordBuilder(){}

    public static DiscordBuilder builder() {
        return new DiscordBuilder();
    }

    public DiscordBuilder setPassword(String pass){
        this.password = pass;
        return this;
    }

    public DiscordBuilder setEmail(String email){
        this.email = email;
        return this;
    }

    public DiscordAPI buildGuestAndLogin(String inviteId, String name) throws BadUsernamePasswordException, NoLoginDetailsException, DiscordFailedToConnectException {
        DiscordAPIImpl api = new DiscordAPIImpl(name, "Guest");

        LoginTokensGuest login = new LoginTokensGuest();
        login.setPassword("Guest");
        login.setUsername(name);

        api.setInviteLink(inviteId);
        api.setLoginTokens(login);
        api.login();

        return api;
    }

    public DiscordAPI build(){
        return new DiscordAPIImpl(email, password);
    }
}
