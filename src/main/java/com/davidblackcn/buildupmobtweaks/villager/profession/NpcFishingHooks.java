package com.davidblackcn.buildupmobtweaks.villager.profession;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

/** Native projectile physics and native bite timing, with a separate living-villager owner. */
public final class NpcFishingHooks {
    public static boolean marked(FishingHook h){return Boolean.TRUE.equals(h.getAttached(WorkProjectiles.HOOK));}
    public static void tick(FishingHook h){
        if(!(h.getOwner() instanceof Villager v)){h.discard();return;}
        if(h.level() instanceof ServerLevel l){
            var r=ProfessionWorkState.peek(v).orElse(null);
            if(r==null||r.projectile!=h||!(r.active instanceof FishWork)||!ProfessionModule.allowed(v,l,r.context.profile(),false)||v.distanceToSqr(h)>256||h.tickCount>1100){h.discard();return;}
        }
        var p=h.blockPosition();if(!h.level().hasChunkAt(p)){h.discard();return;}
        var fluid=h.level().getFluidState(p);
        if(fluid.is(FluidTags.WATER)){
            double surface=p.getY()+fluid.getHeight(h.level(),p)-.1;
            var speed=h.getDeltaMovement();h.setDeltaMovement(speed.x*.5,(surface-h.getY())*.25,speed.z*.5);h.move(MoverType.SELF,h.getDeltaMovement());
            if(!h.level().isClientSide()){
                for(int dx=-6;dx<=6;dx+=6)for(int dz=-6;dz<=6;dz+=6)if(!h.level().hasChunkAt(p.offset(dx,0,dz))){h.discard();return;}
                ((NpcFishingAccess)h).buildup$naturalBite(h.blockPosition());
            }
        }else{
            if(!h.level().isClientSide())((NpcFishingAccess)h).buildup$workCollision();
            h.move(MoverType.SELF,h.getDeltaMovement());h.setDeltaMovement(h.getDeltaMovement().add(0,-.03,0).scale(.98));
        }
    }
    private NpcFishingHooks(){}
}
