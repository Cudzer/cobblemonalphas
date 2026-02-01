package dev.cudzer.cobblemonalphas.render;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.cudzer.cobblemonalphas.util.RenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public class AlphaRoarRender {

    private final int ROAR_INTENSITY = 30;

    // Line properties
    // Opaque black
    private final int LINE_COLOR = 0xFF000000;;
    
    private final float LINE_SPEED = 0.5f;
    private final float LINE_SIZE = 1f;
    private final float LINE_MAX_AGE = 8f;

    private final double CONE_ANGLE = Math.toRadians(45);

    private static class RoarStreak {
        Vec3 direction;
        float age;

        RoarStreak(Vec3 direction) {
            this.direction = direction;
            this.age = 0f;
        }
    }

    private final Map<Integer, List<RoarStreak>> activeStreaks = new java.util.HashMap<>();

    public void render(
            PokemonEntity entity,
            PoseStack poseStack,
            PokemonClientDelegate clientDelegate,
            MultiBufferSource buffer) {

        // Fetch the positions of the head
        Map<String, MatrixWrapper> locatorStates = clientDelegate.getLocatorStates();
        MatrixWrapper mouthLocator = locatorStates.get("mouth");

        // Return early if we can't find a head
        if (mouthLocator == null)
            return;

        // Initialize a map of entitiy + roar streaks
        int entityId = entity.getId();
        List<RoarStreak> streaks = activeStreaks.computeIfAbsent(entityId, id -> new java.util.ArrayList<>());

        // Get camera and mouth positions
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 mouthPos = RenderUtils.captureWorldPos(poseStack, mouthLocator);

        // Set up the vertex consumer
        VertexConsumer vc = buffer.getBuffer(RenderType.lines());

        // Add some new lines every frame
        for (int i = 0; i < ROAR_INTENSITY; i++) {
            streaks.add(new RoarStreak(generateDirection(mouthPos, camera.getPosition())));
        }

        // If there are any lines left move them
        Iterator<RoarStreak> iterator = streaks.iterator();
        while (iterator.hasNext()) {
            RoarStreak streak = iterator.next();

            float deltaSeconds = Minecraft.getInstance().getFrameTimeNs() * 1.0e-9f;
            streak.age += deltaSeconds;

            float distance = streak.age * LINE_SPEED;

            if (distance > LINE_MAX_AGE) {
                iterator.remove();
                continue;
            }

            Vec3 centerPos = mouthPos.add(streak.direction.scale(distance));

            Vec3 half = streak.direction.scale(LINE_SIZE * 0.5f);

            Vec3 startPos = centerPos.subtract(half);
            Vec3 endPos = centerPos.add(half);

            drawLine(vc, camera, startPos, endPos);
        }

    }

    private void drawLine(VertexConsumer vc, Camera camera, Vec3 startPos, Vec3 endPos) {
        Vec3 cameraPos = camera.getPosition();

        Vec3 start = startPos.subtract(cameraPos);
        Vec3 end = endPos.subtract(cameraPos);

        Vec3 lineDir = end.subtract(start).normalize();

        Vector3f look = camera.getLookVector();
        Vec3 viewDir = new Vec3(look.x(), look.y(), look.z());

        Vec3 side = viewDir.cross(lineDir).normalize();

        vc.addVertex(
                (float) (start.x + side.x),
                (float) (start.y + side.y),
                (float) (start.z + side.z))
                .setColor(LINE_COLOR)
                .setNormal(0, 1, 0);

        vc.addVertex(
                (float) (end.x + side.x),
                (float) (end.y + side.y),
                (float) (end.z + side.z))
                .setColor(LINE_COLOR)
                .setNormal(0, 1, 0);
    }

    private Vec3 generateDirection(Vec3 mouthPos, Vec3 cameraPos) {
        Vec3 axis = cameraPos.subtract(mouthPos).normalize();

        Vec3 up = Math.abs(axis.y) < 0.999
                ? new Vec3(0, 1, 0)
                : new Vec3(1, 0, 0);

        Vec3 tangent = axis.cross(up).normalize();
        Vec3 bitangent = axis.cross(tangent);

        double theta = Math.random() * Math.PI * 2;
        double radius = Math.tan(CONE_ANGLE);

        Vec3 offset = tangent.scale(Math.cos(theta) * radius)
                .add(bitangent.scale(Math.sin(theta) * radius));

        return axis.add(offset).normalize();
    }

}
