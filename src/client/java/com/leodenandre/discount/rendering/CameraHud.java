package com.leodenandre.discount.rendering;

import com.leodenandre.discount.items.ModItems;
import com.leodenandre.discount.network.packet.c2s.CameraImageC2SPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.impl.client.rendering.hud.HudLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Set;

import static net.minecraft.client.gl.PostEffectProcessor.MAIN;

public class CameraHud {


    private static PostEffectProcessor effect;
    private static boolean enabled = true;
    private static KeyBinding toggleKey;
private static KeyBinding takePictureKey;

    public static void init(){
        HudRenderCallback.EVENT.register(CameraHud::render);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {

                    @Override public Identifier getFabricId() {
                        return Identifier.of("discount", "pixelate_reload");
                    }
                    @Override public void reload(net.minecraft.resource.ResourceManager manager) {
                        unloadEffect();
                        loadEffect();
                    }
                }
        );

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.discount.pixel_toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, KeyBinding.MISC_CATEGORY));
        //takePictureKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        //        "key.discount.take_picture", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_SPACE, KeyBinding.MISC_CATEGORY));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!CameraHud.isEnabled()) return;
            var o = client.options;

            if (o.jumpKey.wasPressed()) {
                takeImage();

                while (o.jumpKey.wasPressed()) { /* consume */ }
            }

            o.forwardKey.setPressed(false);
            o.backKey.setPressed(false);
            o.leftKey.setPressed(false);
            o.rightKey.setPressed(false);
            o.jumpKey.setPressed(false);
            o.sprintKey.setPressed(false);
            o.sneakKey.setPressed(false);
           if(client.player != null){

               if(!client.player.getInventory().getSelectedStack().isOf(ModItems.CAMERA)){
                   close();
               }

               if (client.player.input != null) {
                   client.player.setSprinting(false);
               }
           }

        });



        WorldRenderEvents.LAST.register(CameraHud::renderEffect);

    }

    public static void toggle(){
        if (enabled) {
            close();
        } else {
            open();
        }
    }
    public static void open(){
        enabled = true;
        var mc = MinecraftClient.getInstance();
        mc.options.hudHidden = true;
    }
    public static void close(){
        enabled = false;
        var mc = MinecraftClient.getInstance();
        mc.options.hudHidden = false;
    }

    public static void takeImage(){
        var mc = MinecraftClient.getInstance();

       var oldHud = mc.options.hudHidden;
        mc.options.hudHidden = true;


        mc.gameRenderer.render(RenderTickCounter.ONE, true);



        ScreenshotRecorder.takeScreenshot(mc.getFramebuffer(), (img)->{
            NativeImage scaled = new NativeImage(128, 128,false);
            img.resizeSubRectTo(0,0,img.getWidth(),img.getHeight(), scaled);
            try (img) {
                var payload = new CameraImageC2SPayload(toBufferedImage(scaled));
                ClientPlayNetworking.send(payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mc.options.hudHidden = oldHud;
    }

    public static boolean isEnabled() {
        return enabled;
    }


    record Rect(int left, int top, int right, int bottom){


        public int width() {
            return right - left;
        }
        public int height() {
            return bottom - top;
        }
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();


        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        var screenHeight = (int)(sh*0.9);
        var screenWidth = (int)(screenHeight/1.777);

        var bounds = new Rect(sw/2-screenWidth/2, sh/2-screenHeight/2, sw/2+screenWidth/2, sh/2+screenHeight/2);

        int marginColor=0xFF000000;
        ctx.fill(0, 0, bounds.left, sh, marginColor);
        ctx.fill(bounds.right, 0, sw, sh, marginColor);
        ctx.fill(bounds.left, 0, bounds.right, bounds.top, marginColor);
        ctx.fill(bounds.left, bounds.bottom, bounds.right, sh, marginColor);

        String rec = "REC";
        int dotSize = Math.max(4, sw / 160);
        int textW = mc.textRenderer.getWidth(rec);
        int recPlusDotWidth = textW + dotSize * 2;

        int xRight = (bounds.right+bounds.left)/2-recPlusDotWidth/2;
        int yTop = bounds.top + bounds.width()/10;

        ctx.fill(xRight, yTop, xRight + dotSize, yTop + dotSize, 0xFFFF0000);
        ctx.drawText(mc.textRenderer, rec, xRight + dotSize * 2, yTop, 0xFFFF3A3A, true);



        //Corner lines
        {

            int lineThickness = Math.max(1, sw / 800);
            int lineMargin = bounds.width()/10;
            int lineLength = bounds.width()/7;

            //TOP LEFT
            ctx.fill(
                    bounds.left + lineMargin, bounds.top + lineMargin,
                    bounds.left + lineMargin + lineThickness, bounds.top + lineMargin + lineLength,
                    0xFFFFFFFF
            );
            ctx.fill(
                    bounds.left + lineMargin, bounds.top + lineMargin,
                    bounds.left + lineMargin + lineLength, bounds.top + lineMargin + lineThickness,
                    0xFFFFFFFF
            );

            //TOP RIGHT
            ctx.fill(
                    bounds.right - lineMargin, bounds.top + lineMargin,
                    bounds.right - lineMargin - lineThickness, bounds.top + lineMargin + lineLength,
                    0xFFFFFFFF
            );
            ctx.fill(
                    bounds.right - lineMargin, bounds.top + lineMargin,
                    bounds.right - lineMargin - lineLength, bounds.top + lineMargin + lineThickness,
                    0xFFFFFFFF
            );

            //BOTTOM LEFT
            ctx.fill(
                    bounds.left + lineMargin, bounds.bottom - lineMargin,
                    bounds.left + lineMargin + lineThickness, bounds.bottom - lineMargin - lineLength,
                    0xFFFFFFFF
            );
            ctx.fill(
                    bounds.left + lineMargin, bounds.bottom - lineMargin,
                    bounds.left + lineMargin + lineLength, bounds.bottom - lineMargin - lineThickness,
                    0xFFFFFFFF
            );

            //BOTTOM RIGHT
            ctx.fill(
                    bounds.right - lineMargin, bounds.bottom - lineMargin,
                    bounds.right - lineMargin - lineThickness, bounds.bottom - lineMargin - lineLength,
                    0xFFFFFFFF
            );
            ctx.fill(
                    bounds.right - lineMargin, bounds.bottom - lineMargin,
                    bounds.right - lineMargin - lineLength, bounds.bottom - lineMargin - lineThickness,
                    0xFFFFFFFF
            );
        }


        // 3) (Optional) center crosshair for the camera
        int cx = sw / 2, cy = sh / 2;
        int cross = Math.max(6, sw / 200);
        int thick = Math.max(1, sw / 800);
        int col = 0x80FFFFFF; // semi-white
        // horizontal
        ctx.fill(cx - cross, cy - thick, cx + cross, cy + thick, col);
        // vertical
        ctx.fill(cx - thick, cy - cross, cx + thick, cy + cross, col);
    }


    private static void renderEffect(WorldRenderContext ctx){
        if (!enabled || effect == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        var buffer = mc.getFramebuffer();


        try {
            FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
            PostEffectProcessor.FramebufferSet framebufferSet = PostEffectProcessor.FramebufferSet.singleton(MAIN, frameGraphBuilder.createObjectNode("main", buffer));
            effect.render(frameGraphBuilder, buffer.textureWidth, buffer.textureHeight, framebufferSet);
            frameGraphBuilder.run(ObjectAllocator.TRIVIAL);
        } catch (IllegalStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Buffer already closed")) {
                return;
            }
            throw ex;
        }


    }

    private static void loadEffect() {
        try {
            var mc = MinecraftClient.getInstance();
            var id = Identifier.of("discount", "pixelate");
            var sl = mc.getShaderLoader();
            Set<Identifier> set = Set.of(Identifier.of("minecraft", "main"));
            effect = sl.loadPostEffect(id, set);
        } catch (Exception e) {
            e.printStackTrace();
            effect = null;
        }
    }

    private static void unloadEffect() {
        if (effect != null) { effect.close(); effect = null; }
    }




    private static BufferedImage toBufferedImage(NativeImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getColorArgb(x, y);
                out.setRGB(x, y, argb);
            }
        }
        return out;
    }
}
