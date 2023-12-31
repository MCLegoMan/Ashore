/*
    Ashore
    Contributor(s): MCLegoMan
    Github: https://github.com/MCLegoMan/Ashore
    License: LGPL-3.0-or-later
*/

package com.mclegoman.ashore.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity {
	@Shadow private float velocityDecay;
	@Shadow private BoatEntity.Location location;
	@Shadow private BoatEntity.Location lastLocation;
	@Shadow private double waterLevel;
	@Shadow private float yawVelocity;
	@Shadow
	public abstract float getWaterLevelAbove();
	@Shadow
	private double fallVelocity;
	@Shadow
	private float landFriction;
	public BoatEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}
	@Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
	private void updateVelocity(CallbackInfo ci) {
		double d = -0.03999999910593033;
		double e = this.hasNoGravity() ? 0.0 : d;
		double f = 0.0;
		this.velocityDecay = 0.05F;
		if (this.lastLocation == BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.ON_LAND) {
			this.waterLevel = this.getBodyY(1.0);
			this.setPosition(this.getX(), (double)(this.getWaterLevelAbove() - this.getHeight()) + 0.101, this.getZ());
			this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
			this.fallVelocity = 0.0;
			this.location = BoatEntity.Location.IN_WATER;
		} else {
			if (this.location == BoatEntity.Location.IN_WATER) {
				f = (this.waterLevel - this.getY()) / (double)this.getHeight();
				this.velocityDecay = 0.6F;
			} else if (this.location == BoatEntity.Location.UNDER_FLOWING_WATER) {
				e = -7.0E-4;
				this.velocityDecay = 0.6F;
			} else if (this.location == BoatEntity.Location.UNDER_WATER) {
				f = 0.009999999776482582;
				this.velocityDecay = 0.45F;
			} else if (this.location == BoatEntity.Location.IN_AIR) {
				this.velocityDecay = 0.9F;
			} else if (this.location == BoatEntity.Location.ON_LAND) {
				this.velocityDecay = this.landFriction > 0.9F ? this.landFriction : 0.9F;
				if (this.getPrimaryPassenger() instanceof PlayerEntity) {
					this.landFriction /= 2.0F;
				}
			}
			this.velocityDecay = Math.min(this.velocityDecay, 1.0F);
			Vec3d vec3d = this.getVelocity();
			this.setVelocity(vec3d.x * (double)this.velocityDecay, vec3d.y + e, vec3d.z * (double)this.velocityDecay);
			this.yawVelocity *= this.velocityDecay;
			if (f > 0.0) {
				Vec3d vec3d2 = this.getVelocity();
				this.setVelocity(vec3d2.x, (vec3d2.y + f * 0.06153846016296973) * 0.75, vec3d2.z);
			}
		}
		ci.cancel();
	}
}
