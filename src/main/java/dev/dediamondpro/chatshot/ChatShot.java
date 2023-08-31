package dev.dediamondpro.chatshot;

//#if FORGE==1
//$$ import dev.dediamondpro.chatshot.config.ConfigHelper;
//$$ import net.minecraftforge.client.ConfigScreenHandler;
//$$ import net.minecraftforge.fml.ModList;
//$$ import net.minecraftforge.fml.ModLoadingContext;
//$$ import net.minecraftforge.fml.common.Mod;
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
//#endif
