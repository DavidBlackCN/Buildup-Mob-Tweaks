package com.davidblackcn.buildupmobtweaks.client.rebuild;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.FishingHook;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
public final class NpcFishingClientHooks {
    private static final WeakHashMap<Villager,WeakReference<FishingHook>> HOOKS=new WeakHashMap<>();
    public static void observe(Villager v,FishingHook h){HOOKS.put(v,new WeakReference<>(h));}
    public static boolean cast(Villager v){var ref=HOOKS.get(v);var h=ref==null?null:ref.get();return h!=null&&!h.isRemoved()&&h.getOwner()==v;}
    private NpcFishingClientHooks(){}
}
