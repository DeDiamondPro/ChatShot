package dev.dediamondpro.chatshot.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.impl.controller.TickBoxControllerBuilderImpl;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ConfigHelper {
    public static Screen createScreen(@Nullable net.minecraft.client.gui.screens.Screen parent) {
        Config config = Config.INSTANCE;
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("chatshot.config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("chatshot.config"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("chatshot.tooltip"))
                                .description(OptionDescription.of(Component.translatable("chatshot.tooltip.description")))
                                .binding(true, () -> config.tooltip, it -> config.tooltip = it)
                                .controller(TickBoxControllerBuilderImpl::new)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("chatshot.saveImage"))
                                .description(OptionDescription.of(Component.translatable("chatshot.saveImage.description")))
                                .binding(true, () -> config.saveImage, it -> config.saveImage = it)
                                .controller(TickBoxControllerBuilderImpl::new)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("chatshot.copyMessage"))
                                .description(OptionDescription.of(Component.translatable("chatshot.copyMessage.description")))
                                .binding(true, () -> config.showCopyMessage, it -> config.showCopyMessage = it)
                                .controller(TickBoxControllerBuilderImpl::new)
                                .build())
                        .option(Option.<Config.CopyType>createBuilder()
                                .name(Component.translatable("chatshot.clickAction"))
                                .description(OptionDescription.of(Component.translatable("chatshot.clickAction.description")))
                                .binding(Config.CopyType.TEXT, () -> config.clickAction, it -> config.clickAction = it)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(Config.CopyType.class)
                                        .valueFormatter(it -> Component.translatable("chatshot.copyOption." + it.name().toLowerCase())))
                                .build())
                        .option(Option.<Config.CopyType>createBuilder()
                                .name(Component.translatable("chatshot.shiftClickAction"))
                                .description(OptionDescription.of(Component.translatable("chatshot.shiftClickAction.description")))
                                .binding(Config.CopyType.IMAGE, () -> config.shiftClickAction, it -> config.shiftClickAction = it)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(Config.CopyType.class)
                                        .valueFormatter(it -> Component.translatable("chatshot.copyOption." + it.name().toLowerCase())))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("chatshot.shadow"))
                                .description(OptionDescription.of(Component.translatable("chatshot.shadow.description")))
                                .binding(false, () -> config.shadow, it -> config.shadow = it)
                                .controller(TickBoxControllerBuilderImpl::new)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("chatshot.scale"))
                                .description(OptionDescription.of(Component.translatable("chatshot.scale.description")))
                                .binding(2, () -> config.scale, it -> config.scale = it)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
                                .build())
                        .build()
                ).save(Config::save).build().generateScreen(parent);
    }
}
