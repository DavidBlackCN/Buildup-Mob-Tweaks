package com.davidblackcn.buildupmobtweaks.compat.test_profession;
import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import com.davidblackcn.buildupmobtweaks.villager.*;
import com.davidblackcn.buildupmobtweaks.villager.profession.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.Items;
import java.util.List;
/** Test-only example: the optional factory is loaded only for the exact installed mod version. */
public final class WorkerAdapter {
 public static ProfessionAdapter create(){var key=ResourceKey.create(Registries.VILLAGER_PROFESSION,Identifier.fromNamespaceAndPath("buildupmobtweaks-test","worker"));return new ProfessionAdapter(BuildupMobTweaks.id("test/worker"),new ProfessionProfile(BuildupMobTweaks.id("test_worker"),key,PoiTypes.LIBRARIAN,12),List.of(new ProfessionAdapter.Entry(BuildupMobTweaks.id("test_read"),0,()->new WorkCycle("test_read",Items.BOOK))),false);}
 private WorkerAdapter(){}
}
