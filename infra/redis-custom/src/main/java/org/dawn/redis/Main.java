package org.dawn.redis;

import org.dawn.redis.aof.AofManager;
import org.dawn.redis.command.Command;
import org.dawn.redis.command.CommandRegistry;
import org.dawn.redis.protocol.RespParser;
import org.dawn.redis.rdb.RdbReader;
import org.dawn.redis.server.ClientSession;
import org.dawn.redis.server.RedisServer;
import org.dawn.redis.server.ServerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int port = 8888;
        ServerConfig.parse(args);
        RdbReader.load(ServerConfig.getDir(), ServerConfig.getDbFileName());
        AofManager.init();
        new RedisServer(port).start();
    }


}
