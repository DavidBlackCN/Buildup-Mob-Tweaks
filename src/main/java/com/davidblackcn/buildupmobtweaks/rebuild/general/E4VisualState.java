package com.davidblackcn.buildupmobtweaks.rebuild.general;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.FeatureId;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Mob;
public final class E4VisualState {
    public static void bootstrap(){}
    public static final AttachmentType<Integer> FLAGS=AttachmentRegistry.create(BuildupMobTweaks.id("e4_visual"),b->b.initializer(()->0).syncWith(ByteBufCodecs.VAR_INT,AttachmentSyncPredicate.all()));
    public static void update(Mob m){
        int bits=0;
        if(E4Behavior.enabled(m,FeatureId.BURN_FREEZE_VISUAL)){if(m.isOnFire()&&!m.fireImmune())bits|=1;else if(m.isFullyFrozen())bits|=2;}
        if(m.getType()==net.minecraft.world.entity.EntityTypes.DROWNED&&E4Behavior.enabled(m,FeatureId.DROWNED_SWIM_VISUAL))bits|=4;
        if(bits==0&&!m.hasAttached(FLAGS))return;
        if(bits!=m.getAttachedOrCreate(FLAGS))m.setAttached(FLAGS,bits);
    }
    private E4VisualState(){}
}
