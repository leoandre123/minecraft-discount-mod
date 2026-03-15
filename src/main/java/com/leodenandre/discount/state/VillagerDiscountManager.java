package com.leodenandre.discount.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class VillagerDiscountManager extends PersistentState {

    private HashMap<UUID, VillagerData> villagers = new HashMap<>();

    //public static final Codec<VillagerDiscountManager> CODEC = RecordCodecBuilder.create((instance) -> {
        //return instance.group(
                //Codec.unboundedMap(Codec.STRING, VillagerData.CODEC).fieldOf("villager_map").forGetter((manager)->{return manager.villagers;})
        //).apply(instance, VillagerDiscountManager::new);
    //});

    public VillagerDiscountManager() {
        this.markDirty();
    }


    private static final double MIN_VERTICAL_DOT = 0.8;  // ~36.8° cone

    public static boolean isLeashDangling(VillagerEntity entity) {
        if (!(entity instanceof net.minecraft.entity.Leashable leashable)) return false;
        if (!leashable.isLeashed()) return false;

        Entity holder = leashable.getLeashHolder();
        if (holder == null || holder.getWorld() != entity.getWorld()) return false;

        // 1) Rope taut? (past elastic distance, not snapped)
        double d = leashable.getDistanceToCenter(holder);
        double slack = leashable.getElasticLeashDistance() - entity.getWidth() - holder.getWidth(); // mirrors tickLeash()
        boolean taut = d > slack && d < leashable.getLeashSnappingDistance();

        if (!taut) return false;

        // 2) Holder above mob, mob not on ground/in fluid
        if (!(holder.getY() > entity.getY() + 0.5)) return false;
        if (entity.isOnGround() || entity.isInFluid()) return false;

        // 3) Rope direction mostly vertical upward from mob to holder
        Vec3d mobCenter    = entity.getBoundingBox().getCenter();
        Vec3d holderCenter = holder.getBoundingBox().getCenter();
        Vec3d dir = holderCenter.subtract(mobCenter).normalize();        // up from mob toward holder
        double verticalDot = dir.dotProduct(new Vec3d(0, 1, 0));
        if (verticalDot < MIN_VERTICAL_DOT) return false;

        // Optional: ignore fast horizontal dragging
        Vec3d v = entity.getVelocity();
        if (Math.abs(v.x) + Math.abs(v.z) > 0.2) {
            // being yanked sideways; treat as not dangling
            return false;
        }

        return true;
    }


    private static record VillagerData(float moral) {
        public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                    Codec.FLOAT.fieldOf("moral").forGetter(VillagerData::moral)
            ).apply(instance, VillagerData::new);
        });

        public float moral() {
            return moral;
        }
    }
}
