package de.ellpeck.naturesaura.compat;

import de.ellpeck.naturesaura.compat.crafttweaker.CraftTweakerCompat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

public final class Compat {

    public static boolean baubles;
    public static boolean craftTweaker;

    public static void preInit() {
        baubles = Loader.isModLoaded("baubles");
        craftTweaker = Loader.isModLoaded("crafttweaker");

        if (baubles)
            MinecraftForge.EVENT_BUS.register(new BaublesCompat());
    }

    public static void postInit() {
        if (craftTweaker)
            CraftTweakerCompat.postInit();
    }
}
