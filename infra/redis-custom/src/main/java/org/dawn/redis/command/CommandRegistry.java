package org.dawn.redis.command;

import org.dawn.redis.command.Impl.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandRegistry {
    private static final Map<String, Command> commands = new ConcurrentHashMap<>();

    public static void register(String name, Command command) {
        commands.put(name.toUpperCase(), command);
    }

    public static Command get(String name) {
        if (name == null) return null;
        return commands.get(name.toUpperCase());
    }

    static {
        register("PING", new PingCommand());
        register("ECHO", new EchoCommand());
        register("SET", new SetCommand());
        register("GET", new GetCommand());
        register("RPUSH", new RPushCommand());
        register("LRANGE", new LRangeCommand());
        register("LPUSH", new LPushCommand());
        register("LLEN", new LLenCommand());
        register("LPOP", new LPopCommand());
        register("BLPOP", new BLPopCommand());
        register("INCR", new IncrCommand());
        register("MULTI", new MultiCommand());
        register("EXEC", new ExecCommand());
        register("DISCARD", new DiscardCommand());
        register("WATCH", new WatchCommand());
        register("UNWATCH", new UnwatchCommand());
        register("CONFIG", new ConfigCommand());
        register("KEYS", new KeysCommand());
        register("SUBSCRIBE", new SubscribeCommand());
        register("PUBLISH", new PublishCommand());
        register("UNSUBSCRIBE", new UnsubscribeCommand());
    }
}
