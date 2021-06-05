package org.zeith.comm12.squake;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgeBusEvents
{
	@SubscribeEvent
	public static void onLivingFall(LivingFallEvent event)
	{
		if(!(event.getEntityLiving() instanceof PlayerEntity))
			return;

		if(ModConfig.increasedFallDistance() != 0.0D)
		{
			event.setDistance(event.getDistance() - ModConfig.increasedFallDistance());
		}
	}
}