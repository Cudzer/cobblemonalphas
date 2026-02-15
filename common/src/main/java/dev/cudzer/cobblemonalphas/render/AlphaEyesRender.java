package dev.cudzer.cobblemonalphas.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import dev.cudzer.cobblemonalphas.util.RenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class AlphaEyesRender {
    public static final AlphaEyesRender INSTANCE = new AlphaEyesRender();

    static class Trail {
        static final int MAX_POINTS = 45;
        long lastFrameUpdated = -1;
        final Deque<Vec3> points = new ArrayDeque<>();

        private void add(Vec3 newPos, long currentTick) {
            if (currentTick == lastFrameUpdated)
                return;
            lastFrameUpdated = currentTick;

            // Update the history and timestamp
            points.addFirst(newPos);

            // Remove any points when the trail becomes too long
            while (points.size() > MAX_POINTS) {
                points.removeLast();
            }
        }

        int size() {
            return points.size();
        }
    }

    private static final float TRAIL_WIDTH = 0.1f;
    private static final Map<PokemonEntity, List<Trail>> TRAILS = new HashMap<>();

    public void render(
            PokemonEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            PokemonClientDelegate clientDelegate,
            MultiBufferSource buffer) {

        // Fetch the positions of all eye locators
        MatrixWrapper[] eyeLocators = clientDelegate
                .getLocatorStates()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().contains("eye"))
                .map(Map.Entry::getValue)
                .toArray(MatrixWrapper[]::new);

        // If there are no locators on the model or the pokemon doesn't have eyes skip
        if (eyeLocators.length < 1)
            return;

        // Create a trail cache entry for the alpha if it doesn't have one
        // Each eye is assigned its own history under the entity
        List<Trail> trails = TRAILS.computeIfAbsent(entity, k -> {
            List<Trail> list = new ArrayList<>(eyeLocators.length);
            for (int i = 0; i < eyeLocators.length; i++) {
                list.add(new Trail());
            }

            return list;
        });

        // Fetch the postion and rotation
        Vec3 entityPos = entity.position();

        // Fetch static parameters needed for rendering
        long gameTick = Minecraft.getInstance().level.getGameTime();
        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());

        // Add the current position of each eye to their respective history
        int idx = 0;
        for (MatrixWrapper wrapper : eyeLocators) {
            // 1) Get the locator in model-relative space
            Vec3 modelPos = RenderUtils.locatorToModelSpace(wrapper);

            // 2) Translate to world space
            Vec3 worldPos = entityPos.add(modelPos);

            // 3) Store the position
            Trail trail = trails.get(idx);
            trail.add(worldPos, gameTick);

            // 4) Render
            renderTrail(trail, vc, poseStack);
            idx++;
        }
    }

    private void renderTrail(Trail history, VertexConsumer vc, PoseStack poseStack) {
        if (history.points.size() < 2)
            return;

        Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = cam.getPosition();
        Vec3 camForward = RenderUtils.toVec3(cam.getLookVector());
        
        // We need the inverse of the entity pose so we can submit
        // positions in entity-local space and let the pose stack put them
        // back into view space correctly.
        Matrix4f pose = poseStack.last().pose();
        Matrix4f inversePose = new Matrix4f(pose).invert();

        // Setup variables for the loop
        int segments = history.points.size() - 1;
        int seg = 0;

        Iterator<Vec3> it = history.points.iterator();
        Vec3 prev = it.next();

        // Render out the trail
        while (it.hasNext()) {
            // Fetch the position
            Vec3 curr = it.next();

            // World → camera-relative
            Vec3 p1 = prev.subtract(camPos);
            Vec3 p2 = curr.subtract(camPos);

            // Camera-relative → entity-local (undo the pose stack transform)
            // so that addVertex(pose, ...) puts them back correctly
            Vec3 p1local = RenderUtils.toVec3(inversePose.transformPosition(RenderUtils.toVector3f(p1)));
            Vec3 p2local = RenderUtils.toVec3(inversePose.transformPosition(RenderUtils.toVector3f(p2)));

            Vec3 dir = p2local.subtract(p1local).normalize();
            Vec3 side = dir.cross(camForward).normalize();

            // Tapering factor
            float t1 = (float) seg / segments;
            float t2 = (float) (seg + 1) / segments;
            float w1 = TRAIL_WIDTH * (1.0f - t1);
            float w2 = TRAIL_WIDTH * (1.0f - t2);

            // Colors (Red with fade)
            int c1 = (int) ((1.0f - t1) * 255) << 24 | 255 << 16;
            int c2 = (int) ((1.0f - t2) * 255) << 24 | 255 << 16;

            Vector3f p1a = RenderUtils.toVector3f(p1local.add(side.scale(w1)));
            Vector3f p1b = RenderUtils.toVector3f(p1local.subtract(side.scale(w1)));

            Vector3f p2a = RenderUtils.toVector3f(p2local.add(side.scale(w2)));
            Vector3f p2b = RenderUtils.toVector3f(p2local.subtract(side.scale(w2)));

            // Submit in entity-local space WITH the pose matrix
            // so the GPU transform pipeline is correct for all passes
            vc.addVertex(pose, p1a.x, p1a.y, p1a.z).setColor(c1);
            vc.addVertex(pose, p1b.x, p1b.y, p1b.z).setColor(c1);
            vc.addVertex(pose, p2b.x, p2b.y, p2b.z).setColor(c2);
            vc.addVertex(pose, p2a.x, p2a.y, p2a.z).setColor(c2);

            // Update loop params
            prev = curr;
            seg++;
        }
    }
}
