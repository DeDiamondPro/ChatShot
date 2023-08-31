package dev.dediamondpro.chatshot.compat;

import java.util.ArrayList;
import java.util.function.Supplier;

public class CompatHandler {
    private static final ArrayList<ICompat> compatHandlers = new ArrayList<>();

    static {
        ArrayList<Supplier<ICompat>> compatHandlerFactories = new ArrayList<>() {{
            add(ImmediatelyFastCompat::new);
        }};

        for (Supplier<ICompat> handler : compatHandlerFactories) {
            try {
                compatHandlers.add(handler.get());
            } catch (Throwable ignored) {
            }
        }
    }

    public static void beforeScreenShot() {
        for (ICompat handler : compatHandlers) {
            try {
                handler.beforeScreenshot();
            } catch (NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError ignored) {
                compatHandlers.remove(handler);
            } catch (Throwable ignored) {
            }
        }
    }

    public static void afterScreenShot() {
        for (ICompat handler : compatHandlers) {
            try {
                handler.afterScreenshot();
            } catch (NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError ignored) {
                compatHandlers.remove(handler);
            } catch (Throwable ignored) {
            }
        }
    }
}
