package org.zeith.comm12.squake;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfig
{
	public static boolean isEnabled()
	{
		return COMMON.ENABLED.get();
	}

	public static void setEnabled(boolean enabled)
	{
		COMMON.ENABLED.set(enabled);
		COMMON.ENABLED.save();
	}

	public static boolean sharkingEnabled()
	{
		return COMMON.SHARKING_ENABLED.get();
	}

	public static boolean trimpingEnabled()
	{
		return COMMON.TRIMPING_ENABLED.get();
	}

	public static boolean uncappedBunnyhopEnabled()
	{
		return COMMON.UNCAPPED_BUNNYHOP_ENABLED.get();
	}

	public static double accelerate()
	{
		return COMMON.ACCELERATE.get();
	}

	public static double airAccelerate()
	{
		return COMMON.AIR_ACCELERATE.get();
	}

	public static double sharkingSurfTension()
	{
		return 1.0D - COMMON.SHARKING_SURFACE_TENSION.get();
	}

	public static double trimpMult()
	{
		return COMMON.TRIMP_MULTIPLIER.get();
	}

	public static double sharkingWaterFriction()
	{
		return 1.0D - COMMON.SHARKING_WATER_FRICTION.get() * 0.05D;
	}

	public static double maxAirAccelPerTick()
	{
		return COMMON.MAX_AIR_ACCEL_PER_TICK.get();
	}

	public static float softCap()
	{
		return COMMON.SOFT_CAP.get().floatValue() * 0.125F;
	}

	public static float hardCap()
	{
		return COMMON.HARD_CAP.get().floatValue() * 0.125F;
	}

	public static float softCapDegen()
	{
		return COMMON.SOFT_CAP_DEGEN.get().floatValue();
	}

	public static float increasedFallDistance()
	{
		return COMMON.INCREASED_FALL_DISTANCE.get().floatValue();
	}

	public static class Common
	{
		public final ForgeConfigSpec.BooleanValue ENABLED;

		public final ForgeConfigSpec.BooleanValue UNCAPPED_BUNNYHOP_ENABLED;
		public final ForgeConfigSpec.BooleanValue SHARKING_ENABLED;
		public final ForgeConfigSpec.BooleanValue TRIMPING_ENABLED;

		public final ForgeConfigSpec.DoubleValue AIR_ACCELERATE;
		public final ForgeConfigSpec.DoubleValue MAX_AIR_ACCEL_PER_TICK;
		public final ForgeConfigSpec.DoubleValue ACCELERATE;
		public final ForgeConfigSpec.DoubleValue HARD_CAP;
		public final ForgeConfigSpec.DoubleValue SOFT_CAP;
		public final ForgeConfigSpec.DoubleValue SOFT_CAP_DEGEN;
		public final ForgeConfigSpec.DoubleValue SHARKING_WATER_FRICTION;
		public final ForgeConfigSpec.DoubleValue SHARKING_SURFACE_TENSION;
		public final ForgeConfigSpec.DoubleValue TRIMP_MULTIPLIER;
		public final ForgeConfigSpec.DoubleValue INCREASED_FALL_DISTANCE;

		Common(ForgeConfigSpec.Builder builder)
		{
			builder.comment("Movement configurations")
					.push("movement");

			// boolean values

			ENABLED = builder
					.comment("turns off/on the quake-style movement for the client (essentially the saved value of the ingame toggle keybind)")
					.define("enabled", true);

			UNCAPPED_BUNNYHOP_ENABLED = builder
					.comment("if enabled, the soft and hard caps will not be applied at all")
					.define("uncappedBunnyhopEnabled", true);

			SHARKING_ENABLED = builder
					.comment("if enabled, holding jump while swimming at the surface of water allows you to glide")
					.define("sharkingEnabled", true);

			TRIMPING_ENABLED = builder
					.comment("if enabled, holding sneak while jumping will convert your horizontal speed into vertical speed")
					.define("trimpEnabled", true);

			// double values

			AIR_ACCELERATE = builder
					.comment("a higher value means you can turn more sharply in the air without losing speed")
					.defineInRange("airAccelerate", 14.0D, 0D, Double.MAX_VALUE);

			MAX_AIR_ACCEL_PER_TICK = builder
					.comment("a higher value means faster air acceleration")
					.defineInRange("maxAirAccelerationPerTick", 0.045D, 0D, Double.MAX_VALUE);

			ACCELERATE = builder
					.comment("a higher value means you accelerate faster on the ground")
					.defineInRange("groundAccelerate", 10D, 0D, Double.MAX_VALUE);

			HARD_CAP = builder
					.comment("see uncappedBunnyhopEnabled; if you ever jump while above the hard cap speed (moveSpeed*hardCapThreshold), your speed is set to the hard cap speed")
					.defineInRange("hardCapThreshold", 2D, 0D, Double.MAX_VALUE);

			SOFT_CAP = builder
					.comment("see uncappedBunnyhopEnabled and softCapDegen; soft cap speed = (moveSpeed*softCapThreshold)")
					.defineInRange("softCapThreshold", 1.4D, 0D, Double.MAX_VALUE);

			SOFT_CAP_DEGEN = builder
					.comment("the modifier used to calculate speed lost when jumping above the soft cap")
					.defineInRange("softCapDegen", 0.65D, 0D, Double.MAX_VALUE);

			SHARKING_WATER_FRICTION = builder
					.comment("amount of friction while sharking (between 0 and 1)")
					.defineInRange("sharkingWaterFriction", 0.1D, 0D, 1D);

			SHARKING_SURFACE_TENSION = builder
					.comment("amount of downward momentum you lose while entering water, a higher value means that you are able to shark after hitting the water from higher up")
					.defineInRange("sharkingSurfaceTension", 0.2D, 0D, Double.MAX_VALUE);

			TRIMP_MULTIPLIER = builder
					.comment("a lower value means less horizontal speed converted to vertical speed and vice versa")
					.defineInRange("trimpMultiplier", 1.4D, 0D, Double.MAX_VALUE);

			INCREASED_FALL_DISTANCE = builder
					.comment("increases the distance needed to fall in order to take fall damage; this is a server-side setting")
					.defineInRange("fallDistanceThresholdIncrease", 0D, 0D, Double.MAX_VALUE);

			builder.pop();
		}
	}

	static final ForgeConfigSpec commonSpec;
	public static final ModConfig.Common COMMON;

	static
	{
		final Pair<ModConfig.Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ModConfig.Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent)
	{
		Squake.LOGGER.debug("Loaded squake config file {}", configEvent.getConfig().getFileName());
	}
}