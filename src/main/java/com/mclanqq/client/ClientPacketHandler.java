package com.mclanqq.client;

import com.mclanqq.network.ChatBroadcastPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/** 客户端网络包分发（只在物理客户端加载）。 */
public final class ClientPacketHandler {

    private ClientPacketHandler() {
    }

    public static void handle(ChatBroadcastPacket packet) {
        ClientChatManager.Received r = ClientChatManager.receive(packet);
        if (r.mine || r.viewing) {
            return;
        }
        NotificationManager.push(r.name, r.text, r.key);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.4f, 0.7f));
        }
    }
}
