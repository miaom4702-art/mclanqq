package com.mclanqq.client;

import com.mclanqq.network.ChatSendPacket;
import com.mclanqq.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** 二次元像素风联机聊天界面（默认数字 0 打开）。 */
public class ChatScreen extends Screen {

    private static final int LX = 6;
    private static final int LW = 156;
    private static final int RX = LX + LW + 6;

    private EditBox input;
    private String selected = ClientChatManager.GROUP_KEY;
    private final List<Entry> entries = new ArrayList<>();

    public ChatScreen() {
        super(Component.literal("MC 联机 QQ"));
    }

    /** 供数据层判断“当前是否正看着某个会话”。 */
    public static boolean isViewing(String key) {
        Minecraft mc = Minecraft.getInstance();
        return mc.screen instanceof ChatScreen cs && cs.selected.equals(key);
    }

    private static final class Entry {
        final String key;
        final String label;
        final int y;

        Entry(String key, String label, int y) {
            this.key = key;
            this.label = label;
            this.y = y;
        }
    }

    @Override
    protected void init() {
        int rw = this.width - RX - 6;
        this.input = new EditBox(this.font, RX + 16, this.height - 30, rw - 32, 16, Component.literal("消息"));
        this.input.setBordered(false);
        this.input.setMaxLength(256);
        this.input.setTextColor(0xFFF3E9FF);
        this.input.setSuggestion("输入消息，Enter 发送…");
        this.addRenderableWidget(this.input);
        this.setInitialFocus(this.input);
        ClientChatManager.markRead(this.selected);
    }

