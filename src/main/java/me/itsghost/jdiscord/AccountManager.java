package me.itsghost.jdiscord;

import java.io.IOException;
import java.io.InputStream;

public interface AccountManager {
    void setOnlineStatus(boolean online);

    void setDisplayName(String displayName);

    void setAvatar(InputStream is) throws IOException;

    void changePass(String pass);

    void changeEmail(String pass);

    String getGame();

    void setGame(String name);
}
