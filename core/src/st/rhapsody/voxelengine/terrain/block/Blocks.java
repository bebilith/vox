package st.rhapsody.voxelengine.terrain.block;

/**
 * Created by nicklas on 5/2/14.
 */
public class Blocks {



    private final static Block[] blocks = new Block[128];

    public final static Block stone = new Block((byte) 1, (byte) 32);
    public final static Block grass = new Block((byte) 2, (byte) 0);
    public final static Block light = new LightBlock((byte) 3, (byte) 5);

    public static void addBlock(Block block){
        blocks[block.getId()] = block;
    }

    public static Block getBlockById(byte blockId){
        return blocks[blockId];
    }
}