package com.example.modernkit.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

/**
 * Rainbow armor with real 3D pieces attached to the normal biped parts.
 * "outer" (scale 1.0) is used for helmet, chestplate and boots; "inner" (scale 0.5) for leggings.
 * UV offsets must match scripts/make_rainbow_3d_textures.py (texture is 64x64).
 */
@Environment(EnvType.CLIENT)
public class RainbowArmorModel extends BipedEntityModel<LivingEntity> {

    public RainbowArmorModel(float scale, boolean inner) {
        super(scale, 0.0F, 64, 64);

        if (inner) {
            // leggings: belt buckle, hip guards, knee plates, thigh plates
            box(body, 24, 32, -3.0F, 7.0F, -3.5F, 6, 3, 1);
            box(body, 40, 32, -5.5F, 7.0F, -2.0F, 1, 4, 4);
            box(body, 40, 32, 4.5F, 7.0F, -2.0F, 1, 4, 4);
            box(rightLeg, 0, 32, -2.0F, 4.5F, -3.5F, 4, 3, 1);
            box(rightLeg, 12, 32, -3.5F, 1.0F, -2.0F, 1, 5, 4);
            box(leftLeg, 0, 32, -2.0F, 4.5F, -3.5F, 4, 3, 1);
            box(leftLeg, 12, 32, 2.5F, 1.0F, -2.0F, 1, 5, 4);
        } else {
            // helmet: tall crest, cheek guards, wing fins, brow plate
            box(head, 0, 32, -0.5F, -14.0F, -4.0F, 1, 5, 8);
            box(head, 18, 32, -6.0F, -4.0F, -2.5F, 1, 4, 5);
            box(head, 18, 32, 5.0F, -4.0F, -2.5F, 1, 4, 5);
            box(head, 30, 32, -6.0F, -7.0F, 0.5F, 1, 3, 4);
            box(head, 30, 32, 5.0F, -7.0F, 0.5F, 1, 3, 4);
            box(head, 18, 41, -4.0F, -8.5F, -5.5F, 8, 2, 1);

            // chestplate: chest plate with gem, abdomen plate, back plate
            box(body, 40, 32, -4.0F, 1.0F, -4.0F, 8, 5, 1);
            box(body, 56, 16, -1.0F, 2.0F, -5.0F, 2, 2, 1);
            box(body, 38, 50, -3.0F, 7.0F, -3.75F, 6, 3, 1);
            box(body, 0, 45, -3.0F, 1.0F, 3.0F, 6, 8, 1);

            // arms: pauldron cap, side plate, spike, forearm bracer
            box(rightArm, 14, 45, -4.0F, -5.0F, -3.0F, 6, 2, 6);
            box(rightArm, 38, 40, -5.0F, -3.0F, -3.0F, 1, 4, 6);
            box(rightArm, 56, 38, -2.0F, -8.0F, -1.0F, 2, 3, 2);
            box(rightArm, 0, 54, -4.5F, 7.0F, -3.5F, 7, 3, 7);
            box(leftArm, 14, 45, -2.0F, -5.0F, -3.0F, 6, 2, 6);
            box(leftArm, 38, 40, 4.0F, -3.0F, -3.0F, 1, 4, 6);
            box(leftArm, 56, 38, 0.0F, -8.0F, -1.0F, 2, 3, 2);
            box(leftArm, 0, 54, -2.5F, 7.0F, -3.5F, 7, 3, 7);

            // boots: cuff ring, toe cap, ankle fin
            box(rightLeg, 28, 54, -3.5F, 7.5F, -3.5F, 7, 2, 7);
            box(rightLeg, 52, 50, -2.0F, 10.5F, -4.5F, 4, 2, 2);
            box(rightLeg, 56, 43, -4.0F, 8.5F, -1.5F, 1, 3, 3);
            box(leftLeg, 28, 54, -3.5F, 7.5F, -3.5F, 7, 2, 7);
            box(leftLeg, 52, 50, -2.0F, 10.5F, -4.5F, 4, 2, 2);
            box(leftLeg, 56, 43, 3.0F, 8.5F, -1.5F, 1, 3, 3);
        }
    }

    private void box(ModelPart parent, int u, int v, float x, float y, float z, int w, int h, int d) {
        ModelPart piece = new ModelPart(this, u, v);
        piece.addCuboid(x, y, z, (float) w, (float) h, (float) d);
        parent.addChild(piece);
    }
}
