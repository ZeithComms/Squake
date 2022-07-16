package org.zeith.comm12.squake;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ToggleKeyHandler
{
	private static final KeyMapping TOGGLE_KEY = new KeyMapping("squake.key.toggle", GLFW.GLFW_KEY_COMMA, "key.categories.squake");
	
	public static void setup()
	{
		MinecraftForge.EVENT_BUS.addListener(ToggleKeyHandler::onKeyEvent);
	}
	
	public static void registerKeys(RegisterKeyMappingsEvent evt)
	{
		evt.register(TOGGLE_KEY);
	}
	
	private static void onKeyEvent(InputEvent.Key event)
	{
		if(TOGGLE_KEY.consumeClick())
		{
			ModConfig.setEnabled(!ModConfig.isEnabled());
			var feedback = MutableComponent.create(new TranslatableContents(ModConfig.isEnabled() ? "squake.key.toggle.enabled" : "squake.key.toggle.disabled"));
			var t1 = MutableComponent.create(new LiteralContents("["));
			var t2 = MutableComponent.create(new LiteralContents("Squake")).withStyle(ChatFormatting.GOLD);
			var t3 = MutableComponent.create(new LiteralContents("] "));
			Minecraft.getInstance().gui.getChat().addMessage(t1.append(t2).append(t3).append(feedback));
		}
	}
}