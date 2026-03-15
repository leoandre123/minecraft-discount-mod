package com.leodenandre.discount.items;

import com.leodenandre.discount.client.ClientCameraBridge;
import com.leodenandre.discount.client.ClientCameraBridgeHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CameraItem extends Item {
    public CameraItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            ClientCameraBridgeHolder.INSTANCE.toggleCameraUi();
        }
        return super.use(world, user, hand);
    }


    public static ItemStack createMap(ServerWorld world, BufferedImage image) {
        var map = FilledMapItem.createMap(world, 0, 0, (byte) 0, false, false);
        MapIdComponent id = map.get(DataComponentTypes.MAP_ID);

        //var mapState = FilledMapItem.getMapState(map, world);

        var mapState = MapState.of((byte) 0, true, world.getRegistryKey());

        //if(mapState == null)return map;


        BufferedImage scaled = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        {
            var g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, 128, 128, null);
            g.dispose();
        }

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int argb = scaled.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a < 8) { // nearly transparent -> clear
                    mapState.setColor(x, y, (byte) 0); // 0 == MapColor.CLEAR at brightness 0
                    continue;
                }
                byte colorByte = nearestMapColorByte(argb);
                mapState.setColor(x, y, colorByte);
            }
        }
        world.putMapState(id, mapState);
        mapState.markDirty();
        return map;
    }

    private static byte nearestMapColorByte(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        int bestDist = Integer.MAX_VALUE;
        byte bestByte = 0;

        // MapColor ids are 0..63; brightness has 4 levels (0..3).
        for (int colorId = 0; colorId < 64; colorId++) {
            var mapColor = net.minecraft.block.MapColor.get(colorId);                 // MapColor.get(...)
            for (var brightness : net.minecraft.block.MapColor.Brightness.values()) { // 4 shades
                int rgb = mapColor.getRenderColor(brightness); // 0xRRGGBB
                int rr = (rgb >> 16) & 0xFF, gg = (rgb >> 8) & 0xFF, bb = rgb & 0xFF;
                int dr = r - rr, dg = g - gg, db = b - bb;
                int dist = dr * dr + dg * dg + db * db;
                if (dist < bestDist) {
                    bestDist = dist;
                    bestByte = mapColor.getRenderColorByte(brightness); // packed palette byte
                }
            }
        }
        return bestByte;
    }

}
