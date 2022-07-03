package org.zeith.comm12.squake;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SquakeClientPlayer
{
	private static final List<float[]> baseVelocities = new ArrayList<>();
	private static Method setDidJumpThisTick = null;
	private static Method setIsJumping = null;
	
	static
	{
		try
		{
			if(ModList.get().isLoaded("squeedometer"))
			{
				Class<?> hudSpeedometer = Class.forName("squeek.speedometer.HudSpeedometer");
				setDidJumpThisTick = hudSpeedometer.getDeclaredMethod("setDidJumpThisTick", boolean.class);
				setIsJumping = hudSpeedometer.getDeclaredMethod("setIsJumping", boolean.class);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean moveEntityWithHeading(Player player, ISquakeEntity squakeEntity, float sidemove, float upmove, float forwardmove)
	{
		if(!player.level.isClientSide)
			return false;
		
		if(!ModConfig.isEnabled())
			return false;
		
		if(player.hasEffect(MobEffects.LEVITATION))
			return false;
		
		if(squakeEntity.shouldReturnMovement_Squake())
			return false;
		
		boolean didQuakeMovement;
		double d0 = player.getX();
		double d1 = player.getY();
		double d2 = player.getZ();
		
		if((player.getAbilities().flying || player.isFallFlying()) && player.getVehicle() == null)
			return false;
		else
			didQuakeMovement = quake_moveEntityWithHeading(player, sidemove, upmove, forwardmove);
		
		if(didQuakeMovement)
			player.checkMovementStatistics(player.getX() - d0, player.getY() - d1, player.getZ() - d2);
		
		return didQuakeMovement;
	}
	
	public static void beforeOnLivingUpdate(Player player)
	{
		if(!player.level.isClientSide)
			return;
		
		if(setDidJumpThisTick != null)
		{
			try
			{
				setDidJumpThisTick.invoke(null, false);
			} catch(Exception e)
			{
			}
		}
		
		if(!baseVelocities.isEmpty())
		{
			baseVelocities.clear();
		}
		
		if(setIsJumping != null)
		{
			try
			{
				setIsJumping.invoke(null, isJumping(player));
			} catch(Exception e)
			{
			}
		}
	}
	
	public static boolean moveRelativeBase(Entity entity, ISquakeEntity squakeEntity, float sidemove, float upmove, float forwardmove, float friction)
	{
		if(entity instanceof Player player)
			return moveRelative(player, squakeEntity, sidemove, upmove, forwardmove, friction);
		
		return false;
	}
	
	public static boolean moveRelative(Player player, ISquakeEntity squakeEntity, float sidemove, float upmove, float forwardmove, float friction)
	{
		if(!player.level.isClientSide)
			return false;
		
		if(!ModConfig.isEnabled())
			return false;
		
		if(player.hasEffect(MobEffects.LEVITATION))
			return false;
		
		if(squakeEntity.shouldReturnMovement_Squake())
			return false;
		
		if((player.getAbilities().flying && player.getVehicle() == null) || player.isInWater() || player.isInLava() || player.onClimbable())
		{
			return false;
		}
		
		// this is probably wrong, but its what was there in 1.10.2
		float wishspeed = friction;
		wishspeed *= 2.15f;
		float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
		float[] wishvel = new float[]{
				wishdir[0] * wishspeed,
				wishdir[1] * wishspeed
		};
		baseVelocities.add(wishvel);
		
		return true;
	}
	
	public static void afterJump(Player player)
	{
		if(!player.level.isClientSide)
			return;
		
		if(!ModConfig.isEnabled())
			return;
		
		if(player.hasEffect(MobEffects.LEVITATION))
			return;
		
		// undo this dumb thing
		if(player.isSprinting())
		{
			float f = player.getYRot() * 0.017453292F;
			
			double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
			
			motionX += Mth.sin(f) * 0.2F;
			motionZ -= Mth.cos(f) * 0.2F;
			
			Motions.setMotionHoriz(player, motionX, motionZ);
		}
		
		quake_Jump(player);
		
		if(setDidJumpThisTick != null)
		{
			try
			{
				setDidJumpThisTick.invoke(null, true);
			} catch(Exception e)
			{
			}
		}
	}
	
	/* =================================================
	 * START HELPERS
	 * =================================================
	 */
	
	private static double getSpeed(Player player)
	{
		double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
		return Math.sqrt(motionX * motionX + motionZ * motionZ);
	}
	
	private static float getSurfaceFriction(Player player)
	{
		float f2 = 1.0F;
		
		if(player.isOnGround())
		{
			BlockPos groundPos = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getBoundingBox().minY) - 1, Mth.floor(player.getZ()));
			f2 = 1.0F - Motions.getSlipperiness(player, groundPos);
		}
		
		return f2;
	}
	
	private static float getSlipperiness(Player player)
	{
		float f2 = 0.91F;
		if(player.isOnGround())
		{
			BlockPos groundPos = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getBoundingBox().minY) - 1, Mth.floor(player.getZ()));
			f2 = Motions.getSlipperiness(player, groundPos) * 0.91F;
		}
		return f2;
	}
	
	private static float minecraft_getMoveSpeed(Player player)
	{
		float f2 = getSlipperiness(player);
		
		float f3 = 0.16277136F / (f2 * f2 * f2);
		
		return player.getSpeed() * f3;
	}
	
	private static float[] getMovementDirection(Player player, float sidemove, float forwardmove)
	{
		float f3 = sidemove * sidemove + forwardmove * forwardmove;
		float[] dir = {
				0.0F,
				0.0F
		};
		
		if(f3 >= 1.0E-4F)
		{
			f3 = Mth.sqrt(f3);
			
			if(f3 < 1.0F)
			{
				f3 = 1.0F;
			}
			
			f3 = 1.0F / f3;
			sidemove *= f3;
			forwardmove *= f3;
			float f4 = Mth.sin(player.getYRot() * (float) Math.PI / 180.0F);
			float f5 = Mth.cos(player.getYRot() * (float) Math.PI / 180.0F);
			dir[0] = (sidemove * f5 - forwardmove * f4);
			dir[1] = (forwardmove * f5 + sidemove * f4);
		}
		
		return dir;
	}
	
	private static float quake_getMoveSpeed(Player player)
	{
		float baseSpeed = player.getSpeed();
		return !player.isShiftKeyDown() ? baseSpeed * 2.15F : baseSpeed * 1.11F;
	}
	
	private static float quake_getMaxMoveSpeed(Player player)
	{
		float baseSpeed = player.getSpeed();
		return baseSpeed * 2.15F;
	}
	
	private static void spawnBunnyhopParticles(Player player, int numParticles)
	{
		// taken from sprint
		int j = Mth.floor(player.getX());
		int i = Mth.floor(player.getY() - 0.20000000298023224D - player.getMyRidingOffset());
		int k = Mth.floor(player.getZ());
		BlockState blockState = player.level.getBlockState(new BlockPos(j, i, k));
		
		var motion = player.getDeltaMovement();
		RandomSource random = player.getRandom();
		
		if(blockState.getRenderShape() != RenderShape.INVISIBLE)
		{
			for(int iParticle = 0; iParticle < numParticles; iParticle++)
			{
				player.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), player.getX() + (random.nextFloat() - 0.5D) * player.getBbWidth(), player.getBoundingBox().minY + 0.1D, player.getZ() + (random.nextFloat() - 0.5D) * player.getBbWidth(), -motion.x * 4.0D, 1.5D, -motion.z * 4.0D);
			}
		}
	}
	
	private static boolean isJumping(Player player)
	{
		return player.jumping;
	}
	
	/* =================================================
	 * END HELPERS
	 * =================================================
	 */
	
	/* =================================================
	 * START MINECRAFT PHYSICS
	 * =================================================
	 */
	
	private static void minecraft_ApplyGravity(Player player)
	{
		double motionY = Motions.getMotionY(player);
		
		if(player.level.isClientSide && (!player.level.isLoaded(new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ())) || player.level.getChunk(new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ())).getStatus() != ChunkStatus.FULL))
		{
			if(player.getY() > 0.0D)
			{
				motionY = -0.1D;
			} else
			{
				motionY = 0.0D;
			}
		} else
		{
			// gravity
			var gravity = player.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
			motionY -= gravity.getValue();
		}
		
		// air resistance
		motionY *= 0.9800000190734863D;
		
		Motions.setMotionY(player, motionY);
	}
	
	private static void minecraft_ApplyFriction(Player player, float momentumRetention)
	{
		double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
		motionX *= momentumRetention;
		motionZ *= momentumRetention;
		Motions.setMotionHoriz(player, motionX, motionZ);
	}
	
	private static void minecraft_SwingLimbsBasedOnMovement(Player player)
	{
		player.animationSpeedOld = player.animationSpeed;
		double d0 = player.getX() - player.xo;
		double d1 = player.getZ() - player.zo;
		float f6 = Mth.sqrt((float) (d0 * d0 + d1 * d1)) * 4.0F;
		if(f6 > 1.0F) f6 = 1.0F;
		player.animationSpeed += (f6 - player.animationSpeed) * 0.4F;
		player.animationPosition += player.animationSpeed;
	}
	
	private static void minecraft_WaterMove(Player player, float sidemove, float upmove, float forwardmove)
	{
		double d0 = player.getY();
		player.moveRelative(0.04F, new Vec3(sidemove, upmove, forwardmove));
		
		double motionX = Motions.getMotionX(player), motionY = Motions.getMotionY(player), motionZ = Motions.getMotionZ(player);
		
		player.move(MoverType.SELF, player.getDeltaMovement());
		
		motionX *= 0.800000011920929D;
		motionY *= 0.800000011920929D;
		motionZ *= 0.800000011920929D;
		motionY -= 0.02D;
		
		player.setDeltaMovement(motionX, motionY, motionZ);
		
		if(player.horizontalCollision && Motions.isOffsetPositionInLiquid(player, Motions.getMotionX(player), Motions.getMotionY(player) + 0.6000000238418579D - player.getY() + d0, Motions.getMotionZ(player)))
		{
			Motions.setMotionY(player, 0.30000001192092896D);
		}
	}
	
	/* =================================================
	 * END MINECRAFT PHYSICS
	 * =================================================
	 */
	
	/* =================================================
	 * START QUAKE PHYSICS
	 * =================================================
	 */
	
	/**
	 * Moves the entity based on the specified heading.  Args: strafe, forward
	 */
	public static boolean quake_moveEntityWithHeading(Player player, float sidemove, float upmove, float forwardmove)
	{
		// take care of ladder movement using default code
		if(player.onClimbable())
		{
			return false;
		}
		// take care of lava movement using default code
		else if((player.isInLava() && !player.getAbilities().flying))
		{
			return false;
		} else if(player.isInWater() && !player.getAbilities().flying)
		{
			if(ModConfig.sharkingEnabled())
				quake_WaterMove(player, sidemove, upmove, forwardmove);
			else
			{
				return false;
			}
		} else
		{
			// get all relevant movement values
			float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMoveSpeed(player) : 0.0F;
			float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
			boolean isOnGroundForReal = player.isOnGround() && !isJumping(player);
			float momentumRetention = getSlipperiness(player);
			
			// ground movement
			if(isOnGroundForReal)
			{
				// apply friction before acceleration so we can accelerate back up to maxspeed afterwards
				//quake_Friction(); // buggy because material-based friction uses a totally different format
				minecraft_ApplyFriction(player, momentumRetention);
				
				double sv_accelerate = ModConfig.accelerate();
				
				if(wishspeed != 0.0F)
				{
					// alter based on the surface friction
					sv_accelerate *= minecraft_getMoveSpeed(player) * 2.15F / wishspeed;
					
					quake_Accelerate(player, wishspeed, wishdir[0], wishdir[1], sv_accelerate);
				}
				
				if(!baseVelocities.isEmpty())
				{
					float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
					
					double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
					
					// add in base velocities
					for(float[] baseVel : baseVelocities)
					{
						motionX += baseVel[0] * speedMod;
						motionZ += baseVel[1] * speedMod;
					}
					
					Motions.setMotionHoriz(player, motionX, motionZ);
				}
			}
			// air movement
			else
			{
				double sv_airaccelerate = ModConfig.airAccelerate();
				quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], sv_airaccelerate);
				
				if(ModConfig.sharkingEnabled() && ModConfig.sharkingSurfTension() > 0.0D && isJumping(player) && Motions.getMotionY(player) < 0.0F)
				{
					var aabb = player.getBoundingBox().move(player.getDeltaMovement());
					boolean isFallingIntoWater = player.level.containsAnyLiquid(aabb);
					
					if(isFallingIntoWater)
						Motions.setMotionY(player, Motions.getMotionY(player) * ModConfig.sharkingSurfTension());
				}
			}
			
			// apply velocity
			player.move(MoverType.SELF, player.getDeltaMovement());
			
			// HL2 code applies half gravity before acceleration and half after acceleration, but this seems to work fine
			minecraft_ApplyGravity(player);
		}
		
		// swing them arms
		minecraft_SwingLimbsBasedOnMovement(player);
		
		return true;
	}
	
	private static void quake_Jump(Player player)
	{
		quake_ApplySoftCap(player, quake_getMaxMoveSpeed(player));
		
		boolean didTrimp = quake_DoTrimp(player);
		
		if(!didTrimp)
		{
			quake_ApplyHardCap(player, quake_getMaxMoveSpeed(player));
		}
	}
	
	private static boolean quake_DoTrimp(Player player)
	{
		if(ModConfig.trimpingEnabled() && player.isShiftKeyDown())
		{
			double curspeed = getSpeed(player);
			float movespeed = quake_getMaxMoveSpeed(player);
			if(curspeed > movespeed)
			{
				double speedbonus = curspeed / movespeed * 0.5F;
				if(speedbonus > 1.0F)
					speedbonus = 1.0F;
				
				Motions.setMotionY(player, Motions.getMotionY(player) + speedbonus * curspeed * ModConfig.trimpMult());
				
				if(ModConfig.trimpMult() > 0)
				{
					float mult = (float) (1.0f / ModConfig.trimpMult());
					double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
					motionX *= mult;
					motionZ *= mult;
					Motions.setMotionHoriz(player, motionX, motionZ);
				}
				
				spawnBunnyhopParticles(player, 30);
				
				return true;
			}
		}
		
		return false;
	}
	
	private static void quake_ApplyWaterFriction(Player player, double friction)
	{
		player.setDeltaMovement(player.getDeltaMovement().scale(friction));
	}
	
	@SuppressWarnings("unused")
	private static void quake_WaterAccelerate(Player player, float wishspeed, float speed, double wishX, double wishZ, double accel)
	{
		float addspeed = wishspeed - speed;
		if(addspeed > 0)
		{
			float accelspeed = (float) (accel * wishspeed * 0.05F);
			if(accelspeed > addspeed)
			{
				accelspeed = addspeed;
			}
			double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
			motionX += accelspeed * wishX;
			motionZ += accelspeed * wishZ;
			Motions.setMotionHoriz(player, motionX, motionZ);
		}
	}
	
	private static void quake_WaterMove(Player player, float sidemove, float upmove, float forwardmove)
	{
		double posY = player.getY();
		
		// get all relevant movement values
		float wishspeed = (sidemove != 0.0F || forwardmove != 0.0F) ? quake_getMaxMoveSpeed(player) : 0.0F;
		float[] wishdir = getMovementDirection(player, sidemove, forwardmove);
		boolean isSharking = isJumping(player) && Motions.isOffsetPositionInLiquid(player, 0.0D, 1.0D, 0.0D);
		double curspeed = getSpeed(player);
		
		if(!isSharking || curspeed < 0.078F)
		{
			minecraft_WaterMove(player, sidemove, upmove, forwardmove);
		} else
		{
			if(curspeed > 0.09)
				quake_ApplyWaterFriction(player, ModConfig.sharkingWaterFriction());
			
			if(curspeed > 0.098)
				quake_AirAccelerate(player, wishspeed, wishdir[0], wishdir[1], ModConfig.accelerate());
			else
				quake_Accelerate(player, .0980F, wishdir[0], wishdir[1], ModConfig.accelerate());
			
			player.move(MoverType.SELF, player.getDeltaMovement());
			
			Motions.setMotionY(player, 0);
		}
		
		// water jump
		if(player.horizontalCollision && Motions.isOffsetPositionInLiquid(player, Motions.getMotionX(player), Motions.getMotionY(player) + 0.6000000238418579D - player.getY() + posY, Motions.getMotionZ(player)))
		{
			Motions.setMotionY(player, 0.30000001192092896D);
		}
		
		if(!baseVelocities.isEmpty())
		{
			float speedMod = wishspeed / quake_getMaxMoveSpeed(player);
			// add in base velocities
			
			double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
			for(float[] baseVel : baseVelocities)
			{
				motionX += baseVel[0] * speedMod;
				motionZ += baseVel[1] * speedMod;
			}
			Motions.setMotionHoriz(player, motionX, motionZ);
		}
	}
	
	private static void quake_Accelerate(Player player, float wishspeed, double wishX, double wishZ, double accel)
	{
		double addspeed, accelspeed, currentspeed;
		
		// Determine veer amount
		// this is a dot product
		currentspeed = Motions.getMotionX(player) * wishX + Motions.getMotionZ(player) * wishZ;
		
		// See how much to add
		addspeed = wishspeed - currentspeed;
		
		// If not adding any, done.
		if(addspeed <= 0)
			return;
		
		// Determine acceleration speed after acceleration
		accelspeed = accel * wishspeed / getSlipperiness(player) * 0.05F;
		
		// Cap it
		if(accelspeed > addspeed)
			accelspeed = addspeed;
		
		// Adjust pmove vel.
		double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
		motionX += accelspeed * wishX;
		motionZ += accelspeed * wishZ;
		Motions.setMotionHoriz(player, motionX, motionZ);
	}
	
	private static void quake_AirAccelerate(Player player, float wishspeed, double wishX, double wishZ, double accel)
	{
		double addspeed, accelspeed, currentspeed;
		
		float wishspd = wishspeed;
		float maxAirAcceleration = (float) ModConfig.maxAirAccelPerTick();
		
		if(wishspd > maxAirAcceleration)
			wishspd = maxAirAcceleration;
		
		// Determine veer amount
		// this is a dot product
		currentspeed = Motions.getMotionX(player) * wishX + Motions.getMotionZ(player) * wishZ;
		
		// See how much to add
		addspeed = wishspd - currentspeed;
		
		// If not adding any, done.
		if(addspeed <= 0)
			return;
		
		// Determine acceleration speed after acceleration
		accelspeed = accel * wishspeed * 0.05F;
		
		// Cap it
		if(accelspeed > addspeed)
			accelspeed = addspeed;
		
		// Adjust pmove vel.
		double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
		motionX += accelspeed * wishX;
		motionZ += accelspeed * wishZ;
		Motions.setMotionHoriz(player, motionX, motionZ);
	}
	
	@SuppressWarnings("unused")
	private static void quake_Friction(Player player)
	{
		double speed, newspeed, control;
		float friction;
		float drop;
		
		// Calculate speed
		speed = getSpeed(player);
		
		// If too slow, return
		if(speed <= 0.0F)
		{
			return;
		}
		
		drop = 0.0F;
		
		// convars
		float sv_friction = 1.0F;
		float sv_stopspeed = 0.005F;
		
		float surfaceFriction = getSurfaceFriction(player);
		friction = sv_friction * surfaceFriction;
		
		// Bleed off some speed, but if we have less than the bleed
		//  threshold, bleed the threshold amount.
		control = (speed < sv_stopspeed) ? sv_stopspeed : speed;
		
		// Add the amount to the drop amount.
		drop += control * friction * 0.05F;
		
		// scale the velocity
		newspeed = speed - drop;
		if(newspeed < 0.0F)
			newspeed = 0.0F;
		double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
		if(newspeed != speed)
		{
			// Determine proportion of old speed we are using.
			newspeed /= speed;
			// Adjust velocity according to proportion.
			motionX *= newspeed;
			motionZ *= newspeed;
		}
		Motions.setMotionHoriz(player, motionX, motionZ);
	}
	
	private static void quake_ApplySoftCap(Player player, float movespeed)
	{
		float softCapPercent = ModConfig.softCap();
		float softCapDegen = ModConfig.softCapDegen();
		
		if(ModConfig.uncappedBunnyhopEnabled())
		{
			softCapPercent = 1.0F;
			softCapDegen = 1.0F;
		}
		
		float speed = (float) (getSpeed(player));
		float softCap = movespeed * softCapPercent;
		
		// apply soft cap first; if soft -> hard is not done, then you can continually trigger only the hard cap and stay at the hard cap
		if(speed > softCap)
		{
			if(softCapDegen != 1.0F)
			{
				float applied_cap = (speed - softCap) * softCapDegen + softCap;
				float multi = applied_cap / speed;
				double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
				motionX *= multi;
				motionZ *= multi;
				Motions.setMotionHoriz(player, motionX, motionZ);
			}
			
			spawnBunnyhopParticles(player, 10);
		}
	}
	
	private static void quake_ApplyHardCap(Player player, float movespeed)
	{
		if(ModConfig.uncappedBunnyhopEnabled())
			return;
		
		float hardCapPercent = ModConfig.hardCap();
		
		float speed = (float) (getSpeed(player));
		float hardCap = movespeed * hardCapPercent;
		
		if(speed > hardCap && hardCap != 0.0F)
		{
			float multi = hardCap / speed;
			
			double motionX = Motions.getMotionX(player), motionZ = Motions.getMotionZ(player);
			motionX *= multi;
			motionZ *= multi;
			Motions.setMotionHoriz(player, motionX, motionZ);
			
			spawnBunnyhopParticles(player, 30);
		}
	}
	
	@SuppressWarnings("unused")
	private static void quake_OnLivingUpdate()
	{
	}
	
	/* =================================================
	 * END QUAKE PHYSICS
	 * =================================================
	 */
}