    private String nameFor(String uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                if (info.getProfile().getId().toString().equals(uuid)) {
                    return info.getProfile().getName();
                }
            }
        }
        return uuid.length() >= 8 ? "玩家-" + uuid.substring(0, 8) : uuid;
    }

    private void openConversation(String key) {
        this.selected = key;
        ClientChatManager.markRead(key);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int W = this.width;
        int H = this.height;
        GuiUtils.background(g, 0, 0, W, H);

        // ---------- 左侧 ----------
        int lh = H - 12;
        GuiUtils.panel(g, LX, 6, LW, lh, GuiUtils.PURPLE);
        GuiUtils.panel(g, LX, 6, LW, 38, GuiUtils.PURPLE_HI);
        g.blit(GuiUtils.MASCOT, LX + 6, 17, 16, 16, 0f, 0f, 16, 16, 16, 16);
        g.drawString(this.font, "联机 QQ", LX + 28, 11, GuiUtils.PINK, false);
        g.drawString(this.font, "MC LAN QQ", LX + 28, 23, GuiUtils.TEXT_DIM, false);

        this.entries.clear();
        int y = 50;
        this.entries.add(new Entry(ClientChatManager.GROUP_KEY, "联机大厅", y));
        y += 20;
        Minecraft mc = Minecraft.getInstance();
        String me = ClientChatManager.myUuid();
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                String uuid = info.getProfile().getId().toString();
                if (uuid.equals(me)) {
                    continue;
                }
                this.entries.add(new Entry(uuid, info.getProfile().getName(), y));
                y += 20;
                if (y > 6 + lh - 22) {
                    break;
                }
            }
        }
        for (Entry e : this.entries) {
            boolean active = e.key.equals(this.selected);
            boolean hover = mouseX >= LX + 3 && mouseX <= LX + LW - 3 && mouseY >= e.y && mouseY <= e.y + 18;
            if (active) {
                GuiUtils.panel(g, LX + 3, e.y, LW - 6, 18, 0xE6FF8FC7);
            } else if (hover) {
                GuiUtils.panel(g, LX + 3, e.y, LW - 6, 18, GuiUtils.HOVER);
            }
            boolean group = e.key.equals(ClientChatManager.GROUP_KEY);
            if (group) {
                g.blit(GuiUtils.STAR, LX + 8, e.y + 5, 8, 8, 0f, 0f, 8, 8, 8, 8);
            } else {
                GuiUtils.avatar(g, e.label, LX + 7, e.y + 1, 16);
            }
            g.drawString(this.font, e.label, LX + 28, e.y + 5, active ? 0xFF3A1B2E : GuiUtils.TEXT, false);
            int u = ClientChatManager.unread(e.key);
            if (u > 0) {
                String s = u > 99 ? "99+" : String.valueOf(u);
                int bw = this.font.width(s) + 8;
                int bx = LX + LW - 7 - bw;
                GuiUtils.panel(g, bx, e.y + 3, bw, 13, 0xFFFF5AA8);
                g.drawString(this.font, s, bx + 4, e.y + 5, 0xFFFFFFFF, false);
            }
        }

        // ---------- 右侧 ----------
        int rw = W - RX - 6;
        GuiUtils.panel(g, RX, 6, rw, lh, GuiUtils.PURPLE);
        GuiUtils.panel(g, RX, 6, rw, 38, GuiUtils.PURPLE_HI);
        boolean group = this.selected.equals(ClientChatManager.GROUP_KEY);
        String title = group ? "联机大厅" : nameFor(this.selected);
        g.drawString(this.font, title, RX + 12, 11, GuiUtils.PINK, false);
        int online = mc.getConnection() != null ? mc.getConnection().getOnlinePlayers().size() : 0;
        g.drawString(this.font, group ? ("在线 " + online + " 人") : "私聊 · 在线 " + online,
                RX + 12, 23, GuiUtils.TEXT_DIM, false);
        g.blit(GuiUtils.HEART, RX + rw - 18, 14, 8, 8, 0f, 0f, 8, 8, 8, 8);

        // 消息区
        int areaTop = 50;
        int areaBottom = H - 44;
        int areaH = areaBottom - areaTop;
        int left = RX + 14;
        int right = RX + rw - 14;
        int contentW = right - left;
        int avatarW = 16;
        int gap = 6;
        int maxBubble = contentW - avatarW - gap - 4;
        List<Message> msgs = ClientChatManager.get(this.selected);
        if (msgs.isEmpty()) {
            g.drawString(this.font, "还没有消息，来打个招呼吧～", left, areaTop + 4, GuiUtils.TEXT_DIM, false);
        } else {
            int n = msgs.size();
            int[] bh = new int[n];
            int[] bw = new int[n];
            List<List<FormattedCharSequence>> wrap = new ArrayList<>(n);
            int total = 0;
            for (int i = 0; i < n; i++) {
                Message m = msgs.get(i);
                List<FormattedCharSequence> lines = this.font.split(Component.literal(m.text), maxBubble - 14);
                wrap.add(lines);
                int tw = 0;
                for (FormattedCharSequence seq : lines) {
                    tw = Math.max(tw, this.font.width(seq));
                }
                bw[i] = Math.min(maxBubble, tw + 14);
                bh[i] = lines.size() * 10 + 8 + 11;
                total += bh[i] + 4;
            }
            int start = 0;
            while (start < n && total > areaH) {
                total -= bh[start] + 4;
                start++;
            }
            int cy = areaTop;
            for (int i = start; i < n; i++) {
                Message m = msgs.get(i);
                List<FormattedCharSequence> lines = wrap.get(i);
                int avX;
                int bubbleX;
                if (m.mine) {
                    avX = right - avatarW;
                    bubbleX = right - avatarW - gap - bw[i];
                } else {
                    avX = left;
                    bubbleX = left + avatarW + gap;
                }
                String label = m.mine ? "我" : m.fromName;
                g.drawString(this.font, label,
                        m.mine ? bubbleX + bw[i] - this.font.width(label) : bubbleX,
                        cy, m.mine ? GuiUtils.PINK_SOFT : GuiUtils.nameColor(m.fromName), false);
                GuiUtils.avatar(g, m.fromName, avX, cy + 10, avatarW);
                GuiUtils.panel(g, bubbleX, cy + 10, bw[i], lines.size() * 10 + 8,
                        m.mine ? GuiUtils.OWN_BUBBLE : GuiUtils.bubbleColor(m.fromName));
                int ty = cy + 14;
                for (FormattedCharSequence seq : lines) {
                    g.drawString(this.font, seq, bubbleX + 7, ty, m.mine ? 0xFF3A1B2E : GuiUtils.TEXT, false);
                    ty += 10;
                }
                cy += bh[i] + 4;
            }
        }

        // 输入区
        GuiUtils.panel(g, RX + 6, H - 38, rw - 12, 30, GuiUtils.PURPLE_LO);
        g.drawString(this.font, "Enter 发送 · 点左侧玩家可私聊", RX + 12, H - 40, GuiUtils.TEXT_DIM, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void sendCurrent() {
        String text = this.input.getValue().trim();
        if (text.isEmpty()) {
            return;
        }
        NetworkHandler.sendToServer(new ChatSendPacket(this.selected, text));
        this.input.setValue("");
        this.input.setSuggestion("输入消息，Enter 发送…");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            sendCurrent();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Entry e : this.entries) {
            if (mouseX >= LX + 3 && mouseX <= LX + LW - 3 && mouseY >= e.y && mouseY <= e.y + 18) {
                openConversation(e.key);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
