package st.rhapsody.voxelengine.terrain.block;

/**
 * Created by nicklas on 5/2/14.
 */
public class Block {

    private final byte id;
    private final byte textureId;


    protected Block(byte id, byte textureId) {
        this.id = id;
        this.textureId = textureId;
        Blocks.addBlock(this);
    }

    public byte getId() {
        return id;
    }

    public byte getTextureId() {
        return textureId;
    }

    public boolean doesBlockShine() {
        return false;
    }
}
