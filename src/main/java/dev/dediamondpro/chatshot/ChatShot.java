package dev.dediamondpro.chatshot;

//#if FORGELIKE==1
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
//$$                 //#if NEOFORGE == 1
//$$                 //$$ IConfigScreenFactory.class,
//$$                 //$$ () -> (client, parent) -> ConfigHelper.createScreen(parent)
//$$                 //#else
//$$                 ConfigScreenHandler.ConfigScreenFactory.class,
//$$                 () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> ConfigHelper.createScreen(parent))
//$$                 //#endif
//$$         );
//$$     }
//$$ }
//#endif
