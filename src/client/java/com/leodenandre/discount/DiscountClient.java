package com.leodenandre.discount;

import com.leodenandre.discount.client.ClientCameraBridge;
import com.leodenandre.discount.client.ClientCameraBridgeHolder;
import com.leodenandre.discount.network.ModPackets;
import com.leodenandre.discount.rendering.CameraHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;

import java.io.ByteArrayOutputStream;

public class DiscountClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		CameraHud.init();

		initCameraBridge();

	}

	void initCameraBridge()
	{
		ClientCameraBridgeHolder.INSTANCE = new ClientCameraBridge() {
			@Override
			public void openCameraUi() {
				CameraHud.open();
			}
			@Override
			public void closeCameraUi() {
				CameraHud.close();
			}
			@Override
			public void toggleCameraUi() {
				CameraHud.toggle();
			}

			@Override
			public void takePicture() {
				CameraHud.takeImage();
			}
		};
	}
}
