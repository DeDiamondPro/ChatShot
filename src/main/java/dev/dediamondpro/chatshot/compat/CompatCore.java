package dev.dediamondpro.chatshot.compat;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class CompatCore {
    private static final ConcurrentLinkedDeque<ICompatHandler> compatHandlers = new ConcurrentLinkedDeque<>();

    static {
        ArrayList<Supplier<ICompatHandler>> compatHandlerFactories = new ArrayList<>() {{
            add(NoChatReportsCompat::new);
        }};

        for (Supplier<ICompatHandler> handler : compatHandlerFactories) {
            try {
                compatHandlers.add(handler.get());
            } catch (Throwable ignored) {
            }
        }
    }

    public static int getButtonOffset() {
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
}
