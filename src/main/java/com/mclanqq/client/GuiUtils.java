package com.mclanqq.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/** 渲染工具：二次元像素风贴图、九宫格面板、Q 版头像。 */
public final class GuiUtils {

    public static final ResourceLocation PANEL = new ResourceLocation("mclanqq", "textures/gui/panel.png");
    public static final ResourceLocation BG = new ResourceLocation("mclanqq", "textures/gui/bg.png");
    public static final ResourceLocation MASCOT = new ResourceLocation("mclanqq", "textures/gui/mascot.png");
    public static final ResourceLocation STAR = new ResourceLocation("mclanqq", "textures/gui/star.png");
    public static final ResourceLocation HEART = new ResourceLocation("mclanqq", "textures/gui/heart.png");

    // 主题色
    public static final int PINK = 0xFFFF8FC7;
    public static final int PINK_SOFT = 0xFFFFC4E3;
    public static final int PURPLE = 0xF23B2F5C;
    public static final int PURPLE_HI = 0xFF5B4A86;
    public static final int PURPLE_LO = 0xFF2A2145;
    public static final int TEXT = 0xFFF3E9FF;
    public static final int TEXT_DIM = 0xFFBCA9DE;
    public static final int OWN_BUBBLE = 0xE6FF8FC7;
    public static final int OTHER_BUBBLE = 0xE642375F;
    public static final int HOVER = 0xFF4A3C72;

    private static final Map<String, int[]> AVATAR_CACHE = new HashMap<>();

    private GuiUtils() {
    }

    private static void tint(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float gg = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        RenderSystem.setShaderColor(r, gg, b, a);
    }

    /** 九宫格面板，带颜色。 */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, int argb) {
        if (w <= 0 || h <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        tint(argb);
        nine(g, PANEL, x, y, w, h, 6, 24);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    public static void nine(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h, int b, int ts) {
        if (w < b * 2 + 1 || h < b * 2 + 1) {
            // 太小就直接整块拉伸，避免出现负尺寸
            g.blit(tex, x, y, w, h, 0f, 0f, ts, ts, ts, ts);
            return;
        }
        int iw = w - b * 2;
        int ih = h - b * 2;
        int c = ts - b * 2;
        g.blit(tex, x, y, b, b, 0f, 0f, b, b, ts, ts);
        g.blit(tex, x + w - b, y, b, b, ts - b, 0f, b, b, ts, ts);
        g.blit(tex, x, y + h - b, b, b, 0f, ts - b, b, b, ts, ts);
        g.blit(tex, x + w - b, y + h - b, b, b, ts - b, ts - b, b, b, ts, ts);
        g.blit(tex, x + b, y, iw, b, b, 0f, c, b, ts, ts);
        g.blit(tex, x + b, y + h - b, iw, b, b, ts - b, c, b, ts, ts);
        g.blit(tex, x, y + b, b, ih, 0f, b, b, c, ts, ts);
        g.blit(tex, x + w - b, y + b, b, ih, ts - b, b, b, c, ts, ts);
        g.blit(tex, x + b, y + b, iw, ih, b, b, c, c, ts, ts);
    }

    /** 平铺樱花夜色背景。 */
    public static void background(GuiGraphics g, int x, int y, int w, int h) {
        g.blitRepeating(BG, x, y, w, h, 0, 0, 64, 64);
    }

    // ------------------------------------------------------------------ 头像

    /** 昵称对应的色相（0~359）。 */
    public static int hue(String name) {
        return hash(name == null ? "?" : name) % 360;
    }

    /** 昵称文字颜色（亮色，用于名字）。 */
    public static int nameColor(String name) {
        return hsl(hue(name), 0.85f, 0.78f);
    }

    /** 昵称气泡底色（较深，保证白字可读），带透明度。 */
    public static int bubbleColor(String name) {
        return 0xE6000000 | (hsl(hue(name), 0.45f, 0.34f) & 0xFFFFFF);
    }

    public static void avatar(GuiGraphics g, String name, int x, int y, int size) {
        int[] px = AVATAR_CACHE.computeIfAbsent(name == null ? "?" : name, GuiUtils::computeAvatar);
        int cell = Math.max(1, size / 8);
        for (int py = 0; py < 8; py++) {
            for (int pxi = 0; pxi < 8; pxi++) {
                int col = px[py * 8 + pxi];
                if ((col >>> 24) == 0) {
                    continue;
                }
                int px0 = x + pxi * cell;
                int py0 = y + py * cell;
                g.fill(px0, py0, px0 + cell, py0 + cell, col);
            }
        }
    }

    private static int[] computeAvatar(String name) {
        int hash = hash(name);
        int hue = hash % 360;
        int[] skins = {0xFFFFD9B3, 0xFFFFC9A0, 0xFFF2B98C, 0xFFE8A87C};
        int skin = skins[(hash >>> 8) % skins.length];
        int hair = hsl(hue, 0.55f, 0.55f);
        int hairDark = hsl(hue, 0.55f, 0.38f);
        int eye = 0xFF2A2140;
        int eyeShine = 0xFFFFFFFF;
        int blush = 0xFFFF9EC4;
        int mouth = 0xFFB0568A;
        boolean catEars = ((hash >>> 16) & 1) == 0;

        int[] out = new int[64];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int col = skin;
                if (y < 2) {
                    col = hair;
                } else if (x == 0 || x == 7) {
                    col = y <= 5 ? hair : skin;
                } else if (y == 3 && (x == 2 || x == 5)) {
                    col = eye;
                } else if (y == 4 && (x == 2 || x == 5)) {
                    col = eye;
                } else if (y == 3 && (x == 3 || x == 6)) {
                    col = eyeShine;
                } else if (y == 5 && (x == 1 || x == 6)) {
                    col = blush;
                } else if (y == 6 && (x == 3 || x == 4)) {
                    col = mouth;
                } else if (y == 7 && x == 0) {
                    col = hairDark;
                }
                out[y * 8 + x] = col;
            }
        }
        if (catEars) {
            out[0 * 8 + 1] = hair;
            out[0 * 8 + 2] = hairDark;
            out[0 * 8 + 5] = hair;
            out[0 * 8 + 6] = hairDark;
        }
        return out;
    }

    private static int hash(String s) {
        int h = 0x811c9dc5;
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x01000193;
        }
        return h & 0x7fffffff;
    }

    private static int hsl(int h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float hp = h / 60f;
        float x = c * (1 - Math.abs(hp % 2 - 1));
        float r = 0, gg = 0, b = 0;
        if (hp < 1) {
            r = c; gg = x;
        } else if (hp < 2) {
            r = x; gg = c;
        } else if (hp < 3) {
            gg = c; b = x;
        } else if (hp < 4) {
            gg = x; b = c;
        } else if (hp < 5) {
            r = x; b = c;
        } else {
            r = c; b = x;
        }
        float m = l - c / 2;
        int ri = Math.round((r + m) * 255);
        int gi = Math.round((gg + m) * 255);
        int bi = Math.round((b + m) * 255);
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }
}
