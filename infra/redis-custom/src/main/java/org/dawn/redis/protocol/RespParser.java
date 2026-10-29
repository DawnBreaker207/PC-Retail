package org.dawn.redis.protocol;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RespParser {
    public static List<String> parseCommand(BufferedReader reader) throws Exception {
        String firstLine = reader.readLine();
        if (firstLine == null) {
            return null;
        }
        if (firstLine.startsWith("*")) {
            int count = Integer.parseInt(firstLine.substring(1));
            List<String> tokens = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                reader.readLine();
                tokens.add(reader.readLine());
            }
            return tokens;
        }
        return Collections.emptyList();
    }
}
