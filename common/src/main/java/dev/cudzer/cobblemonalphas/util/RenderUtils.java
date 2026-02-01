package dev.cudzer.cobblemonalphas.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class RenderUtils {
    public static Vec3 captureWorldPos(PoseStack stack, MatrixWrapper locator) {
        stack.pushPose();

        stack.mulPose(locator.getMatrix());
        stack.mulPose(Axis.XP.rotationDegrees(180));
        stack.mulPose(Axis.YP.rotationDegrees(180));

        // Extract the position of (0,0,0) from the matrix
        Matrix4f mat = stack.last().pose();
        Vector3f vec = new Vector3f(0, 0, 0);
        mat.transformPosition(vec);

        stack.popPose();

        // Convert Camera-Relative -> Absolute World Space
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return new Vec3(vec.x, vec.y, vec.z).add(cameraPos);
    }   
}
