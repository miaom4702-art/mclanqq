package com.mclanqq.client;

import com.mclanqq.McLanQqMod;
import com.mclanqq.network.ChatBroadcastPacket;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/** 单人也能测试的客户端指令：/mcqq test */
@Mod.EventBusSubscriber(modid = McLanQqMod.MODID, value = Dist.CLIENT)
public final class ClientCommands {

    private static final String[] NAMES = {"小樱", "阿狸", "测试玩家", "猫娘A", "路人甲"};
    private static final Random RANDOM = new Random();

    private ClientCommands() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("mcqq")
                .then(Commands.literal("test").executes(ctx -> {
                    String name = NAMES[RANDOM.nextInt(NAMES.length)];
                    String text = "这是一条测试消息 #" + (RANDOM.nextInt(900) + 100);
                    ChatBroadcastPacket fake = new ChatBroadcastPacket(
                            ChatBroadcastPacket.SCOPE_GROUP,
                            "fake-uuid-" + name, name, "",
                            text, System.currentTimeMillis());
                    ClientPacketHandler.handle(fake);
                    ctx.getSource().sendSuccess(
                            () -> Component.literal("已模拟收到一条消息，注意右上角弹窗和提示音"), false);
                    return 1;
                }))
                .then(Commands.literal("testdm").executes(ctx -> {
                    String name = NAMES[RANDOM.nextInt(NAMES.length)];
                    String text = "私聊测试消息 #" + (RANDOM.nextInt(900) + 100);
                    ChatBroadcastPacket fake = new ChatBroadcastPacket(
                            ChatBroadcastPacket.SCOPE_DM,
                            "fake-uuid-" + name, name, "",
                            text, System.currentTimeMillis());
                    ClientPacketHandler.handle(fake);
                    ctx.getSource().sendSuccess(
                            () -> Component.literal("已模拟收到一条私聊消息"), false);
                    return 1;
                }));
        event.getDispatcher().register(root);
    }
}
