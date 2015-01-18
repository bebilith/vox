package st.rhapsody.voxelengine.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import st.rhapsody.voxelengine.terrain.Chunk;
import st.rhapsody.voxelengine.terrain.block.Block;

import java.util.HashMap;

/**
 * Created by nicklas on 4/24/14.
 */
public class VoxelMesh extends BoxMesh {

    private final static HashMap<Float, float[]> finalLightValue = new HashMap<Float, float[]>();

    public void addBlock(Vector3 worldPosition, int x, int y, int z, Chunk chunk, Block block, float light) {
        if (block.getId() == 0) {
            return;
        }

        // In case that we are rebuilding the mesh we syncronize which will make this call to wait for the rebuild to finish
        // before modifying it.
        synchronized (rebuilding) {
            setupMesh(x, y, z);
            if (transform == null) {
                transform = new Matrix4().setTranslation(worldPosition);
            }


            float lightValue = (float) Math.pow(0.96d, light);
            float[] finalLight;
            if (finalLightValue.containsKey(lightValue)){
                finalLight = finalLightValue.get(lightValue);
            }else{
                finalLight = new float[]{lightValue, lightValue, lightValue, 1};
                finalLightValue.put(lightValue,finalLight);
            }

            if (chunk.isBlockTransparent(x, y, z + 1)) {
                lightFloat = finalLight;
                addFront(block.getTextureId());
            }
            if (chunk.isBlockTransparent(x, y, z - 1)) {
                lightFloat = finalLight;
                addBack(block.getTextureId());
            }
            if (chunk.isBlockTransparent(x + 1, y, z)) {
                lightFloat = finalLight;
                addRight(block.getTextureId());
            }
            if (chunk.isBlockTransparent(x - 1, y, z)) {
                lightFloat = finalLight;
                addLeft(block.getTextureId());
            }
            if (chunk.isBlockTransparent(x, y + 1, z)) {
                lightFloat = finalLight;
                addTop(block.getTextureId());
            }
            if (chunk.isBlockTransparent(x, y - 1, z)) {
                lightFloat = finalLight;
                addBottom(block.getTextureId());
            }
            for (int j = 0; j < points.length; j++) {
                points[j] = null;
            }
            lightFloat = null;
        }

    }


}
