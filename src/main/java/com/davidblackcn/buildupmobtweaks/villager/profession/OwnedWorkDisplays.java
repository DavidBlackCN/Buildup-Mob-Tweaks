package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.WorkActivity;
import com.davidblackcn.buildupmobtweaks.villager.farmer.InteractionPolicy;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** Paid physical displays. Refund only from a still-proven carrier, never from a missing receipt. */
public final class OwnedWorkDisplays {
    public static final AttachmentType<CompoundTag> LEDGER=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("villager_displays_v1"),CompoundTag.CODEC);
    public static final AttachmentType<CompoundTag> MARKER=AttachmentRegistry.createPersistent(BuildupMobTweaks.id("villager_display_instance_v1"),CompoundTag.CODEC);
    public record Plan(String kind,BlockPos pos,Direction face,Item prop,EquipmentSlot slot){}
    public static boolean protectedEntity(Entity e){return (e instanceof ArmorStand||e instanceof ItemFrame)&&e.hasAttached(MARKER);}
    private static final Set<String> FIELDS=Set.of("version","uuid","owner","dimension","pos","job","profession","kind","item");
    private static UUID uuid(String s){try{return UUID.fromString(s);}catch(IllegalArgumentException ex){return null;}}
    private static boolean markerKnown(CompoundTag d){return d!=null&&d.keySet().equals(FIELDS)&&d.getIntOr("version",-1)==1&&uuid(d.getStringOr("uuid",""))!=null&&uuid(d.getStringOr("owner",""))!=null&&Identifier.tryParse(d.getStringOr("dimension",""))!=null&&Identifier.tryParse(d.getStringOr("profession",""))!=null&&Identifier.tryParse(d.getStringOr("item",""))!=null&&Set.of("frame","stand","block").contains(d.getStringOr("kind",""))&&d.getLong("pos").isPresent()&&d.getLong("job").isPresent();}
    public static boolean known(Villager v){if(!v.hasAttached(LEDGER))return true;var d=v.getAttached(LEDGER);return d.keySet().equals(Set.of("version","instances"))&&d.getIntOr("version",-1)==1&&d.getCompound("instances").filter(c->c.keySet().stream().allMatch(k->Set.of("frame","stand","block").contains(k)&&c.getCompound(k).filter(OwnedWorkDisplays::markerKnown).isPresent())).isPresent();}
    private static CompoundTag instances(Villager v){return v.hasAttached(LEDGER)?v.getAttached(LEDGER).getCompound("instances").orElseGet(CompoundTag::new):new CompoundTag();}
    private static void put(Villager v,String kind,CompoundTag marker){var d=new CompoundTag();d.putInt("version",1);var i=instances(v).copy();i.put(kind,marker.copy());d.put("instances",i);v.setAttached(LEDGER,d);}
    private static void forget(Villager v,String kind){if(!known(v))return;var d=v.getAttached(LEDGER).copy();var i=instances(v).copy();i.remove(kind);d.put("instances",i);v.setAttached(LEDGER,d);}
    public static boolean reserved(Villager v,String kind){return !known(v)||instances(v).contains(kind);}
    private static Item base(String kind){return kind.equals("frame")?Items.ITEM_FRAME:kind.equals("stand")?Items.ARMOR_STAND:Items.DECORATED_POT;}
    private static int stock(Villager v,Item item){for(int i=0;i<v.getInventory().getContainerSize();i++){var s=v.getInventory().getItem(i);if(!s.isEmpty()&&s.is(item)&&ItemStack.isSameItemSameComponents(s,new ItemStack(item)))return i;}return -1;}
    private static boolean configured(String kind){var c=BuildupMobTweaks.config().villagers.professions;return c.enabled.get()&&switch(kind){case "frame"->c.decoration_frames.get();case "stand"->c.decoration_stands.get();case "block"->c.decoration_blocks.get();default->false;};}
    private static Item prop(Villager v,String kind){String p=v.getVillagerData().profession().unwrapKey().orElseThrow().identifier().getPath();if(kind.equals("block"))return Items.DECORATED_POT;if(kind.equals("stand"))return switch(p){case "armorer"->Items.IRON_HELMET;case "weaponsmith"->Items.IRON_SWORD;case "toolsmith"->Items.IRON_PICKAXE;default->Items.LEATHER_CHESTPLATE;};return switch(p){case "cartographer"->Items.MAP;case "fletcher"->Items.ARROW;default->Items.BOOK;};}
    private static EquipmentSlot slot(Item prop){return prop==Items.IRON_HELMET?EquipmentSlot.HEAD:prop==Items.LEATHER_CHESTPLATE?EquipmentSlot.CHEST:EquipmentSlot.MAINHAND;}
    public static Plan find(WorkActivity.Context c,String kind){
        var v=c.villager();if(!configured(kind)||reserved(v,kind)||stock(v,base(kind))<0)return null;var prop=prop(v,kind);if(!kind.equals("block")&&stock(v,prop)<0)return null;
        var job=v.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).orElseThrow().pos();
        if(kind.equals("frame")){
            for(var wall:BlockPos.betweenClosed(job.offset(-2,0,-2),job.offset(2,1,2)))for(var face:Direction.Plane.HORIZONTAL){if(ProfessionModule.scanBudget(c.level(),1)==0)return null;var p=wall.relative(face);if(!c.level().hasChunkAt(p)||!c.level().getBlockState(p).isAir()||!c.level().getBlockState(wall).isFaceSturdy(c.level(),wall,face))continue;var frame=new ItemFrame(c.level(),p,face);if(frame.survives()&&c.level().getEntitiesOfClass(ItemFrame.class,frame.getBoundingBox()).isEmpty()&&InteractionPolicy.allows(c.level(),v,p,InteractionPolicy.Operation.DISPLAY,true))return new Plan(kind,p.immutable(),face,prop,slot(prop));}
        }else{
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){if(ProfessionModule.scanBudget(c.level(),1)==0)return null;var p=job.offset(dx,0,dz);if(!WorkMotion.clearFoot(c.level(),p)||!c.level().getBlockState(p).isAir()||!c.level().getBlockState(p.above()).isAir()||!c.level().getEntities(null,new AABB(Vec3.atLowerCornerOf(p),Vec3.atLowerCornerOf(p.above()).add(1,1,1))).isEmpty()||!InteractionPolicy.allows(c.level(),v,p,InteractionPolicy.Operation.DISPLAY,true))continue;return new Plan(kind,p,Direction.NORTH,prop,slot(prop));}
        }
        return null;
    }
    private static CompoundTag marker(WorkActivity.Context c,Plan plan,UUID id){var d=new CompoundTag();d.putInt("version",1);d.putString("uuid",id.toString());d.putString("owner",c.villager().getUUID().toString());d.putString("dimension",c.level().dimension().identifier().toString());d.putLong("pos",plan.pos().asLong());d.putLong("job",c.villager().getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).orElseThrow().pos().asLong());d.putString("profession",c.profile().profession().identifier().toString());d.putString("kind",plan.kind());d.putString("item",BuiltInRegistries.ITEM.getKey(plan.prop()).toString());return d;}
    private static void give(ServerLevel l,Villager v,ItemStack s){if(!v.isAlive()||v.isRemoved()){l.addFreshEntity(new ItemEntity(l,v.getX(),v.getY()+.5,v.getZ(),s));return;}var left=v.getInventory().addItem(s);if(!left.isEmpty()){var world=(ServerLevel)v.level();world.addFreshEntity(new ItemEntity(world,v.getX(),v.getY()+.5,v.getZ(),left));}}
    public static boolean place(WorkActivity.Context c,Plan plan){
        var v=c.villager();var l=c.level();if(reserved(v,plan.kind())||!configured(plan.kind())||!l.hasChunkAt(plan.pos())||!l.getBlockState(plan.pos()).isAir()||!InteractionPolicy.allows(l,v,plan.pos(),InteractionPolicy.Operation.DISPLAY,true))return false;
        if(plan.kind().equals("frame")){var candidate=new ItemFrame(l,plan.pos(),plan.face());if(!candidate.survives()||!l.getEntitiesOfClass(ItemFrame.class,candidate.getBoundingBox()).isEmpty())return false;}else if(!WorkMotion.clearFoot(l,plan.pos())||!l.getBlockState(plan.pos().above()).isAir()||!l.getEntities(null,new AABB(Vec3.atLowerCornerOf(plan.pos()),Vec3.atLowerCornerOf(plan.pos().above()).add(1,1,1))).isEmpty())return false;
        int from=stock(v,base(plan.kind())),item=plan.kind().equals("block")?-1:stock(v,plan.prop());if(from<0||!plan.kind().equals("block")&&item<0)return false;
        var paidBase=v.getInventory().removeItem(from,1);var paidProp=item<0?ItemStack.EMPTY:v.getInventory().removeItem(item,1);
        if(plan.kind().equals("block")){
            if(!l.setBlock(plan.pos(),Blocks.DECORATED_POT.defaultBlockState(),3)){give(l,v,paidBase);return false;}
            if(!(l.getBlockEntity(plan.pos()) instanceof DecoratedPotBlockEntity pot))return false;
            var data=marker(c,plan,UUID.randomUUID());pot.setAttached(MARKER,data);pot.setChanged();put(v,plan.kind(),data);return true;
        }
        Entity display;
        if(plan.kind().equals("frame")){var frame=new ItemFrame(l,plan.pos(),plan.face());frame.setItem(paidProp,false);display=frame;}
        else{var stand=new ArmorStand(l,plan.pos().getX()+.5,plan.pos().getY(),plan.pos().getZ()+.5);stand.setItemSlot(plan.slot(),paidProp);stand.setShowArms(true);stand.setNoGravity(true);((OwnedStandAccess)stand).buildup$displayMarker(true);display=stand;}
        var data=marker(c,plan,display.getUUID());display.setAttached(MARKER,data);
        if(!l.addFreshEntity(display)){give(l,v,paidBase);give(l,v,paidProp);return false;}
        put(v,plan.kind(),data);return true;
    }
    private static boolean sameEntity(Entity e,CompoundTag d){
        if(!e.hasAttached(MARKER)||!d.equals(e.getAttached(MARKER))||e.getCustomName()!=null)return false;
        var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(d.getStringOr("item","minecraft:air")));var expected=new ItemStack(item);
        if(e instanceof ItemFrame f)return ItemStack.matches(f.getItem(),expected);
        if(e instanceof ArmorStand a){var slot=slot(item);for(var s:EquipmentSlot.VALUES)if(s==slot?!ItemStack.matches(a.getItemBySlot(s),expected):!a.getItemBySlot(s).isEmpty())return false;return a.isMarker();}
        return false;
    }
    private static boolean keeper(Villager v,CompoundTag d){
        if(!BuildupMobTweaks.config().general.enabled.get()||!BuildupMobTweaks.config().villagers.enabled.get()||!configured(d.getStringOr("kind",""))||!ProfessionWorkState.known(v))return false;
        if(v.entityTags().contains("buildupmobtweaks:profession_external")||v.entityTags().contains("buildupmobtweaks:disable_villager_work"))return false;
        return v.isAlive()&&!v.isRemoved()&&v.getVillagerData().profession().unwrapKey().map(k->k.identifier().toString().equals(d.getStringOr("profession",""))).orElse(false)
                &&v.getBrain().checkMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE,net.minecraft.world.entity.ai.memory.MemoryStatus.REGISTERED)
                &&v.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE).filter(j->j.dimension().identifier().toString().equals(d.getStringOr("dimension",""))&&j.pos().asLong()==d.getLongOr("job",0)).isPresent();
    }
    public static void boundary(Villager v,ServerLevel current){if(v.tickCount%20==0)reconcile(v,current,false);}
    public static void retire(Villager v,ServerLevel current){reconcile(v,current,true);}
    private static void reconcile(Villager v,ServerLevel current,boolean retire){if(!v.hasAttached(LEDGER)||!known(v)||!ProfessionWorkState.known(v))return;for(var kind:List.copyOf(instances(v).keySet())){
        var d=instances(v).getCompound(kind).orElseThrow();if(!markerKnown(d))continue;var l=current.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse(d.getStringOr("dimension",""))));if(l==null)continue;var p=BlockPos.of(d.getLongOr("pos",0));if(!l.hasChunkAt(p))continue;
        if(kind.equals("block")){
            var be=l.getBlockEntity(p);if(be==null||!be.hasAttached(MARKER)||!d.equals(be.getAttached(MARKER))){forget(v,kind);continue;}
            if(!(be instanceof DecoratedPotBlockEntity pot)||!pot.getTheItem().isEmpty()||pot.getLootTable()!=null||!pot.getDecorations().equals(PotDecorations.EMPTY)||!pot.components().isEmpty()||!l.getBlockState(p).equals(Blocks.DECORATED_POT.defaultBlockState())){be.removeAttached(MARKER);be.setChanged();forget(v,kind);continue;}
            if((retire||!keeper(v,d))&&InteractionPolicy.allows(l,v,p,InteractionPolicy.Operation.DISPLAY,true)){if(l.setBlock(p,Blocks.AIR.defaultBlockState(),3)){forget(v,kind);give(l,v,new ItemStack(Items.DECORATED_POT));}}
        }else{
            var entity=l.getEntity(uuid(d.getStringOr("uuid","")));if(entity==null){if(l.areEntitiesLoaded(ChunkPos.pack(p)))forget(v,kind);continue;}
            if(!sameEntity(entity,d)){entity.removeAttached(MARKER);forget(v,kind);continue;}
            if((retire||!keeper(v,d))&&InteractionPolicy.allows(l,v,p,InteractionPolicy.Operation.DISPLAY,true)){
                forget(v,kind);ItemStack actual;if(entity instanceof ItemFrame f){actual=f.getItem();f.setItem(ItemStack.EMPTY,false);}else{var a=(ArmorStand)entity;var slot=slot(BuiltInRegistries.ITEM.getValue(Identifier.parse(d.getStringOr("item",""))));actual=a.getItemBySlot(slot);a.setItemSlot(slot,ItemStack.EMPTY);}entity.discard();give(l,v,new ItemStack(base(kind)));give(l,v,actual);
            }
        }
    }}
    /** Register persistent attachment IDs before any world/entity deserialization. */
    public static void bootstrap(){}
    private OwnedWorkDisplays(){}
}
