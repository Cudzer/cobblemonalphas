package dev.cudzer.cobblemonalphas.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
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

public class AlphaEyesRender {
    public static final AlphaEyesRender INSTANCE = new AlphaEyesRender();
    static final float TRAIL_WIDTH = 0.1f;
    static final float MIN_SPAN = 0.1f;
    static final int MAX_POINTS = 10;

    static class Trail {
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

        // Don't draw anything unless there is at least some movement
        Vec3 first = history.points.getFirst();
        Vec3 last = history.points.getLast();
        if (first.distanceTo(last) < MIN_SPAN)
            return;

        Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = cam.getPosition();
        Vec3 camForward = RenderUtils.toVec3(cam.getLookVector());

        Matrix4f pose = poseStack.last().pose();
        Matrix4f inversePose = new Matrix4f(pose).invert();

        int count = history.points.size();
        int segments = count - 1;

        // Convert all points to entity-local space up front
        List<Vec3> locals = new ArrayList<>(count);
        for (Vec3 worldPt : history.points) {
            Vec3 camRel = worldPt.subtract(camPos);
            locals.add(RenderUtils.toVec3(
                    inversePose.transformPosition(RenderUtils.toVector3f(camRel))));
        }

        // Check if the length of the trail would be big enough
        double maxDistSqr = 0.0;
        Vec3 base = locals.get(0);

        for (Vec3 p : history.points) {
            maxDistSqr = Math.max(maxDistSqr, p.distanceToSqr(base));
        }

        if (maxDistSqr < MIN_SPAN * MIN_SPAN)
            return;

        // Pre-compute a side vector and width at each point
        Vec3[] sides = new Vec3[count];
        float[] widths = new float[count];
        int[] colors = new int[count];

        for (int i = 0; i < count; i++) {
            Vec3 dir;
            if (i == 0) {
                // First point: use direction toward next point
                dir = locals.get(1).subtract(locals.get(0));
            } else if (i == count - 1) {
                // Last point: use direction from previous point
                dir = locals.get(i).subtract(locals.get(i - 1));
            } else {
                // Middle points: average the two adjacent segment directions
                Vec3 d1 = locals.get(i).subtract(locals.get(i - 1)).normalize();
                Vec3 d2 = locals.get(i + 1).subtract(locals.get(i)).normalize();
                dir = d1.add(d2);
            }

            // Skip degenerate
            if (dir.lengthSqr() < 1e-10) {
                sides[i] = new Vec3(1, 0, 0);
            } else {
                Vec3 side = dir.normalize().cross(camForward);
                if (side.lengthSqr() < 1e-10) {
                    sides[i] = new Vec3(1, 0, 0);
                } else {
                    sides[i] = side.normalize();
                }
            }

            float t = (float) i / segments;
            widths[i] = TRAIL_WIDTH * (1.0f - t);
            colors[i] = (int) ((1.0f - t) * 255) << 24 | 255 << 16;
        }

        // Emit connected quads — each pair of adjacent points shares
        // the exact same edge vertices, so no gaps can appear
        for (int i = 0; i < segments; i++) {
            Vec3 p1 = locals.get(i);
            Vec3 p2 = locals.get(i + 1);

            Vector3f p1a = RenderUtils.toVector3f(p1.add(sides[i].scale(widths[i])));
            Vector3f p1b = RenderUtils.toVector3f(p1.subtract(sides[i].scale(widths[i])));
            Vector3f p2a = RenderUtils.toVector3f(p2.add(sides[i + 1].scale(widths[i + 1])));
            Vector3f p2b = RenderUtils.toVector3f(p2.subtract(sides[i + 1].scale(widths[i + 1])));

            vc.addVertex(pose, p1a.x, p1a.y, p1a.z).setColor(colors[i]);
            vc.addVertex(pose, p1b.x, p1b.y, p1b.z).setColor(colors[i]);
            vc.addVertex(pose, p2b.x, p2b.y, p2b.z).setColor(colors[i + 1]);
            vc.addVertex(pose, p2a.x, p2a.y, p2a.z).setColor(colors[i + 1]);
        }
    }
}
