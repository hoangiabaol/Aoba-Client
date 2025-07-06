package net.aoba.module.modules.combat;

import net.aoba.module.Category;
import net.aoba.module.Module;
import net.aoba.event.events.TickEvent;
import net.aoba.event.listeners.TickListener;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class AutoAnchor extends Module implements TickListener {
    private BlockPos lastAnchor = null;
    private int lastGlowstoneCharges = -1;
    private int prevSlot = -1;

    public AutoAnchor() {
        super("AutoAnchor");
        setCategory(Category.of("Combat"));
        setDescription("Tự động chuyển slot khi đặt và nạp glowstone cho respawn anchor.");
    }

    @Override
    public void onEnable() {
        AOBA_CLIENT.eventManager.AddListener(TickListener.class, this);
    }

    @Override
    public void onDisable() {
        AOBA_CLIENT.eventManager.RemoveListener(TickListener.class, this);
    }

    @Override
    public void onToggle() {}

    @Override
    public void onTick(TickEvent.Pre event) {
        // Không sử dụng
    }

    @Override
    public void onTick(TickEvent.Post event) {
        if (MC.player == null || MC.world == null) return;
        if (MC.crosshairTarget instanceof BlockHitResult bhr) {
            BlockPos pos = bhr.getBlockPos();
            if (MC.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR &&
                    MC.world.getBlockState(pos).contains(RespawnAnchorBlock.CHARGES)) {
                int charges = MC.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES);
                int selectedSlot = getSelectedSlot();
                if (charges == 0 && (lastAnchor == null || !lastAnchor.equals(pos))) {
                    int glowSlot = findGlowstoneInHotbar();
                    if (glowSlot != -1 && selectedSlot != glowSlot) {
                        prevSlot = selectedSlot;
                        setSelectedSlot(glowSlot);
                        lastAnchor = pos;
                        lastGlowstoneCharges = 0;
                    }
                }
                if (lastAnchor != null && lastAnchor.equals(pos) && lastGlowstoneCharges == 0 && charges > 0) {
                    int anchorSlot = findAnchorInHotbar();
                    if (anchorSlot != -1 && anchorSlot != selectedSlot) {
                        setSelectedSlot(anchorSlot);
                    } else if (prevSlot != -1) {
                        setSelectedSlot(prevSlot);
                    }
                    lastGlowstoneCharges = charges;
                }
            }
        }
    }

    private int findGlowstoneInHotbar() {
        for (int i = 0; i < 9; i++) {
            if (MC.player.getInventory().getStack(i).getItem() == Items.GLOWSTONE)
                return i;
        }
        return -1;
    }

    private int findAnchorInHotbar() {
        for (int i = 0; i < 9; i++) {
            if (MC.player.getInventory().getStack(i).getItem() == Items.RESPAWN_ANCHOR)
                return i;
        }
        return -1;
    }

    private int getSelectedSlot() {
        return MC.player.getInventory().getSelectedSlot();
    }
    private void setSelectedSlot(int slot) {
        MC.player.getInventory().setSelectedSlot(slot);
        MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    @Override
    public String getDescription() {
        return "Tự động chuyển slot khi đặt và nạp glowstone cho respawn anchor.";
    }
}
