package com.davidblackcn.buildupmobtweaks.client.rebuild;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.EntityTypes;

/** These five native renderers share their adult body and equipment meshes at both ages. */
public final class SkeletonBabyVisuals {
    public static boolean usesAdultMesh(LivingEntityRenderState state){
        return state instanceof SkeletonRenderState&&(state.entityType==EntityTypes.SKELETON
            ||state.entityType==EntityTypes.STRAY||state.entityType==EntityTypes.BOGGED
            ||state.entityType==EntityTypes.WITHER_SKELETON||state.entityType==EntityTypes.PARCHED);
    }
    private SkeletonBabyVisuals(){}
}
