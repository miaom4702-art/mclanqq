package com.mclanqq.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 客户端 -> 服务端：发送一条消息（大厅或私聊）。 */
public class ChatSendPacket {

    public static final String GROUP = "group";

    private final String target;
    private final String text;

    public ChatSendPacket(String target, String text) {
        this.target = target == null ? GROUP : target;
        this.text = text;
    }

    public static void encode(ChatSendPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.target, 64);
        buf.writeUtf(msg.text, 512);
    }

    public static ChatSendPacket decode(FriendlyByteBuf buf) {
        return new ChatSendPacket(buf.readUtf(64), buf.readUtf(512));
    }

    public static void handle(ChatSendPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }
            MinecraftServer server = sender.getServer();
            if (server == null) {
                return;
            }
            String text = msg.text == null ? "" : msg.text.trim();
            if (text.isEmpty()) {
                return;
            }
            if (text.length() > 256) {
                text = text.substring(0, 256);
            }
            long time = System.currentTimeMillis();
            String fromUuid = sender.getStringUUID();
            String fromName = sender.getName().getString();

            if (GROUP.equals(msg.target)) {
                ChatBroadcastPacket out = new ChatBroadcastPacket(
                        ChatBroadcastPacket.SCOPE_GROUP, fromUuid, fromName, "", text, time);
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    NetworkHandler.sendToPlayer(p, out);
                }
            } else {
                ServerPlayer target = null;
                try {
                    target = server.getPlayerList().getPlayer(UUID.fromString(msg.target));
                } catch (IllegalArgumentException ignored) {
                }
                NetworkHandler.sendToPlayer(sender, new ChatBroadcastPacket(
                        ChatBroadcastPacket.SCOPE_DM, fromUuid, fromName, msg.target, text, time));
                if (target != null && target != sender) {
                    NetworkHandler.sendToPlayer(target, new ChatBroadcastPacket(
                            ChatBroadcastPacket.SCOPE_DM, fromUuid, fromName, msg.target, text, time));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
