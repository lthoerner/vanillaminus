package com.lthoerner.vanillaminus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lthoerner.vanillaminus.VanillaMinusShearable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin extends HostileEntity implements Shearable, VanillaMinusShearable {
	@Unique
    private static final TrackedData<Boolean> SHEARED = DataTracker.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	protected void vanillaminus$addShearedDataTracker(DataTracker.Builder builder, CallbackInfo info) {
		builder.add(SHEARED, false);
	}

	@Override
	public boolean isShearable() {
		return this.isAlive() && !this.isSheared();
	}

	@Override
	public void sheared(SoundCategory shearedSoundCategory) {
		this.getWorld().playSoundFromEntity(null, this, SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0f, 1.0f);
		this.setSheared(true);
		ItemEntity tntItem = this.dropItem(Items.TNT, 1);
		tntItem.setVelocity(tntItem.getVelocity().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1f, this.random.nextFloat() * 0.05f, (this.random.nextFloat() - this.random.nextFloat()) * 0.1f));
	}

	public boolean isSheared() {
		return this.dataTracker.get(SHEARED);
	}

	@Unique
	private void setSheared(boolean sheared) {
		this.dataTracker.set(SHEARED, sheared);
	}

	@Inject(
			method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void vanillaminus$makeCreeperShearable(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (itemStack.isOf(Items.SHEARS)) {
			if (!this.getWorld().isClient && this.isShearable()) {
				this.sheared(SoundCategory.PLAYERS);
				this.emitGameEvent(GameEvent.SHEAR, player);
				itemStack.damage(1, player, CreeperEntity.getSlotForHand(hand));
				info.setReturnValue(ActionResult.SUCCESS);
			}
			info.setReturnValue(ActionResult.CONSUME);
		}
	}

	@ModifyExpressionValue(
			method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
			at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.isIn (Lnet/minecraft/registry/tag/TagKey;)Z")
	)
	private boolean vanillaminus$preventManualIgnitionIfSheared(boolean original) {
		return original && !this.isSheared();
	}

	@Inject(
			method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V",
			at = @At(value = "TAIL")
	)
	private void vanillaminus$addShearedNbt(NbtCompound nbt, CallbackInfo info) {
		nbt.putBoolean("Sheared", this.isSheared());
	}

	@Inject(
			method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V",
			at = @At(value = "TAIL")
	)
	private void vanillaminus$setShearedNbt(NbtCompound nbt, CallbackInfo info) {
		this.setSheared(nbt.getBoolean("Sheared"));
	}

	@ModifyArg(
			method = "ignite()V",
			at = @At(value = "INVOKE", target = "java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;")
	)
	private boolean vanillaminus$preventIgnitionIfSheared(boolean original) {
		return !this.isSheared();
	}
}