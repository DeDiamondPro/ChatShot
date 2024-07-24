package dev.dediamondpro.chatshot;

//#if FORGE == 1
//$$ import net.minecraftforge.client.ConfigScreenHandler;
//$$ import net.minecraftforge.fml.ModList;
//$$ import net.minecraftforge.fml.ModLoadingContext;
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import dev.dediamondpro.chatshot.config.ConfigHelper;
//$$
//$$ @Mod("chatshot")
//$$ public class ChatShot {
//$$     public ChatShot() {
//$$         if (ModList.get().isLoaded("yet_another_config_lib_v3")) ModLoadingContext.get().registerExtensionPoint(
//$$                 ConfigScreenHandler.ConfigScreenFactory.class,
//$$                 () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> ConfigHelper.createScreen(parent))
//$$         );
//$$     }
//$$ }
//#elseif NEOFORGE == 1
//$$ import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
//$$ import net.neoforged.fml.ModList;
//$$ import net.neoforged.fml.ModLoadingContext;
//$$ import net.neoforged.fml.common.Mod;
//$$ import dev.dediamondpro.chatshot.config.ConfigHelper;
//$$
//$$ @Mod("chatshot")
//$$ public class ChatShot {
//$$     public ChatShot() {
//$$         if (ModList.get().isLoaded("yet_another_config_lib_v3")) ModLoadingContext.get().registerExtensionPoint(
//$$                 IConfigScreenFactory.class,
//$$                 () -> (client, parent) -> ConfigHelper.createScreen(parent)
//$$         );
//$$     }
//$$ }
//#endif
