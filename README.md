#jDiscord

This API thrives to be the best Discord API written in Java with features no other API has, such as account management, and VOIP (WIP).

[Download](#shut-up-and-take-my-money) with maven

#Features
- Guest support
- Kicking and banning                  
- Profile settings/account settings	
- Group editing
- Message building
- Online statuses
- Avatars + Roles 
- DMs
- Group messaging
- User talk (edited) event 
- User join/banned/kicked events
- Invite joining
- TTS
- Permissions
- History
- Salt
- Much more... 


#Events
- AddedToServer       (AddedToGuildEvent)
- APILoadedEvent      (You might get NPEs if you don't wait for this)
- ChannelCreatedEvent (group/channel)
- ChannelDeletedEvent (group/channel)
- ChannelUpdatedEvent (group/channel)
- UserBannedEvent
- UserChatEvent
- UserJoinedEvent
- UserKickedEvent
- UserTypingEvent
- UserOnlineStatusChangedEvent
- UserDeletedMessageEvent
- MentionEvent (1.3)
- Much more... 



#Creating a DiscordAPI instance

In order to create the DiscordAPI instance, you'll need to use the DiscordBuilder class. 

Examples:
```java
DiscordAPI api = new DiscordBuilder("email", "pass").build().login();

DiscordAPI api = new DiscordBuilder("email", "pass").build();
api.login();
```

#Using the event manager
In order to listen for an event, create a class that implements EventListener, and register it by calling `api.getEventManager().registerListener(new YourListener(api));`. All events can be found in the `me.itsghost.jdiscord.events` package as well as the [Events](#events) section. 

```java
public class ExampleListener implements EventListener {
    DiscordAPI api;
    public ExampleListener(DiscordAPI api){
        this.api = api;
    }
    public void userChat(UserChatEvent e){
        if (e.getMsg().getMessage().equals("#ping")){
            e.getGroup().sendMessage(new MessageBuilder()
                    .addString("Yes, ")
                    .addUserTag(e.getGroupUser(), e.getGroup())
                    .addString("?")
                    .build());
        }
        System.out.println((e.getMsg().isEdited() ? "# " : "") + "[" + e.getGroup().getName() + "] " + e.getGroupUser() + " > " + e.getMsg().getMessage());
    }
    public void typing(UserTypingEvent e){
        System.out.println(e.getGroupUser() + " is typing in " + e.getGroup());
    }
}

public class Test {
    public static void main(String[] args) {
        DiscordAPI api = new DiscordBuilder("email", "pass").login();
        api.getEventManager().registerListener(new ExampleListener(api)); //Register listener
    }
}
```
#Shut up and take my money! 
###(Now using shaded jar due to compatibility issues with past builds)
[Maven](http://itsghost.me/maven)


Repository:
```
<repository>
  <id>me.itsghost</id>
  <url>http://itsghost.me/maven/</url>
</repository>
```
Dependency:
```
<dependency>
    <groupId>me.itsghost</groupId>
    <artifactId>jDiscord</artifactId>
    <version>2.1.2</version>
</dependency>
```


#Dependencies
- [Apache Commons Lang 3](https://commons.apache.org/proper/commons-lang/)
- [lombok](https://projectlombok.org/)
- [JSON](http://www.json.org/java/)
- [Java-Websocket](https://github.com/tootallnate/java-websocket)
- http://itsghost.me/commons-codec-1.10.jar
- [nv-websocket-client] (http://hastebin.com/ucucaqomip.xml)
