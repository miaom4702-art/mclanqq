package com.mclanqq.client;

import com.mclanqq.network.ChatBroadcastPacket;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 客户端聊天数据：按会话（大厅 / 某个玩家 UUID）保存消息与未读数。 */
public final class ClientChatManager {

    public static final String GROUP_KEY = "group";
    private static final int MAX_HISTORY = 200;

    public static final class Received {
        public final String key;
        public final boolean mine;
        public final String name;
        public final String text;
        public final boolean viewing;

        Received(String key, boolean mine, String name, String text, boolean viewing) {
            this.key = key;
            this.mine = mine;
            this.name = name;
            this.text = text;
            this.viewing = viewing;
        }
    }

    private static final Map<String, List<Message>> CONVERSATIONS = new LinkedHashMap<>();
    private static final Map<String, Integer> UNREAD = new LinkedHashMap<>();

    private ClientChatManager() {
    }

    public static String myUuid() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getStringUUID() : "";
    }

    public static Received receive(ChatBroadcastPacket packet) {
        String key;
        boolean mine;
        if (ChatBroadcastPacket.SCOPE_GROUP.equals(packet.scope)) {
            key = GROUP_KEY;
            mine = packet.fromUuid.equals(myUuid());
        } else {
            String me = myUuid();
            if (packet.fromUuid.equals(me)) {
                key = packet.targetUuid;
                mine = true;
            } else {
                key = packet.fromUuid;
                mine = false;
            }
        }
        List<Message> list = CONVERSATIONS.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(new Message(mine, packet.fromName, packet.text, packet.time));
        while (list.size() > MAX_HISTORY) {
            list.remove(0);
        }
        boolean viewing = ChatScreen.isViewing(key);
        if (!mine && !viewing) {
            UNREAD.merge(key, 1, Integer::sum);
        } else if (viewing) {
            UNREAD.remove(key);
        }
        return new Received(key, mine, packet.fromName, packet.text, viewing);
    }

    public static List<Message> get(String key) {
        return CONVERSATIONS.getOrDefault(key, Collections.emptyList());
    }

    public static int unread(String key) {
        return UNREAD.getOrDefault(key, 0);
    }

    public static void markRead(String key) {
        UNREAD.remove(key);
        NotificationManager.clearFor(key);
    }

    public static int totalUnread() {
        int sum = 0;
        for (int v : UNREAD.values()) {
            sum += v;
        }
        return sum;
    }

    public static void clearAll() {
        CONVERSATIONS.clear();
        UNREAD.clear();
    }
}
