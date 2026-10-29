package org.dawn.redis.server;

import org.dawn.redis.command.Command;
import org.dawn.redis.command.CommandRegistry;
import org.dawn.redis.protocol.RespParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class RedisServer {
    private final int port;
    private static final Set<String> ALLOWED = Set.of("SUBSCRIBE", "UNSUBSCRIBE", "PSUBSCRIBE", "PUNSUBSCRIBE", "PING", "QUIT", "RESET");

    public RedisServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Redis Server starting...");
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            System.out.println("Waiting for connection...");

            while (true) {
                Socket client = socket.accept();
                System.out.println("Connection established from " + client.getRemoteSocketAddress());
                Thread.ofVirtual().start(() -> handleClient(client));
            }
        } catch (IOException e) {
            System.out.println("I/O Error " + e.getMessage());
        }
    }

    private static void handleClient(Socket client) {
        ClientSession session = new ClientSession();
        try (client) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            OutputStream out = client.getOutputStream();
            session.setOutputStream(out);
            List<String> command;
            while ((command = RespParser.parseCommand(reader)) != null) {
                if (command.isEmpty()) continue;
                String cmdName = command.getFirst();
                Command cmd = CommandRegistry.get(cmdName);

                if (session.isSubscribed() && !ALLOWED.contains(cmdName.toUpperCase())) {
                    out.write(("-ERR Can't execute '" +
                            cmdName.toLowerCase() +
                            "': only (P)SUBSCRIBE / (P)UNSUBSCRIBE / PING / QUIT / RESET are allowed in this context\r\n").getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    continue;
                }

                if (session.isInTransaction()
                        && !cmdName.equalsIgnoreCase("EXEC")
                        && !cmdName.equalsIgnoreCase("DISCARD")
                        && !cmdName.equalsIgnoreCase("WATCH")) {
                    session.addCommand(command);
                    out.write("+QUEUED\r\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    continue;
                }

                if (cmd != null) {
                    cmd.execute(command, out, session);
                } else {
                    out.write(("-ERR unknown command '" + cmdName + "'\r\n").getBytes(StandardCharsets.UTF_8));
                }
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
        } finally {
            session.unwatch();
            PubSubManager.unsubscribeAll(session);
        }
    }
}
