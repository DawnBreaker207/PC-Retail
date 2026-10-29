package org.dawn.redis.server;

import org.dawn.redis.storage.RedisStore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientSession {
    private boolean inTransaction = false;
    private boolean isDirty = false;
    private final List<List<String>> commandQueue = new ArrayList<>();
    private final Set<String> watchedKeys = new HashSet<>();
    private final Set<String> subscribedChannels = new HashSet<>();
    private OutputStream out;

    public boolean isInTransaction() {
        return inTransaction;
    }

    public void setInTransaction(boolean inTransaction) {
        this.inTransaction = inTransaction;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public void multi() {
        inTransaction = true;
        commandQueue.clear();
    }

    public void addCommand(List<String> command) {
        commandQueue.addLast(command);
    }

    public List<List<String>> getCommandQueue() {
        return Collections.unmodifiableList(commandQueue);
    }

    public void clearCommandQueue() {
        commandQueue.clear();
    }

    public String exec() {
        if (!inTransaction) {
            return "-ERR EXEC without MULTI\r\n";
        }
        inTransaction = false;

        if (commandQueue.isEmpty()) {
            return "*0\r\n";
        }
        return "*1\r\n";
    }

    public void watch(List<String> keys) {
        watchedKeys.addAll(keys);
        for (String key : keys) {
            RedisStore.watch(key, this);
        }
    }

    public void unwatch() {
        for (String key : watchedKeys) {
            RedisStore.unwatch(key, this);
        }
        watchedKeys.clear();
        isDirty = false;
    }

    public void subscribe(String channel) {
        subscribedChannels.add(channel);
    }

    public int getSubscribedChannelCount() {
        return subscribedChannels.size();
    }

    public boolean isSubscribed() {
        return !subscribedChannels.isEmpty();
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public synchronized void sendPushMessage(String channel, String message) {
        if (out == null) return;
        try {
            String resp = "*3\r\n"
                    + "$7\r\nmessage\r\n"
                    + "$" + channel.getBytes(StandardCharsets.UTF_8).length + "\r\n" + channel + "\r\n"
                    + "$" + message.getBytes(StandardCharsets.UTF_8).length + "\r\n" + message + "\r\n";
            out.write(resp.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            ;
        }
    }

    public void unsubscribe(String channel) {
        subscribedChannels.remove(channel);
    }

    public Set<String> getSubscribedChannels() {
        return new HashSet<>(subscribedChannels);
    }
}
