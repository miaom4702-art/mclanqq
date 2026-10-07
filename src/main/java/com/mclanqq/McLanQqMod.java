package com.mclanqq;

import com.mclanqq.network.NetworkHandler;
import net.minecraftforge.fml.common.Mod;

/**
 * MC LAN QQ - 一个“客户端可选”的 QQ 风格联机聊天模组。
 *
 * 大厅群聊与玩家私聊都通过 Forge 网络通道在服务器端中转，
 * 未安装本模组的玩家依然可以正常进入安装了本模组的房间。
 */
@Mod(McLanQqMod.MODID)
public class McLanQqMod {

    public static final String MODID = "mclanqq";

    public McLanQqMod() {
        NetworkHandler.register();
    }
}
