package dev.dediamondpro.chatshot.compat;

import net.raphimc.immediatelyfastapi.BatchingAccess;
import net.raphimc.immediatelyfastapi.ImmediatelyFastApi;

public class ImmediatelyFastCompat implements ICompat {
    private final BatchingAccess batching = ImmediatelyFastApi.getApiImpl().getBatching();
    private boolean isBatching = false;


    @Override
    public void beforeScreenshot() {
        isBatching = batching.isHudBatching();
        if (isBatching) batching.endHudBatching();
    }

    @Override
    public void afterScreenshot() {
        if (isBatching) batching.beginHudBatching();
        isBatching = false;
    }
}
