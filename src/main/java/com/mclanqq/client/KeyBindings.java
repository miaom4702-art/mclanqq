package com.mclanqq.client;

import com.mclanqq.McLanQqMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = McLanQqMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class KeyBindings {

    public static final String CATEGORY = "key.categories.mclanqq";

    public static final KeyMapping OPEN_CHAT = new KeyMapping(
            "key.mclanqq.open", GLFW.GLFW_KEY_0, CATEGORY);

    private KeyBindings() {
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CHAT);
    }
}
