package st.rhapsody.voxelengine.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by nicklas on 4/24/14.
 */
public class TextureUtils {

    public static Vector2[] calculateUVMapping(int textureIndex, int atlasWidth, int atlasHeight) {

        int u = textureIndex % atlasWidth;
        int v = textureIndex / atlasHeight;

        float xOffset = 1f / atlasWidth;
        float yOffset = 1f / atlasHeight;

        float uOffset = (u * xOffset);
        float vOffset = (v * yOffset);

        Vector2[] UVList = new Vector2[4];

        UVList[3] = new Vector2(uOffset, vOffset); // 0,0
        UVList[0] = new Vector2(uOffset, vOffset + yOffset); // 0,1
        UVList[2] = new Vector2(uOffset + xOffset, vOffset); // 1,0
        UVList[1] = new Vector2(uOffset + xOffset, vOffset + yOffset); // 1,1

        return UVList;
    }
}
