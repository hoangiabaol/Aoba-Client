package net.aoba.module.modules.render;

import net.aoba.Aoba;
import net.aoba.event.events.TickEvent;
import net.aoba.event.listeners.TickListener;
import net.aoba.module.Category;
import net.aoba.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public class AntiTrap extends Module implements TickListener {
    private static final int MAX_REMOVE = 15;
    private static final int RADIUS = 4;

    public AntiTrap() {
        super("AntiTrap");
        setCategory(Category.of("Render"));
        setDescription("Removes nearby ArmorStands and Signs to prevent trap lag or visual trolling.");
    }

    @Override
    public void onEnable() {
        // Register this module as a TickListener when enabled
        Aoba.getInstance().eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onDisable() {
        // Unregister this module as a TickListener when disabled
        Aoba.getInstance().eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onTick(TickEvent.Pre event) {
        // Not used, but required by TickListener interface
    }

    @Override
    public void onTick(TickEvent.Post event) {
        if (MC.player == null || MC.world == null) return;

        int removeCount = 0;

        // Remove nearby ArmorStand entities
        Iterator<Entity> it = MC.world.getEntities().iterator();
        while (it.hasNext() && removeCount < MAX_REMOVE) {
            Entity entity = it.next();
            if (entity instanceof ArmorStandEntity && MC.player.squaredDistanceTo(entity) < 16.0) {
                MC.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED);
                removeCount++;
            }
        }

        // Remove nearby sign blocks
        BlockPos playerPos = MC.player.getBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = MC.world.getBlockState(pos).getBlock();
                    if (isSign(block)) {
                        MC.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    // Check if a block is any type of sign
    private boolean isSign(Block block) {
        return block == Blocks.OAK_SIGN || block == Blocks.SPRUCE_SIGN ||
                block == Blocks.BIRCH_SIGN || block == Blocks.JUNGLE_SIGN ||
                block == Blocks.ACACIA_SIGN || block == Blocks.DARK_OAK_SIGN ||
                block == Blocks.CHERRY_SIGN || block == Blocks.BAMBOO_SIGN ||
                block == Blocks.CRIMSON_SIGN || block == Blocks.WARPED_SIGN ||
                block == Blocks.OAK_WALL_SIGN || block == Blocks.SPRUCE_WALL_SIGN ||
                block == Blocks.BIRCH_WALL_SIGN || block == Blocks.JUNGLE_WALL_SIGN ||
                block == Blocks.ACACIA_WALL_SIGN || block == Blocks.DARK_OAK_WALL_SIGN ||
                block == Blocks.CHERRY_WALL_SIGN || block == Blocks.BAMBOO_WALL_SIGN ||
                block == Blocks.CRIMSON_WALL_SIGN || block == Blocks.WARPED_WALL_SIGN;
    }

    @Override
    public void onToggle() {
        // No action needed on toggle
    }
}