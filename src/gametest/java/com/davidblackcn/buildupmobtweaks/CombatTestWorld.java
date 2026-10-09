package com.davidblackcn.buildupmobtweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;

final class CombatTestWorld {
    static final String ARENA = "buildupmobtweaks:arena";
    static void floor(GameTestHelper h, Block block) {
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            h.setBlock(new BlockPos(x, 1, z), block);
            h.setBlock(new BlockPos(x, 5, z), Blocks.STONE);
        }
    }
    static <T extends Mob> T mob(GameTestHelper h, EntityType<T> type, int x, int z) {
        T mob = type.create(h.getLevel(), EntitySpawnReason.COMMAND);
        mob.snapTo(h.absolutePos(new BlockPos(x, 2, z)), 0, 0); mob.setNoAi(true); mob.setPersistenceRequired();
        return mob;
    }
    static <T extends Entity> T reload(GameTestHelper h, T entity) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        entity.saveWithoutId(output); var type = entity.getType(); entity.discard();
        var input = TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), output.buildResult());
        @SuppressWarnings("unchecked") T loaded = (T) EntityType.create(type, input, h.getLevel(), EntitySpawnReason.LOAD).orElseThrow();
        h.getLevel().addFreshEntity(loaded); return loaded;
    }
    private CombatTestWorld() {}
    static <T extends Entity> T reloadPassengers(GameTestHelper h, T entity) {
        var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());entity.saveWithoutId(output);
        for(var child:java.util.List.copyOf(entity.getPassengers()))child.discard();entity.discard();
        @SuppressWarnings("unchecked") T loaded=(T)EntityType.loadEntityRecursive(entity.getType(),output.buildResult(),h.getLevel(),EntitySpawnReason.LOAD,e->e);
        h.getLevel().addFreshEntityWithPassengers(loaded);return loaded;
    }
}
