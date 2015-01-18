package st.rhapsody.voxelengine.render;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import st.rhapsody.voxelengine.utils.TextureUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nicklas on 4/24/14.
 */
public class BoxMesh {

    public static final int ATLAS_WIDTH = 32;
    static final float SIZE = 1f;
    final static Object rebuilding = new Object();


    // Normal pointers for all the six sides. They never changes.
    private static final float[] frontNormal = new float[]{0.0f, 0.0f, 1.0f};
    private static final float[] backNormal = new float[]{0.0f, 0.0f, -1.0f};
    private static final float[] rightNormal = new float[]{1.0f, 0.0f, 0.0f};
    private static final float[] leftNormal = new float[]{-1.0f, 0.0f, 0.0f};
    private static final float[] topNormal = new float[]{0.0f, 1.0f, 0.0f};
    private static final float[] bottomNormal = new float[]{0.0f, -1.0f, 0.0f};

    final Vector3[] points = new Vector3[8];
    // To keep memory (and CPU) usage down we will reuse the same Vector3 instances.
    private static final Vector3 pointVector0 = new Vector3();
    private static final Vector3 pointVector1 = new Vector3();
    private static final Vector3 pointVector2 = new Vector3();
    private static final Vector3 pointVector3 = new Vector3();
    private static final Vector3 pointVector4 = new Vector3();
    private static final Vector3 pointVector5 = new Vector3();
    private static final Vector3 pointVector6 = new Vector3();
    private static final Vector3 pointVector7 = new Vector3();

    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

    protected Matrix4 transform;
    float[] lightFloat;
    private float[] v;
    private short[] i;
    private FloatArray vertices;
    private ShortArray indicies;
    private FloatArray verticesPoints;
    private boolean needsRebuild = false;
    private boolean rebuildingInProcess = false;
    private Mesh mesh;

    public Matrix4 getTransform() {
        return transform;
    }

    public com.badlogic.gdx.graphics.Mesh getMesh() {
        // If the mesh is being rebuilt just return null.
        synchronized (rebuilding) {
            if (rebuildingInProcess) {
                return null;
            }
            if (needsRebuild) {
                rebuildingInProcess = true;
                rebuild();
            }
            return mesh;
        }
    }

