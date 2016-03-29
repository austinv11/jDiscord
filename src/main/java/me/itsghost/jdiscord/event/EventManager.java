package me.itsghost.jdiscord.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private List<EventListener> listeners = new ArrayList<>();

    public void registerListener(EventListener e) {
        listeners.add(e);
    }

    public void executeEvent(Object e) {
        for (EventListener ClassO : listeners) {
            for (Method m : ClassO.getClass().getMethods()) {
                try {
                    if (m.getParameterTypes()[0].getName().equals(e.getClass().getName())) {
                        try {
                            m.setAccessible(true);
                            m.invoke(ClassO, e);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            System.out.println("Couldn't run event!");
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

    }
}
