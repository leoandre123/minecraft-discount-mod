package com.leodenandre.discount.network.packet.c2s;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public record CameraImageC2SPayload(BufferedImage img) implements CustomPayload {
    public static final Identifier CAMERA_IMAGE_PAYLOAD_ID = Identifier.of("discount","c2s_camera_snapshot");
    public static final CustomPayload.Id<CameraImageC2SPayload> ID = new CustomPayload.Id<>(CAMERA_IMAGE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, CameraImageC2SPayload> CODEC =
            PacketCodec.of(CameraImageC2SPayload::encode, CameraImageC2SPayload::decode);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }


    static void encode(CameraImageC2SPayload img, RegistryByteBuf buf){
        byte[] bytes;
        try (var out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(img.img, "png", out)) {
                throw new IOException("No PNG writer available");
            }
            bytes = out.toByteArray();
        } catch (IOException e) {
            bytes = new byte[0];
        }
        buf.writeVarInt(bytes.length);
        buf.writeBytes(bytes);
    }

    static CameraImageC2SPayload decode(RegistryByteBuf buf){

        try {
            var len = buf.readVarInt();

            byte[] bytes = new byte[len];
            buf.readBytes(bytes);

            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                BufferedImage img = ImageIO.read(in);
                return new CameraImageC2SPayload(img);
            }
        } catch (Exception e) {
            return new CameraImageC2SPayload(null);
        }
    }

}