    public void setNeedsRebuild() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                synchronized (rebuilding) {
                    v = vertices.toArray();
                    i = indicies.toArray();
                    needsRebuild = true;
                }
            }
        };
        executorService.submit(runnable);

    }

    void setupMesh(int x, int y, int z) {

        if (vertices == null)
            vertices = new FloatArray();

        if (indicies == null)
            indicies = new ShortArray();

        if (verticesPoints == null) {
            verticesPoints = new FloatArray();
        }

        // Creates the 8 vector points that exists on a box. Those will be used to create the vertex.
        points[0] = pointVector0.set(x, y, z + SIZE);
        points[1] = pointVector1.set(x + SIZE, y, z + SIZE);
        points[2] = pointVector2.set(x + SIZE, y + SIZE, z + SIZE);
        points[3] = pointVector3.set(x, y + SIZE, z + SIZE);
        points[4] = pointVector4.set(x + SIZE, y, z);
        points[5] = pointVector5.set(x, y, z);
        points[6] = pointVector6.set(x, y + SIZE, z);
        points[7] = pointVector7.set(x + SIZE, y + SIZE, z);

    }

    // The following methods are used for adding the 6 different sides of a block.
    // It will create the verticies for every side using the 8 possible points created above.
    // Every vertex includes: position in 3D space (x, y, z), texturecoordinates (u,v), normalmapping, lightning.
    // Finally it creates the indicies for the created verticies (each side has two triangles)

    void addFront(int texture) {
        addFront(texture, ATLAS_WIDTH, ATLAS_WIDTH);
    }

    void addFront(int texture, int atlasWidth, int atlasHeight) {
        Vector2[] texCoords = TextureUtils.calculateUVMapping(texture, atlasWidth, atlasHeight);
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[0].x, points[0].y, points[0].z, texCoords[0].x, texCoords[0].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[1].x, points[1].y, points[1].z, texCoords[1].x, texCoords[1].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[2].x, points[2].y, points[2].z, texCoords[2].x, texCoords[2].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[3].x, points[3].y, points[3].z, texCoords[3].x, texCoords[3].y, frontNormal[0], frontNormal[1], frontNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    void addBack(int texture) {
        addBack(texture, ATLAS_WIDTH, ATLAS_WIDTH);
    }

    void addBack(int texture, int atlasWidth, int atlasHeight) {
        Vector2[] texCoords = TextureUtils.calculateUVMapping(texture, atlasWidth, atlasHeight);
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[4].x, points[4].y, points[4].z, texCoords[0].x, texCoords[0].y, backNormal[0], backNormal[1], backNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[5].x, points[5].y, points[5].z, texCoords[1].x, texCoords[1].y, backNormal[0], backNormal[1], backNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[6].x, points[6].y, points[6].z, texCoords[2].x, texCoords[2].y, backNormal[0], backNormal[1], backNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[7].x, points[7].y, points[7].z, texCoords[3].x, texCoords[3].y, backNormal[0], backNormal[1], backNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    void addRight(int texture) {
        Vector2[] texCoords = TextureUtils.calculateUVMapping(texture, ATLAS_WIDTH, ATLAS_WIDTH);
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[1].x, points[1].y, points[1].z, texCoords[0].x, texCoords[0].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[4].x, points[4].y, points[4].z, texCoords[1].x, texCoords[1].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[7].x, points[7].y, points[7].z, texCoords[2].x, texCoords[2].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[2].x, points[2].y, points[2].z, texCoords[3].x, texCoords[3].y, rightNormal[0], rightNormal[1], rightNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    void addLeft(int texture) {
        Vector2[] texCoords = TextureUtils.calculateUVMapping(texture, ATLAS_WIDTH, ATLAS_WIDTH);
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[5].x, points[5].y, points[5].z, texCoords[0].x, texCoords[0].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[0].x, points[0].y, points[0].z, texCoords[1].x, texCoords[1].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[3].x, points[3].y, points[3].z, texCoords[2].x, texCoords[2].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[6].x, points[6].y, points[6].z, texCoords[3].x, texCoords[3].y, leftNormal[0], leftNormal[1], leftNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    void addTop(int texture) {
        Vector2[] texCoords = TextureUtils.calculateUVMapping(texture, ATLAS_WIDTH, ATLAS_WIDTH);
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[3].x, points[3].y, points[3].z, texCoords[0].x, texCoords[0].y, topNormal[0], topNormal[1], topNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[2].x, points[2].y, points[2].z, texCoords[1].x, texCoords[1].y, topNormal[0], topNormal[1], topNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[7].x, points[7].y, points[7].z, texCoords[2].x, texCoords[2].y, topNormal[0], topNormal[1], topNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[6].x, points[6].y, points[6].z, texCoords[3].x, texCoords[3].y, topNormal[0], topNormal[1], topNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));

    }

    void addBottom(int texture) {
        Vector2[] texCoords = TextureUtils.calculateUVMapping(texture, ATLAS_WIDTH, ATLAS_WIDTH);
        int vertexOffset = vertices.size / 12;
        vertices.addAll(
                points[5].x, points[5].y, points[5].z, texCoords[0].x, texCoords[0].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[4].x, points[4].y, points[4].z, texCoords[1].x, texCoords[1].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[1].x, points[1].y, points[1].z, texCoords[2].x, texCoords[2].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3],
                points[0].x, points[0].y, points[0].z, texCoords[3].x, texCoords[3].y, bottomNormal[0], bottomNormal[1], bottomNormal[2], lightFloat[0], lightFloat[1], lightFloat[2], lightFloat[3]);

        indicies.addAll((short) (vertexOffset), (short) (1 + vertexOffset), (short) (2 + vertexOffset), (short) (2 + vertexOffset), (short) (3 + vertexOffset), (short) (vertexOffset));
    }

    /**
     * Rebuilds this mesh by creating a LibGDX mesh out of our collection of verticies and indicies.
     */
    private void rebuild() {
        try {
            synchronized (rebuilding) {
                rebuildingInProcess = true;
                mesh = null;
                mesh = new com.badlogic.gdx.graphics.Mesh(true, 4 * (vertices.size / 12), 6 * indicies.size, VertexAttribute.Position(), VertexAttribute.TexCoords(0), VertexAttribute.Normal(), VertexAttribute.ColorUnpacked());
                mesh.setVertices(v);
                mesh.setIndices(i);

                // Clear everything so it can be garbage collected
                vertices.clear();
                indicies.clear();
                verticesPoints.clear();
                vertices.shrink();
                indicies.shrink();
                verticesPoints.shrink();
                vertices = null;
                indicies = null;
                verticesPoints = null;
                v = null;
                i = null;

                needsRebuild = false;
                rebuildingInProcess = false;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
