/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.module.modules.movement;

import net.aoba.Aoba;
import net.aoba.event.events.SendPacketEvent;
import net.aoba.event.events.TickEvent.Post;
import net.aoba.event.events.TickEvent.Pre;
import net.aoba.event.listeners.SendPacketListener;
import net.aoba.event.listeners.TickListener;
import net.aoba.managers.rotation.Rotation;
import net.aoba.mixin.interfaces.IPlayerMoveC2SPacket;
import net.aoba.module.Category;
import net.aoba.module.Module;
import net.aoba.settings.types.BooleanSetting;
import net.aoba.settings.types.EnumSetting;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

public class NoFall extends Module implements TickListener, SendPacketListener {
	public enum Mode {
		Packet, Bucket
	}

	private final EnumSetting<Mode> mode = EnumSetting.<Mode>builder().id("nofall_mode").displayName("Mode")
			.description("Controls how NoFall will react to fall damage.").defaultValue(Mode.Packet).build();

	private final BooleanSetting ignoreElytra = BooleanSetting.builder().id("nofall_ignore_elytra").displayName("Ignore Elytra")
			.description("Does not trigger NoFall when the player is flying with an elytra.").defaultValue(false)
			.build();

	private final BooleanSetting ignoreMace = BooleanSetting.builder().id("nofall_ignore_mace")
			.displayName("Ignore Mace").description("Does not trigger NoFall when a mace is in the players hand.")
			.defaultValue(true).build();

	public NoFall() {
		super("No-Fall");
		setCategory(Category.of("Movement"));
		setDescription("Prevents fall damage.");

		addSetting(mode);
		addSetting(ignoreElytra);
		addSetting(ignoreMace);
	}

	@Override
	public void onDisable() {
		Aoba.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Aoba.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Aoba.getInstance().eventManager.AddListener(TickListener.class, this);
		Aoba.getInstance().eventManager.AddListener(SendPacketListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSendPacket(SendPacketEvent event) {
		// Only trigger if the current mode is packet mode.
		if (mode.getValue() != Mode.Packet)
			return;

		Packet<?> packet = event.GetPacket();
		if (packet instanceof PlayerMoveC2SPacket) {
			// Ignore creative mode
			if (MC.player.getAbilities().creativeMode)
				return;

			// Ignore Mace
			if (ignoreMace.getValue() && MC.player.getMainHandStack().isOf(Items.MACE))
				return;

			// Ignore Elytras
			if (ignoreElytra.getValue() && MC.player.isGliding())
				return;

			// Set packet to onGround
			IPlayerMoveC2SPacket iPacket = (IPlayerMoveC2SPacket) packet;
			iPacket.setOnGround(true);
		}
	}

	@Override
	public void onTick(Pre event) {
		if (mode.getValue() != Mode.Bucket)
			return;

		if (MC.player.isOnGround())
			return;

		if (!willPlayerLandInWater()) {

			RaycastContext context = new RaycastContext(MC.player.getPos(), MC.player.getPos().subtract(0, 5, 0),
					RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, MC.player);
			BlockHitResult result = MC.world.raycast(context);

			if (result != null && result.getType() == HitResult.Type.BLOCK) {
				BlockPos targetPos = result.getBlockPos().up();

				float rotationDegreesPerTick = 30f;
				Rotation rotation = Rotation.getPlayerRotationDeltaFromPosition(targetPos.toCenterPos());

				float maxYawRotationDelta = Math.clamp((float) -rotation.yaw(), -rotationDegreesPerTick,
						rotationDegreesPerTick);
				float maxPitchRotation = Math.clamp((float) -rotation.pitch(), -rotationDegreesPerTick,
						rotationDegreesPerTick);

				Rotation newRotation = new Rotation(MC.player.getYaw() + maxYawRotationDelta,
						MC.player.getPitch() + maxPitchRotation);
				MC.player.setYaw((float) newRotation.yaw());
				MC.player.setPitch((float) newRotation.pitch());

				ActionResult actionResult = MC.interactionManager.interactBlock(MC.player, Hand.MAIN_HAND, result);

				if (actionResult.isAccepted()) {
					MC.player.swingHand(Hand.MAIN_HAND);
					MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
				}
			}
		}
	}

	@Override
	public void onTick(Post event) {

	}

	private boolean willPlayerLandInWater() {
		for (int i = 0; i < 64; i++) {
			BlockPos blockPos = MC.player.getBlockPos().add(0, -i, 0);
			BlockState state = MC.world.getBlockState(blockPos);

			if (state.blocksMovement())
				break;

			Fluid fluid = state.getFluidState().getFluid();
			if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
				return true;
			}
		}

		return false;
	}
}
