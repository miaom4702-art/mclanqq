package com.mclanqq.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

/** 屏幕右上角的新消息弹窗 + 底部提示。无消息时零开销。 */
public class NotificationOverlay implements IGuiOverlay {

    public static final NotificationOverlay INSTANCE = new NotificationOverlay();

    private static final int CARD_W = 196;
    private static final int CARD_H = 38;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.font == null || mc.screen instanceof ChatScreen) {
            return;
        }
        int total = ClientChatManager.totalUnread();
        if (total <= 0 && NotificationManager.isEmpty()) {
            return; // 正常游玩时几乎零开销
        }

        // 底部居中提示
        if (total > 0) {
            String hint = "按 [0] 打开联机 QQ · " + total + " 条新消息";
            int w = mc.font.width(hint) + 18;
            int x = (screenWidth - w) / 2;
            int y = screenHeight - 62;
            GuiUtils.panel(g, x, y, w, 18, 0xE6FF8FC7);
            g.drawString(mc.font, hint, x + 9, y + 5, 0xFF3A1B2E, false);
        }

        List<NotificationManager.Notif> act = NotificationManager.active(System.currentTimeMillis());
        if (act.isEmpty()) {
            return;
        }
        // 从下往上堆叠
        int x = screenWidth - CARD_W - 6;
        int baseY = 6;
        for (int i = 0; i < act.size(); i++) {
            NotificationManager.Notif n = act.get(i);
            long age = System.currentTimeMillis() - n.time;
            float fade = Math.min(1f, age / 200f) * Math.min(1f, (NotificationManager.DURATION - age) / 400f);
            int alpha = (int) (Math.max(0f, Math.min(1f, fade)) * 235);
            if (alpha <= 4) {
                continue;
            }
            int y = baseY + i * (CARD_H + 4);
            GuiUtils.panel(g, x, y, CARD_W, CARD_H, (alpha << 24) | 0x4A3C72);
            // 悬浮一点点的入场位移
            int slide = (int) ((1f - Math.min(1f, age / 200f)) * 24);
            g.blit(GuiUtils.MASCOT, x + 6 + slide, y + 11, 16, 16, 0f, 0f, 16, 16, 16, 16);
            g.drawString(mc.font, n.name, x + 28 + slide, y + 6, (alpha << 24) | (GuiUtils.PINK & 0xFFFFFF), false);
            String text = mc.font.plainSubstrByWidth(n.text, CARD_W - 40);
            g.drawString(mc.font, text, x + 28 + slide, y + 20, (alpha << 24) | 0xF3E9FF, false);
        }
    }
}
