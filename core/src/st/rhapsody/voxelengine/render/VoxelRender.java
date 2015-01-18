package st.rhapsody.voxelengine.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import st.rhapsody.voxelengine.terrain.Chunk;
import st.rhapsody.voxelengine.terrain.Terrain;

import java.util.Collection;

/**
 * Created by nicklas on 4/24/14.
 */
public class VoxelRender implements RenderableProvider {
    private static int numberOfVertices = 0;
    private static int numberOfIndicies = 0;
    private static int numberOfVisibleChunks = 0;
    private static int blockCounter = 0;
    private static Terrain terrain;
    private final Material material;
    private final PerspectiveCamera camera;
    private static int numberOfVisibleBlocks;

    public VoxelRender(Material material, Terrain terrain, PerspectiveCamera camera) {
        this.material = material;
        this.terrain = terrain;
        this.camera = camera;
    }

    public static int getNumberOfVertices() {
        return numberOfVertices;
    }

    public static int getNumberOfIndicies() {
        return numberOfIndicies;
    }

    public static int getNumberOfChunks() {
        return terrain.getChunks().size();
    }

    public static int getNumberOfVisibleChunks() {
        return numberOfVisibleChunks;
    }

    public static int getBlockCounter() {
        return blockCounter;
    }

    public static int getNumberOfVisibleBlocks() {
        return numberOfVisibleBlocks;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

        numberOfVertices = 0;
        numberOfIndicies = 0;
        numberOfVisibleChunks = 0;
        blockCounter = 0;
        numberOfVisibleBlocks = 0;

        for (Chunk chunk : terrain.getChunks()) {
            blockCounter += chunk.getBlockCounter();
            if (chunk.isRecalculating()) continue;
            boolean b = camera.frustum.sphereInFrustum(chunk.getPosition(), Terrain.WIDTH * 1.5f);
            if (!b) {
                //System.out.println("Not in frustum");
                chunk.setActive(false);
                continue;
            } else {
                chunk.setActive(true);
            }
            numberOfVisibleBlocks += chunk.getBlockCounter();
            numberOfVisibleChunks++;
            Collection<VoxelMesh> meshes = chunk.getMeshes();
            for (BoxMesh boxMesh : meshes) {
                if (boxMesh != null && boxMesh.getMesh() != null) {

                    Mesh mesh = boxMesh.getMesh();

                    Renderable renderable = pool.obtain();
                    renderable.material = material;
                    renderable.meshPartOffset = 0;
                    renderable.meshPartSize = mesh.getNumIndices();
                    renderable.primitiveType = GL20.GL_TRIANGLES;
                    renderable.mesh = mesh;
                    renderables.add(renderable);

                    renderable.worldTransform.set(boxMesh.getTransform());

                    numberOfVertices += mesh.getNumVertices();
                    numberOfIndicies += mesh.getNumIndices();
                }
            }
        }
    }
}
