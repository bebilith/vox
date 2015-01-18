package st.rhapsody.voxelengine.terrain;

import com.badlogic.gdx.math.Vector3;
import st.rhapsody.voxelengine.terrain.block.Blocks;

/**
 * Created by nicklas on 4/25/14.
 */
public class LandscapeChunk extends Chunk {

    public LandscapeChunk(Vector3 baseWorldPosition) {
        super(baseWorldPosition);
    }

    @Override
    void calculateChunk(Vector3 baseWorldPosition) {
        Vector3 worldPosOfXYZ = new Vector3();
        for (int x = 0; x < Terrain.WIDTH; x++) {
            for (int y = 0; y < Terrain.HEIGHT; y++) {
                for (int z = 0; z < Terrain.WIDTH; z++) {
                        worldPosOfXYZ.set(x, y, z).add(baseWorldPosition);
                        setBlock(x,y,z,getByteAtWorldPosition(x,y,z,worldPosOfXYZ));
                }
            }
        }
    }

    public byte getByteAtWorldPosition(int x, int y, int z, Vector3 worldPositionOfXYZ){
        if (y == 0) {
            return Blocks.grass.getId();
        }
        float maxHeight = Terrain.HEIGHT - 10;

        double noise = getNoiseValue(worldPositionOfXYZ, landscapeRandomOffset, 0.007f);

        noise *= maxHeight;


        if (y == 1) {
            double lampNoise = getNoiseValue(worldPositionOfXYZ, landscapeRandomOffset, 0.7f);
            if (lampNoise > 0.95f && isInOpenSpace(x,y,z)){
                return Blocks.light.getId();
            }
        }

        if (noise >= y) {
            return Blocks.stone.getId();
        }

        return 0;
    }

    private boolean isInOpenSpace(int x, int y, int z) {
        if (isBlockTransparent(x, y + 1, z) &&
                isBlockTransparent(x+1, y , z) &&
                isBlockTransparent(x-1, y , z) &&
                isBlockTransparent(x, y , z+1) &&
                isBlockTransparent(x, y , z-1)){
            return true;
        }

        return false;
    }
}
