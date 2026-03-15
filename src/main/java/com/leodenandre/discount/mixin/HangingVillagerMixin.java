package com.leodenandre.discount.mixin;

import com.leodenandre.discount.state.VillagerDiscountManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface HangingVillagerMixin {

	@Inject(at = @At("TAIL"), method = "onLongLeashTick")
	public default void checkHanging(CallbackInfo ci) {
		Entity leashedEntity = (Entity)(Object)this;
		if(leashedEntity instanceof VillagerEntity villager){
			if(VillagerDiscountManager.isLeashDangling(villager)){
				var serverWorld = ((ServerWorld)villager.getWorld());
				villager.damage(serverWorld,serverWorld.getDamageSources().drown(),1.0f); // 2 health = 1 heart
			}
		}

	}

}