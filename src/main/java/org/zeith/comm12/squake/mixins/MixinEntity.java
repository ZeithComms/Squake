package org.zeith.comm12.squake.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zeith.comm12.squake.SquakeClientPlayer;

@Mixin(Entity.class)
public class MixinEntity
{
	@Inject(
			method = "moveRelative",
			at = @At("HEAD"),
			cancellable = true
	)
	public void moveRelativeBase(float friction, Vec3 relative, CallbackInfo ci)
	{
		if(SquakeClientPlayer.moveRelativeBase((Entity) (Object) this, (float) relative.x, (float) relative.y, (float) relative.z, friction))
			ci.cancel();
	}
}