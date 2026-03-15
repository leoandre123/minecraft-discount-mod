package com.leodenandre.discount.mixin.client;

import net.minecraft.entity.passive.MerchantEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantEntity.class)
public class ExampleClientMixin {

	@Inject(at = @At("RETURN"), method = "canBeLeashed", cancellable = true)
	public void canBeLeashed(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

}