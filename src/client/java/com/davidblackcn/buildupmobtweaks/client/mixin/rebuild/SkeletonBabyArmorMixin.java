package com.davidblackcn.buildupmobtweaks.client.mixin.rebuild;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The native skeleton renderer uses the same adult armor mesh at both ages. */
@Mixin(HumanoidArmorLayer.class)
public abstract class SkeletonBabyArmorMixin {
    @ModifyExpressionValue(method="renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
        at=@At(value="FIELD",target="Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;HUMANOID_BABY:Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;"))
    private EquipmentClientInfo.LayerType buildup$skeletonArmorUv(EquipmentClientInfo.LayerType original,PoseStack pose,SubmitNodeCollector collector,ItemStack item,EquipmentSlot slot,int light,HumanoidRenderState state){
        if(!(state instanceof SkeletonRenderState)||!(state.entityType==EntityTypes.SKELETON||state.entityType==EntityTypes.STRAY||state.entityType==EntityTypes.BOGGED||state.entityType==EntityTypes.WITHER_SKELETON||state.entityType==EntityTypes.PARCHED))return original;
        // Retain physical age, model scale and equipment; change only the matching texture layout.
        return slot==EquipmentSlot.LEGS?EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS:EquipmentClientInfo.LayerType.HUMANOID;
    }
}
