/* Mob AI Tweaks relationship adaptation: Copyright (c) 2024 N0t_UN_Owen, MIT. See NOTICE.md. */
package com.davidblackcn.buildupmobtweaks.rebuild.relations;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.feature.*;
import com.davidblackcn.buildupmobtweaks.mixin.rebuild.RemainingTargetAccess;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import java.util.*;

public final class RelationBehavior {
    private static RelationBehavior instance;
    private final FeatureRegistry features;
    private static final net.minecraft.tags.TagKey<EntityType<?>> EXCLUDED=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,BuildupMobTweaks.id("relations_ai_excluded"));
    private static final AttachmentType<State> RUNTIME=AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("relation_runtime"),State::new);
    public static final class State { public boolean installed; public int queries,acquisitions,exits; public long nextQuery; public LivingEntity owned; }
    public RelationBehavior(FeatureRegistry features){this.features=features;}
    public static RelationBehavior instance(){return instance;}
    public static State state(Mob m){return m.getAttachedOrCreate(RUNTIME);}
    public static boolean illager(Mob m){return m.getType()==EntityTypes.PILLAGER||m.getType()==EntityTypes.VINDICATOR||m.getType()==EntityTypes.EVOKER||m.getType()==EntityTypes.ILLUSIONER;}
    public static boolean supported(Mob m){return illager(m)||m.getType()==EntityTypes.ZOMBIE||m.getType()==EntityTypes.ZOMBIE_VILLAGER;}
    public static boolean unknownState(Mob m){return m.hasAttached(com.davidblackcn.buildupmobtweaks.rebuild.illusion.IllusionState.DATA)&&!com.davidblackcn.buildupmobtweaks.rebuild.illusion.IllusionState.known(m)
            ||m instanceof net.minecraft.world.entity.monster.zombie.Zombie z&&z.hasAttached(com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieState.DATA)&&!com.davidblackcn.buildupmobtweaks.rebuild.zombie.ZombieState.known(z)
            ||m instanceof net.minecraft.world.entity.monster.skeleton.AbstractSkeleton s&&s.hasAttached(com.davidblackcn.buildupmobtweaks.rebuild.skeleton.SkeletonState.DATA)&&!com.davidblackcn.buildupmobtweaks.rebuild.skeleton.SkeletonState.known(s);}
    public boolean enabled(Mob m,FeatureId id){return features.isEnabled(id)&&!m.getType().builtInRegistryHolder().is(EXCLUDED)&&!m.entityTags().contains("buildupmobtweaks:vanilla_ai")&&!m.entityTags().contains("buildupmobtweaks:disable_"+id.id().getPath());}
    public boolean refuseGolemRevenge(Mob m,LivingEntity t){return m.getType()==EntityTypes.IRON_GOLEM&&t!=null&&(t.getType()==EntityTypes.IRON_GOLEM||t.getType()==EntityTypes.SNOW_GOLEM)&&enabled(m,FeatureId.GOLEM_FRIENDLY_FIRE_FIX);}
    public static boolean valid(Mob m,LivingEntity t){return t!=null&&t.isAlive()&&!t.isRemoved()&&t.level()==m.level()&&!m.isAlliedTo(t)&&m.canAttack(t)&&(!(t instanceof Player p)||!p.isCreative()&&!p.isSpectator());}
    private static boolean curing(Mob m){return m instanceof ZombieVillager v&&v.isConverting();}
    public boolean related(Mob m,Mob t){
        if(!supported(m)||!supported(t)||curing(m)||curing(t))return false;
        Mob zombie=illager(m)?t:m;
        if(illager(m)==illager(t))return false;
        var id=zombie.getType()==EntityTypes.ZOMBIE?FeatureId.ILLAGER_ZOMBIE_CONFLICT:FeatureId.ILLAGER_ZOMBIE_VILLAGER;
        return enabled(m,id)&&valid(m,t);
    }
    public void register(){instance=this;
        ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof Mob m&&supported(m)&&!unknownState(m)){var s=state(m);if(!s.installed){s.installed=true;((RemainingTargetAccess)m).buildup$targets().addGoal(6,new Relations(m));}}});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,l)->{if(e instanceof Mob m&&m.hasAttached(RUNTIME))exit(m);});
    }
    public void boundary(Mob m){if(!m.hasAttached(RUNTIME))return;var s=state(m);if(s.owned!=null&&(!m.isAlive()||m.isNoAi()||!(s.owned instanceof Mob other)||!related(m,other)))exit(m);}
    private static void exit(Mob m){var s=state(m);if(s.owned!=null){if(m.getTarget()==s.owned)m.setTarget(null);s.owned=null;s.exits++;}}
    private final class Relations extends Goal {
        private final Mob mob;private Mob candidate;
        Relations(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.TARGET));}
        public boolean canUse(){var s=state(mob);if(mob.isNoAi()||!mob.isAlive()||unknownState(mob)||!enabled(mob,FeatureId.ILLAGER_ZOMBIE_CONFLICT)&&!enabled(mob,FeatureId.ILLAGER_ZOMBIE_VILLAGER)||valid(mob,mob.getTarget())||mob.level().getGameTime()<s.nextQuery)return false;
            s.nextQuery=mob.level().getGameTime()+20;s.queries++;
            double radius=Math.min(16,mob.getAttributeValue(Attributes.FOLLOW_RANGE));var found=new ArrayList<Mob>();
            mob.level().getEntities(EntityTypeTest.forClass(Mob.class),mob.getBoundingBox().inflate(radius),t->related(mob,t)&&mob.distanceToSqr(t)<=radius*radius&&mob.hasLineOfSight(t),found,16);
            candidate=found.stream().min(Comparator.comparingDouble(mob::distanceToSqr)).orElse(null);return candidate!=null;
        }
        public boolean canContinueToUse(){return !mob.isNoAi()&&mob.getTarget()==candidate&&candidate!=null&&related(mob,candidate)&&mob.distanceToSqr(candidate)<=Math.pow(Math.min(16,mob.getAttributeValue(Attributes.FOLLOW_RANGE)),2)&&mob.hasLineOfSight(candidate);}
        public void start(){mob.setTarget(candidate);state(mob).owned=candidate;state(mob).acquisitions++;}
        public void stop(){exit(mob);candidate=null;}
    }
}
