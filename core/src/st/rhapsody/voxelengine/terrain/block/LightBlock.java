package st.rhapsody.voxelengine.terrain.block;

/**
 * Created by nicklas on 5/2/14.
 */
public class LightBlock extends Block{
    protected LightBlock(byte id, byte textureId) {
        super(id, textureId);
    }

    @Override
    public boolean doesBlockShine() {
        return true;
    }
}
