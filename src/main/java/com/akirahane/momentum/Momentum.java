package com.akirahane.momentum;

import com.akirahane.momentum.config.ClientConfig;
import com.akirahane.momentum.config.ServerConfig;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;

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
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        // 将延迟注册器注册到 mod 事件总线，以便物品被注册
        ITEMS.register(modEventBus);
        // 将物品注册到创造模式标签页
        modEventBus.addListener(this::addCreative);
    }

    // 将示例方块物品添加到战斗标签页
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(JET_BOOSTER_ITEM);
        }
    }
}
