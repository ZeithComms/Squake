package org.zeith.comm12.squake.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.comm12.squake.ISquakeEntity;
import org.zeith.comm12.squake.SquakeClientPlayer;

@Mixin(Entity.class)
@Implements({
		@Interface(iface = ISquakeEntity.class, prefix = "sqe$")
})
public abstract class MixinEntity implements ISquakeEntity
{
	private int squakeDisableMovementTicks;
	
	public int sqe$getDisabledMovementTicks_Squake()
	{
		return squakeDisableMovementTicks;
	}
	
	public void sqe$setDisabledMovementTicks_Squake(int amt)
	{
		squakeDisableMovementTicks = amt;
	}
	
	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	public void beforeOnLivingUpdate(CallbackInfo ci)
	{
		if(squakeDisableMovementTicks > 0)
			--squakeDisableMovementTicks;
	}
	
	@Inject(
			method = "moveRelative",
			at = @At("HEAD"),
			cancellable = true
	)
	public void moveRelativeBase(float friction, Vec3 relative, CallbackInfo ci)
	{
		if(SquakeClientPlayer.moveRelativeBase((Entity) (Object) this, this, (float) relative.x, (float) relative.y, (float) relative.z, friction))
			ci.cancel();
	}
	
	@Inject(
			method = "onInsideBubbleColumn",
			at = @At("HEAD")
	)
	public void onInsideBubbleColumn(boolean p_20322_, CallbackInfo ci)
	{
		squakeDisableMovementTicks = 2;
	}
}