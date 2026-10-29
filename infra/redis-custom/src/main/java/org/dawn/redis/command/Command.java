package org.dawn.redis.command;

import org.dawn.redis.server.ClientSession;

import java.io.OutputStream;
import java.util.List;

public interface Command {
    void execute(List<String> args, OutputStream out, ClientSession session) throws Exception;
}
