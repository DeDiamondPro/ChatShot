package dev.dediamondpro.chatshot.compat;

import com.aizistral.nochatreports.common.config.NCRConfig;

public class NoChatReportsCompat implements ICompatHandler {
    @Override
    public int getButtonOffset() {
        return (NCRConfig.getClient().showServerSafety() && NCRConfig.getClient().enableMod() ? 25 : 0)
                //?if <1.21.5
                + (NCRConfig.getEncryption().showEncryptionButton() ? 25 : 0)
                ;
    }
}
