package org.dawn.redis.storage;

import org.dawn.redis.server.ClientSession;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class RedisStore {

    private static final Map<String, String> data = new ConcurrentHashMap<>();
    private static final Map<String, Long> expiry = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> lists = new ConcurrentHashMap<>();
    private static final Map<String, Queue<CompletableFuture<String>>> queue = new ConcurrentHashMap<>();
    private static final Map<String, Set<ClientSession>> watchedClients = new ConcurrentHashMap<>();

    public static void setWithExpiry(String key, String value, long ttlMs) {
        data.put(key, value);
        expiry.put(key, System.currentTimeMillis() + ttlMs);
        touch(key);
    }

    public static void setWithAbsoluteExpiry(String key, String value, long expireAtMs) {
        data.put(key, value);
        expiry.put(key, expireAtMs);
        touch(key);
    }

    public static void set(String key, String value) {
        data.put(key, value);
        expiry.remove(key);
        touch(key);
    }

    public static String get(String key) {
        Long expireAt = expiry.get(key);
        if (expireAt != null && System.currentTimeMillis() > expireAt) {
            data.remove(key);
            expiry.remove(key);
            return null;
        }
        return data.get(key);
    }

    public static int rPush(String key, List<String> values) {
        if (key == null) {
            return 0;
        }
        Queue<CompletableFuture<String>> q = queue.get(key);
        if (q != null && !q.isEmpty() && !values.isEmpty()) {
            CompletableFuture<String> waiter = q.poll();
            if (waiter != null) {
                String val = values.removeFirst();
                waiter.complete(val);
            }
        }
        List<String> list = lists.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));
        list.addAll(values);

        return list.size();
    }


    public static List<String> lRange(String key, int start, int stop) {
        List<String> list = lists.get(key);
        if (list == null || list.isEmpty()) return Collections.emptyList();

        synchronized (list) {
            int size = list.size();
            if (start < 0) start = size + start;
            if (stop < 0) stop = size + stop;
            if (start < 0) start = 0;
            if (start >= list.size() || start > stop) return Collections.emptyList();

            int safeStop = Math.min(stop, list.size() - 1);
            return new ArrayList<>(list.subList(start, safeStop + 1));
        }
    }

    public static int lPush(String key, List<String> values) {
        if (key == null) {
            return 0;
        }
        List<String> list = lists.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));

        synchronized (list) {
            for (String value : values) {
                list.addFirst(value);
            }
            return list.size();
        }
    }

    public static int getLength(String key) {
        List<String> list = lists.get(key);
        if (list == null) {
            return 0;
        }
        synchronized (list) {
            return list.size();
        }
    }

    public static String lPop(String key) {
        List<String> list = lists.get(key);
        if (list == null || list.isEmpty()) {
            return null;
        }
        synchronized (list) {
            if (list.isEmpty()) {
                return null;
            }
            return list.removeFirst();
        }
    }


    public static List<String> lPop(String key, int count) {
        List<String> list = lists.get(key);
        if (list == null || list.isEmpty()) {
            return null;
        }
        synchronized (list) {
            int item = Math.min(count, list.size());
            if (list.isEmpty()) {
                return null;
            }
            List<String> result = new ArrayList<>();
            for (int i = 0; i < item; i++) {
                result.add(list.removeFirst());
            }
            return result;
        }
    }

    public static String blPop(String key) {
        try {
            List<String> list = lists.get(key);
            if (list != null && !list.isEmpty()) {
                return lPop(key);
            }
            CompletableFuture<String> future = new CompletableFuture<>();
            queue.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>()).add(future);
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String blPop(String key, long timeoutMs) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            List<String> list = lists.get(key);
            if (list != null && !list.isEmpty()) {
                return lPop(key);
            }
            queue.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>()).add(future);
            if (timeoutMs == 0) {
                return future.get();
            } else {

                return future.get(timeoutMs, TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException e) {
            Queue<CompletableFuture<String>> q = queue.get(key);
            if (q != null) {
                q.remove(future);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static long incr(String key) {
        String newVal = data.compute(key, (k, current) -> {
            Long expireAt = expiry.get(key);
            if (expireAt != null && System.currentTimeMillis() > expireAt) {
                current = null;
                expiry.remove(key);
                touch(key);
            }
            long val = (current == null) ? 0 : Long.parseLong(current);
            return String.valueOf(val + 1);
        });
        touch(key);
        return Long.parseLong(newVal);
    }


    public static void watch(String key, ClientSession session) {
        watchedClients.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public static void touch(String key) {
        Set<ClientSession> sessions = watchedClients.get(key);
        if (sessions != null) {
            for (ClientSession session : sessions) {
                session.setDirty(true);
            }
        }
    }

    public static void unwatch(String key, ClientSession session) {
        Set<ClientSession> sessions = watchedClients.get(key);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                watchedClients.remove(key);
            }
        }
    }

    public static Set<String> getKeys() {
        return data.keySet();
    }

    private static String formatGetResponse(String value) {
        if (value == null) {
            return "$-1\r\n";
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return "$" + bytes.length + "\r\n" + value + "\r\n";
    }
}
