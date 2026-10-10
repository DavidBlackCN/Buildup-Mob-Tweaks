package com.davidblackcn.buildupmobtweaks.rebuild.ecology;

import com.davidblackcn.buildupmobtweaks.BuildupMobTweaks;
import net.fabricmc.fabric.api.attachment.v1.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Cooldowns survive reload; actions and references deliberately do not. */
public final class EcologyState {
    public static final AttachmentType<CompoundTag> DATA = AttachmentRegistry.createPersistent(BuildupMobTweaks.id("ecology_rebuild"), CompoundTag.CODEC);
    private static final AttachmentType<Runtime> RUNTIME = AttachmentRegistry.createDefaulted(BuildupMobTweaks.id("ecology_runtime"), Runtime::new);
    public static boolean known(LivingEntity e) { return e.hasAttached(DATA) && e.getAttached(DATA).getIntOr("version", -1) == 1; }
    public static boolean initialize(LivingEntity e) {
        if (!e.hasAttached(DATA)) { var d = new CompoundTag(); d.putInt("version", 1); e.setAttached(DATA, d); }
        return known(e);
    }
    public static Runtime runtime(LivingEntity e) { return e.getAttachedOrCreate(RUNTIME); }
    public static boolean ready(LivingEntity e, String key) { return known(e) && e.level().getGameTime() >= e.getAttached(DATA).getLongOr(key, 0); }
    public static void cooldown(LivingEntity e, String key, int ticks) { e.getAttached(DATA).putLong(key, e.level().getGameTime() + ticks); }
    public static final class Runtime {
        public boolean installed;
        public int ink, gazes, escapes, shares, heals, dodges, defenses, horns, queries;
        public long panicUntil, defenseUntil, hornUntil;
        public Vec3 threat;
        public LivingEntity attacker, hornTarget;
        public String search = "idle";
        public int searchQueries;
        public Vec3 pointer;
        public long pointerUntil;
    }
    private EcologyState() {}
}
