package org.zeith.comm12.squake;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ToggleKeyHandler
{
	private static final KeyBinding TOGGLE_KEY = new KeyBinding("squake.key.toggle", GLFW.GLFW_KEY_COMMA, "key.categories.squake");

	public static void setup()
	{
		ClientRegistry.registerKeyBinding(TOGGLE_KEY);
	}

	@SubscribeEvent
	public static void onKeyEvent(InputEvent.KeyInputEvent event)
	{
		if(TOGGLE_KEY.consumeClick())
		{
			ModConfig.setEnabled(!ModConfig.isEnabled());

			String feedback = ModConfig.isEnabled() ? I18n.get("squake.key.toggle.enabled") : I18n.get("squake.key.toggle.disabled");
			Minecraft.getInstance().gui.getChat().addMessage(new StringTextComponent("[" + Squake.MODNAME + "] " + feedback));
		}
	}
}