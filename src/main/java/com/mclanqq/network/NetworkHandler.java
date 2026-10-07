package com.mclanqq.network;

import com.mclanqq.McLanQqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {

    private static final String PROTOCOL = "1";
    private static int nextId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(McLanQqMod.MODID, "main"),
            () -> PROTOCOL,
            NetworkRegistry.acceptMissingOr(PROTOCOL),
            NetworkRegistry.acceptMissingOr(PROTOCOL));

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(nextId++, ChatSendPacket.class,
                ChatSendPacket::encode, ChatSendPacket::decode, ChatSendPacket::handle);
        CHANNEL.registerMessage(nextId++, ChatBroadcastPacket.class,
                ChatBroadcastPacket::encode, ChatBroadcastPacket::decode, ChatBroadcastPacket::handle);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    /** 只发给“也装了这个模组”的玩家，避免影响原版玩家。 */
    public static void sendToPlayer(ServerPlayer player, Object message) {
        if (player.connection != null && CHANNEL.isRemotePresent(player.connection.connection)) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }
}
