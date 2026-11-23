package dev.ducky.utilities;

import net.minecraftforge.common.ForgeConfigSpec;

public final class UtilityConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ADAPTIVE_TOOLS;
    public static final ForgeConfigSpec.BooleanValue ENTITY_CULLING;
    public static final ForgeConfigSpec.DoubleValue ENTITY_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue ITEM_CULLING;
    public static final ForgeConfigSpec.DoubleValue ITEM_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue SORT_CONTAINERS;
    public static final ForgeConfigSpec.BooleanValue SORT_INVENTORY;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("general");
        ADAPTIVE_TOOLS = b.comment("Automatically select a better hotbar tool while targeting a block or mob.")
                .define("adaptiveToolSwitching", true);
        SORT_CONTAINERS = b.define("containerSorting", true);
        SORT_INVENTORY = b.define("inventorySorting", true);
        b.pop();
        b.push("performance");
        ENTITY_CULLING = b.define("entityCulling", true);
        ENTITY_DISTANCE = b.defineInRange("entityRenderDistance", 96.0, 16.0, 512.0);
        ITEM_CULLING = b.define("droppedItemCulling", true);
        ITEM_DISTANCE = b.defineInRange("droppedItemRenderDistance", 48.0, 16.0, 256.0);
        b.pop();
        SPEC = b.build();
    }
    private UtilityConfig() {}
}
