package dev.ducky.utilities;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.config.ModConfig;

@Mod(DuckyUtilities.MOD_ID)
public final class DuckyUtilities {
    public static final String MOD_ID = "ducky_utilities";

    public DuckyUtilities() {
        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, UtilityConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new ClientFeatures());
    }
}
