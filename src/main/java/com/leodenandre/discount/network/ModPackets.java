package com.leodenandre.discount.network;

import com.leodenandre.discount.items.CameraItem;
import com.leodenandre.discount.network.packet.c2s.CameraImageC2SPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public final class ModPackets {
    public static void registerPackets(){
        PayloadTypeRegistry.playC2S().register(CameraImageC2SPayload.ID, CameraImageC2SPayload.CODEC);
    };

    public static void registerServerReceivers(){
        ServerPlayNetworking.registerGlobalReceiver(CameraImageC2SPayload.ID, (payload, context) -> {
            var player = context.player();
            var map = CameraItem.createMap(context.player().getWorld(), payload.img());
            player.getInventory().insertStack(map);
        });
    }

    private ModPackets() {}
}
