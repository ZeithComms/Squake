package org.zeith.comm12.squake;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.compress.archivers.sevenz.CLI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Squake.MODID)
public class Squake
{
	public static final String MODID = "squake";
	public static final String MODNAME = "Squake";
	public static Squake instance;
	public static final Logger LOGGER = LogManager.getLogger(MODNAME);
	
	public Squake()
	{
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.commonSpec);
		modEventBus.register(ModConfig.class);
		modEventBus.register(this);
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			modEventBus.addListener(ToggleKeyHandler::registerKeys);
		});
		
		instance = this;
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void clientSetup(FMLClientSetupEvent e)
	{
		ToggleKeyHandler.setup();
		
		// no config gui screen here.
//		ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> parent));
	}
}