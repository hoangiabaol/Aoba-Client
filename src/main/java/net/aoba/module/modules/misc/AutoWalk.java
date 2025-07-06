/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.module.modules.misc;

import net.aoba.Aoba;
import net.aoba.event.events.TickEvent.Post;
import net.aoba.event.events.TickEvent.Pre;
import net.aoba.event.listeners.TickListener;
import net.aoba.module.Category;
import net.aoba.module.Module;
import net.aoba.settings.types.BooleanSetting;

public class AutoWalk extends Module implements TickListener {
	private final BooleanSetting automaticJump = BooleanSetting.builder().id("autowalk_automatic_jump")
			.displayName("Automatically Jump").description("Automatically jumps when you hit a wall.")
			.defaultValue(true).build();

	public AutoWalk() {
		super("AutoWalk");

		setCategory(Category.of("Misc"));
		setDescription("Automatically forward walks for you.");

		addSetting(automaticJump);
	}

	@Override
	public void onDisable() {
		MC.options.forwardKey.setPressed(false);
		Aoba.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Aoba.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		MC.options.forwardKey.setPressed(true);
		if (MC.player.horizontalCollision && MC.player.isOnGround())
			MC.player.jump();
	}

	@Override
	public void onTick(Post event) {

	}
}
