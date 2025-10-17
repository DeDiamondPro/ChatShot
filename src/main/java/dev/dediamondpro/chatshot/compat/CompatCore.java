package dev.dediamondpro.chatshot.compat;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class CompatCore {
    public static final CompatCore INSTANCE = new CompatCore();
    private final ConcurrentLinkedDeque<ICompatHandler> compatHandlers = new ConcurrentLinkedDeque<>();

    private CompatCore() {
        ArrayList<Supplier<ICompatHandler>> compatHandlerFactories = new ArrayList<>() {{
            //? if ncr
            add(NoChatReportsCompat::new);
            // add(ImmediatelyFastCompat::new);
        }};

        for (Supplier<ICompatHandler> handler : compatHandlerFactories) {
            try {
                compatHandlers.add(handler.get());
            } catch (Throwable ignored) {
            }
        }
    }

    public int getButtonOffset() {
        int offset = 0;
        for (ICompatHandler handler : compatHandlers) {
            try {
                offset = Math.max(offset, handler.getButtonOffset());
            } catch (NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError ignored) {
                compatHandlers.remove(handler);
            } catch (Throwable ignored) {
            }
        }
        return offset;
    }

    public void drawChatHud() {
        for (ICompatHandler handler : compatHandlers) {
            try {
                handler.drawChatHud();
            } catch (NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError ignored) {
                compatHandlers.remove(handler);
            } catch (Throwable ignored) {
            }
        }
    }
}
