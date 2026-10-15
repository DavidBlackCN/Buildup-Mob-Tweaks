package com.davidblackcn.buildupmobtweaks.villager.profession;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.*;
import net.minecraft.world.item.*;
import java.util.List;

final class BuiltinProfessions {
    static void add(String name,ResourceKey<VillagerProfession> profession,ResourceKey<PoiType> poi,Item prop){
        var profile=new ProfessionProfile(BuildupMobTweaks.id(name),profession,poi,12);
        ProfessionRegistry.register(new ProfessionAdapter(BuildupMobTweaks.id("vanilla/"+name),profile,
                entries(name,prop),false));
    }
    static List<ProfessionAdapter.Entry> entries(String name,Item prop){
        var out=new java.util.ArrayList<ProfessionAdapter.Entry>();
        if(java.util.Set.of("armorer","toolsmith","weaponsmith").contains(name))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("repair_golem"),100,RepairWork::new));
        if(name.equals("fisherman"))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("fish"),100,FishWork::new));
        if(name.equals("cleric"))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("heal_villager"),100,HealVillagerWork::new));
        if(name.equals("shepherd"))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("shear_sheep"),100,ShearWork::new));
        if(java.util.Set.of("librarian","cartographer","fletcher").contains(name))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("display_frame"),50,()->new DisplayWork("frame")));
        if(java.util.Set.of("armorer","toolsmith","weaponsmith","leatherworker").contains(name))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("display_stand"),50,()->new DisplayWork("stand")));
        if(name.equals("mason"))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("display_block"),50,()->new DisplayWork("block")));
        if(name.equals("fletcher"))out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id("archery_training"),100,ArcheryWork::new));
        out.add(new ProfessionAdapter.Entry(BuildupMobTweaks.id(name+"_work"),0,()->name.equals("cartographer")?new CartographyWork():name.equals("butcher")?new ButcherInspectionWork():new WorkCycle(name+"_work",prop)));return List.copyOf(out);
    }
    static void register(){
        add("armorer",VillagerProfession.ARMORER,PoiTypes.ARMORER,Items.IRON_INGOT);
        add("butcher",VillagerProfession.BUTCHER,PoiTypes.BUTCHER,Items.BOWL);
        add("cartographer",VillagerProfession.CARTOGRAPHER,PoiTypes.CARTOGRAPHER,Items.MAP);
        add("cleric",VillagerProfession.CLERIC,PoiTypes.CLERIC,Items.GLASS_BOTTLE);
        add("fisherman",VillagerProfession.FISHERMAN,PoiTypes.FISHERMAN,Items.FISHING_ROD);
        add("fletcher",VillagerProfession.FLETCHER,PoiTypes.FLETCHER,Items.ARROW);
        add("leatherworker",VillagerProfession.LEATHERWORKER,PoiTypes.LEATHERWORKER,Items.LEATHER);
        add("librarian",VillagerProfession.LIBRARIAN,PoiTypes.LIBRARIAN,Items.BOOK);
        add("mason",VillagerProfession.MASON,PoiTypes.MASON,Items.STONE);
        add("shepherd",VillagerProfession.SHEPHERD,PoiTypes.SHEPHERD,Items.SHEARS);
        add("toolsmith",VillagerProfession.TOOLSMITH,PoiTypes.TOOLSMITH,Items.IRON_PICKAXE);
        add("weaponsmith",VillagerProfession.WEAPONSMITH,PoiTypes.WEAPONSMITH,Items.IRON_SWORD);
        ProfessionRegistry.register(new ProfessionAdapter(BuildupMobTweaks.id("vanilla/farmer"),com.davidblackcn.buildupmobtweaks.villager.farmer.FarmerModule.profile(),List.of(new ProfessionAdapter.Entry(BuildupMobTweaks.id("feed_baby_animal"),100,FeedBabyWork::new)),true));
    }
    private BuiltinProfessions(){}
}
