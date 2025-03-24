package dev.dediamondpro.chatshot.compat;

import com.aizistral.nochatreports.common.config.NCRConfig;

public class NoChatReportsCompat implements ICompatHandler {
        //#if MC <= 12100

    //$$ @Override
    //$$ public int getButtonOffset() {
    //$$     return (NCRConfig.getClient().showServerSafety() && NCRConfig.getClient().enableMod() ? 25 : 0)
    //$$             + (NCRConfig.getEncryption().showEncryptionButton() ? 25 : 0);
    //$$ }
    //#endif
}
