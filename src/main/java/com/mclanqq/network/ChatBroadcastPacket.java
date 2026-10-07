package com.mclanqq.network;

import com.mclanqq.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 服务端 -> 客户端：把一条消息广播/私发给客户端。 */
public class ChatBroadcastPacket {

    public static final String SCOPE_GROUP = "group";
    public static final String SCOPE_DM = "dm";

    public final String scope;
    public final String fromUuid;
    public final String fromName;
    public final String targetUuid;
    public final String text;
    public final long time;

    public ChatBroadcastPacket(String scope, String fromUuid, String fromName,
                               String targetUuid, String text, long time) {
        this.scope = scope;
        this.fromUuid = fromUuid;
        this.fromName = fromName;
        this.targetUuid = targetUuid == null ? "" : targetUuid;
        this.text = text;
        this.time = time;
    }

    public static void encode(ChatBroadcastPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.scope, 16);
        buf.writeUtf(msg.fromUuid, 64);
        buf.writeUtf(msg.fromName, 64);
        buf.writeUtf(msg.targetUuid, 64);
        buf.writeUtf(msg.text, 512);
        buf.writeLong(msg.time);
    }

    public static ChatBroadcastPacket decode(FriendlyByteBuf buf) {
        return new ChatBroadcastPacket(
                buf.readUtf(16), buf.readUtf(64), buf.readUtf(64),
                buf.readUtf(64), buf.readUtf(512), buf.readLong());
    }

    public static void handle(ChatBroadcastPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandler.handle(msg)));
        ctx.setPacketHandled(true);
    }
}
