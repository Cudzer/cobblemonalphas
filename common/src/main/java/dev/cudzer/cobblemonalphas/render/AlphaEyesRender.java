package dev.cudzer.cobblemonalphas.render;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public class AlphaEyesRender {
    private static final int TRAIL_LENGTH = 60;

    public class TrailData {
        public final Deque<Vec3> leftEyeHistory = new ArrayDeque<>();
        public final Deque<Vec3> rightEyeHistory = new ArrayDeque<>();
    }

    private static final Map<PokemonEntity, TrailData> TRAIL_CACHE = new WeakHashMap<>();

    public void render(PokemonEntity entity, PoseStack poseStack, PokemonClientDelegate clientDelegate,
            MultiBufferSource buffer) {
        // Fetch the positions of the eyes
        Map<String, MatrixWrapper> locatorStates = clientDelegate.getLocatorStates();
        MatrixWrapper leftEyeLocator = locatorStates.get("eye1");
        MatrixWrapper rightEyeLocator = locatorStates.get("eye2");

        // Most models don't have these locators implemented yet so we will skip them
        if (leftEyeLocator == null || rightEyeLocator == null)
            return;

        TrailData data = TRAIL_CACHE.computeIfAbsent(entity, k -> new TrailData());

        Vec3 currentLeft = captureWorldPos(poseStack, leftEyeLocator);
        Vec3 currentRight = captureWorldPos(poseStack, rightEyeLocator);

        updateHistory(data.leftEyeHistory, currentLeft);
        updateHistory(data.rightEyeHistory, currentRight);

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        renderTrail(buffer, data.leftEyeHistory, cameraPos);
        renderTrail(buffer, data.rightEyeHistory, cameraPos);
    }

    private Vec3 captureWorldPos(PoseStack stack, MatrixWrapper locator) {
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

    private void updateHistory(Deque<Vec3> history, Vec3 newPos) {
        // Add new position to the head (start) of the list
        history.addFirst(newPos);

        // Trim the tail if it gets too long
        while (history.size() > TRAIL_LENGTH) {
            history.removeLast();
        }
    }

    private void renderTrail(MultiBufferSource buffer, Deque<Vec3> history, Vec3 cameraPos) {
        if (history.size() < 2)
            return;

        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());

        float startWidth = 0.1f;
        int i = 0;
        int size = history.size();

        java.util.List<Vec3> points = new java.util.ArrayList<>(history);

        for (i = 0; i < points.size() - 1; i++) {
            Vec3 worldPos1 = points.get(i);
            Vec3 worldPos2 = points.get(i + 1);

            Vec3 pos1 = worldPos1.subtract(cameraPos);
            Vec3 pos2 = worldPos2.subtract(cameraPos);

            // Direction of this specific segment
            Vec3 dir = pos1.subtract(pos2).normalize();
            Vec3 toCamera = pos1.reverse().normalize();
            Vec3 side = dir.cross(toCamera).normalize();

            // Tapering
            float p1 = (float) i / size;
            float p2 = (float) (i + 1) / size;
            float w1 = startWidth * (1.0f - p1);
            float w2 = startWidth * (1.0f - p2);

            // Colors (Red with fade)
            int c1 = (int) ((1.0f - p1) * 255) << 24 | 255 << 16;
            int c2 = (int) ((1.0f - p2) * 255) << 24 | 255 << 16;

            // Draw a Quad for this segment
            // Top Left
            vc.addVertex((float) (pos1.x + side.x * w1), (float) (pos1.y + side.y * w1), (float) (pos1.z + side.z * w1))
                    .setColor(c1).setNormal(0, 1, 0);

            // Bottom Left
            vc.addVertex((float) (pos1.x - side.x * w1), (float) (pos1.y - side.y * w1), (float) (pos1.z - side.z * w1))
                    .setColor(c1).setNormal(0, 1, 0);

            // Bottom Right
            vc.addVertex((float) (pos2.x - side.x * w2), (float) (pos2.y - side.y * w2), (float) (pos2.z - side.z * w2))
                    .setColor(c2).setNormal(0, 1, 0);

            // Top Right
            vc.addVertex((float) (pos2.x + side.x * w2), (float) (pos2.y + side.y * w2), (float) (pos2.z + side.z * w2))
                    .setColor(c2).setNormal(0, 1, 0);
        }
    }
}
