package com.akirahane.momentum;

import net.minecraft.world.item.Rarity;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 此处的值应与 META-INF/neoforge.mods.toml 文件中的条目匹配
@Mod(Momentum.MODID)
public class Momentum {
    public static final String MODID = "momentum";
    public static final Logger LOGGER = LogUtils.getLogger();
    // 创建一个延迟注册器来持有物品，所有物品都将注册在 "momentum" 命名空间下
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 注册物品
    public static final DeferredItem<@NotNull Item> JET_BOOSTER_ITEM = ITEMS.registerSimpleItem(
            "jet_booster",
            p -> p.stacksTo(1) // 设置物品可堆叠1
                    .rarity(Rarity.RARE)
    );

    // mod 类的构造函数是 mod 加载时运行的第一段代码。
    // FML 会识别某些参数类型（如 IEventBus 或 ModContainer）并自动传入。
    public Momentum(IEventBus modEventBus, ModContainer modContainer) {
        // 注册我们 mod 的 ModConfigSpec，以便 FML 可以为我们创建和加载配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        // 将延迟注册器注册到 mod 事件总线，以便物品被注册
        ITEMS.register(modEventBus);

        // 注册自身以接收服务器事件和其他游戏事件。
        // 注意：仅当我们希望 *此类*（Momentum）直接响应事件时才需要这一行。
        // 如果此类中没有使用 @SubscribeEvent 注解的方法（如下面的 onServerStarting()），请不要添加这一行。
//        NeoForge.EVENT_BUS.register(this);

        // 将物品注册到创造模式标签页
        modEventBus.addListener(this::addCreative);
    }

    // 将示例方块物品添加到建筑方块标签页
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(JET_BOOSTER_ITEM);
        }
    }
}
