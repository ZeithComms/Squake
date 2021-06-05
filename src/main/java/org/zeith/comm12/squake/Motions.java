package org.zeith.comm12.squake;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

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
		Vector3d motion = entity.getDeltaMovement();
		entity.setDeltaMovement(motionX, motion.y, motion.z);
	}

	public static void setMotionY(Entity entity, double motionY)
	{
		Vector3d motion = entity.getDeltaMovement();
		entity.setDeltaMovement(motion.x, motionY, motion.z);
	}

	public static void setMotionZ(Entity entity, double motionZ)
	{
		Vector3d motion = entity.getDeltaMovement();
		entity.setDeltaMovement(motion.x, motion.y, motionZ);
	}

	public static void setMotionHoriz(Entity entity, double motionX, double motionZ)
	{
		entity.setDeltaMovement(motionX, entity.getDeltaMovement().y, motionZ);
	}

	public static float getSlipperiness(Entity entity, BlockPos pos)
	{
		return entity.level.getBlockState(pos).getSlipperiness(entity.level, pos, entity);
	}

	public static double getSideMove(Vector3d relative)
	{
		return relative.x;
	}

	public static double getUpMove(Vector3d relative)
	{
		return relative.y;
	}

	public static double getForwardMove(Vector3d relative)
	{
		return relative.z;
	}

	public static boolean notZero(double val)
	{
		return Math.abs(val) >= 1.0E-4;
	}

	public static boolean isOffsetPositionInLiquid(PlayerEntity player, double x, double y, double z)
	{
		AxisAlignedBB axisalignedbb = player.getBoundingBox().move(x, y, z);
		return isLiquidPresentInAABB(player, axisalignedbb);
	}

	/**
	 * Determines if a liquid is present within the specified AxisAlignedBB.
	 */
	private static boolean isLiquidPresentInAABB(PlayerEntity player, AxisAlignedBB bb)
	{
		return player.level.noCollision(player, bb) && !player.level.containsAnyLiquid(bb);
	}
}