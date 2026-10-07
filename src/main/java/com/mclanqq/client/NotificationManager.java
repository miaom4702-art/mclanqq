package com.mclanqq.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** 新消息通知队列（用于屏幕右上角弹窗）。 */
public final class NotificationManager {

    public static final long DURATION = 6500L;
    private static final int MAX = 6;

    public static final class Notif {
        public final String name;
        public final String text;
        public final String key;
        public final long time;

        Notif(String name, String text, String key, long time) {
            this.name = name;
            this.text = text;
            this.key = key;
            this.time = time;
        }
    }

    private static final Deque<Notif> LIST = new ArrayDeque<>();

    private NotificationManager() {
    }

    public static void push(String name, String text, String key) {
        LIST.addLast(new Notif(name, text, key, System.currentTimeMillis()));
        while (LIST.size() > MAX) {
            LIST.removeFirst();
        }
    }

    public static void clearFor(String key) {
        LIST.removeIf(n -> n.key.equals(key));
    }

    public static boolean isEmpty() {
        return LIST.isEmpty();
    }

    /** 返回当前仍在展示期内的通知（最多 4 条）。 */
    public static List<Notif> active(long now) {
        if (LIST.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Notif> out = new ArrayList<>(4);
        java.util.Iterator<Notif> it = LIST.descendingIterator();
        while (it.hasNext()) {
            Notif n = it.next();
            if (now - n.time > DURATION) {
                break;
            }
            out.add(0, n);
            if (out.size() >= 4) {
                break;
            }
        }
        return out;
    }
}
