package dev.cudzer.cobblemonalphas.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.mojang.math.Axis;

import net.minecraft.world.phys.Vec3;

public class RenderUtils {
    public static Vec3 locatorToModelSpace(MatrixWrapper locator) {
        // Only apply the locator (bone) matrix — not the pose stack
        Matrix4f mat = new Matrix4f(locator.getMatrix());
        
        // Apply the Cobblemon coordinate flip
        mat.rotate(Axis.XP.rotationDegrees(180));
        mat.rotate(Axis.YP.rotationDegrees(180));

        Vector3f pos = new Vector3f(0, 0, 0);
        mat.transformPosition(pos);
        return new Vec3(pos.x, pos.y, pos.z);
    }

    public static Vec3 toVec3(Vector3f v) {
        return new Vec3((double) v.x, (double) v.y, (double) v.z);
    }

    public static Vector3f toVector3f(Vec3 v) {
        return new Vector3f((float) v.x, (float) v.y, (float) v.z);
    }
}
