package org.zeith.comm12.squake.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.zeith.comm12.squake.SquakeClientPlayer;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity
		extends LivingEntity
{
	public MixinPlayerEntity(EntityType<? extends LivingEntity> type, World world)
	{
		super(type, world);
	}

	@Inject(
			method = "travel",
			at = @At("HEAD"),
			cancellable = true
	)
	public void moveEntityWithHeading(Vector3d vec, CallbackInfo ci)
	{
		PlayerEntity asPlayer = (PlayerEntity) (LivingEntity) this;
		if(SquakeClientPlayer.moveEntityWithHeading(asPlayer, (float) vec.x, (float) vec.y, (float) vec.z))
			ci.cancel();
	}

	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	public void beforeOnLivingUpdate(CallbackInfo ci)
	{
		PlayerEntity asPlayer = (PlayerEntity) (LivingEntity) this;
		SquakeClientPlayer.beforeOnLivingUpdate(asPlayer);
	}

	@Inject(
			method = "jumpFromGround",
			at = @At("TAIL")
	)
	public void afterJump(CallbackInfo ci)
	{
		PlayerEntity asPlayer = (PlayerEntity) (LivingEntity) this;
		SquakeClientPlayer.afterJump(asPlayer);
	}

	private boolean wasVelocityChangedBeforeFall = false;

	@Inject(
			method = "causeFallDamage",
			at = @At("HEAD")
	)
	public void beforeFall(float distance, float damageMultiplier, CallbackInfoReturnable<Boolean> cir)
	{
		if(level.isClientSide) return;
		wasVelocityChangedBeforeFall = hasImpulse;
	}

	@Inject(
			method = "causeFallDamage",
			at = @At("RETURN"),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;awardStat(Lnet/minecraft/util/ResourceLocation;I)V"),
					to = @At("TAIL")
			)
	)
	public void afterFall(float distance, float damageMultiplier, CallbackInfoReturnable<Boolean> cir)
	{
		if(level.isClientSide) return;
		hasImpulse = wasVelocityChangedBeforeFall;
	}
}