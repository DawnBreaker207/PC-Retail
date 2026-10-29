package org.dawn.redis.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PubSubManager {
    private static final Map<String, Set<ClientSession>> subscribedChannels = new ConcurrentHashMap<>();


    public static void subscribe(String channel, ClientSession session) {
        session.subscribe(channel);
        subscribedChannels.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public static int getSubscribedChannelCount(String channel) {
        Set<ClientSession> subs = subscribedChannels.get(channel);
        return (subs != null) ? subs.size() : 0;
    }

    public static void publish(String channel, String message) {
        Set<ClientSession> subs = subscribedChannels.get(channel);
        if (subs != null) {
            for (ClientSession sub : subs) {
                sub.sendPushMessage(channel, message);
            }
        }
    }

    public static void unsubscribe(String channel, ClientSession session) {
        Set<ClientSession> subs = subscribedChannels.get(channel);
        if (subs != null) {
            subs.remove(session);
            if (subs.isEmpty()) {
                subscribedChannels.remove(channel);
            }
        }
    }

    public static void unsubscribeAll(ClientSession session) {
        for (String chanel : session.getSubscribedChannels()) {
            unsubscribe(chanel, session);
        }
    }
}
