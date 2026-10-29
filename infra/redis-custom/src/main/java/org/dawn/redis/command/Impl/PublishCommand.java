package org.dawn.redis.command.Impl;

import org.dawn.redis.command.Command;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.server.PubSubManager;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PublishCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream out, ClientSession session) throws Exception {
        if (args.size() != 3) {
            out.write("-ERR wrong number of arguments for 'publish' command\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        String channel = args.get(1);
        String message = args.get(2);
        PubSubManager.publish(channel, message);
        int count = PubSubManager.getSubscribedChannelCount(channel);
        out.write((":" + count + "\r\n").getBytes(StandardCharsets.UTF_8));
    }
}
