package com.davidblackcn.buildupmobtweaks.mixin.rebuild;
import com.davidblackcn.buildupmobtweaks.rebuild.profession.ProfessionBehavior;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(ZombieVillager.class)
public abstract class ProfessionBirthMixin {
    @WrapMethod(method="finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;")
    private SpawnGroupData buildup$born(ServerLevelAccessor level,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData group,Operation<SpawnGroupData> original){
        var m=(ZombieVillager)(Object)this;var main=m.getMainHandItem();var off=m.getOffhandItem();
        var mainEnchantments=main.get(DataComponents.ENCHANTMENTS);
        float mainDrop=m.getDropChances().byEquipment(EquipmentSlot.MAINHAND),offDrop=m.getDropChances().byEquipment(EquipmentSlot.OFFHAND);
        var result=original.call(level,difficulty,reason,group);
        // Protect pre-existing real stacks even if native Zombie's rare default weapon setter runs.
        if(!main.isEmpty()){
            // Native Mob.enchantSpawnedWeapon mutates the same actual stack, not a copy.
            if(mainEnchantments==null)main.remove(DataComponents.ENCHANTMENTS);else main.set(DataComponents.ENCHANTMENTS,mainEnchantments);
            m.setItemSlot(EquipmentSlot.MAINHAND,main);m.setDropChance(EquipmentSlot.MAINHAND,mainDrop);
        }
        if(!off.isEmpty()){m.setItemSlot(EquipmentSlot.OFFHAND,off);m.setDropChance(EquipmentSlot.OFFHAND,offDrop);}
        if(ProfessionBehavior.instance()!=null)ProfessionBehavior.instance().born(m,reason);return result;
    }
}
