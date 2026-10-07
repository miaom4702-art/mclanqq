package com.mclanqq.client;

import com.mclanqq.McLanQqMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = McLanQqMod.MODID, value = Dist.CLIENT)
public final class ClientInputHandler {

    private ClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        while (KeyBindings.OPEN_CHAT.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                mc.setScreen(new ChatScreen());
            }
        }
    }
}
