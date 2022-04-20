package org.zeith.comm12.squake;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Motions
{
	public static double getMotionX(Entity entity)
	{
		return entity.getDeltaMovement().x;
	}

	public static double getMotionY(Entity entity)
	{
		return entity.getDeltaMovement().y;
	}

	public static double getMotionZ(Entity entity)
	{
		return entity.getDeltaMovement().z;
	}

	public static void setMotionX(Entity entity, double motionX)
	{
		var motion = entity.getDeltaMovement();
		entity.setDeltaMovement(motionX, motion.y, motion.z);
	}

	public static void setMotionY(Entity entity, double motionY)
	{
		var motion = entity.getDeltaMovement();
		entity.setDeltaMovement(motion.x, motionY, motion.z);
	}

	public static void setMotionZ(Entity entity, double motionZ)
	{
		var motion = entity.getDeltaMovement();
		entity.setDeltaMovement(motion.x, motion.y, motionZ);
	}

	public static void setMotionHoriz(Entity entity, double motionX, double motionZ)
	{
		entity.setDeltaMovement(motionX, entity.getDeltaMovement().y, motionZ);
	}

	public static float getSlipperiness(Entity entity, BlockPos pos)
	{
		return entity.level.getBlockState(pos).getFriction(entity.level, pos, entity);
	}

	public static double getSideMove(Vec3 relative)
	{
		return relative.x;
	}

	public static double getUpMove(Vec3 relative)
	{
		return relative.y;
	}

	public static double getForwardMove(Vec3 relative)
	{
		return relative.z;
	}

	public static boolean notZero(double val)
	{
		return Math.abs(val) >= 1.0E-4;
	}

	public static boolean isOffsetPositionInLiquid(Player player, double x, double y, double z)
	{
		var axisalignedbb = player.getBoundingBox().move(x, y, z);
		return isLiquidPresentInAABB(player, axisalignedbb);
	}

	/**
	 * Determines if a liquid is present within the specified AxisAlignedBB.
	 */
	private static boolean isLiquidPresentInAABB(Player player, AABB bb)
	{
		return player.level.noCollision(player, bb) && !player.level.containsAnyLiquid(bb);
	}
}