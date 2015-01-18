package st.rhapsody.voxelengine.terrain;

import com.badlogic.gdx.math.Vector3;
import st.rhapsody.voxelengine.render.BoxMesh;
import st.rhapsody.voxelengine.render.VoxelMesh;
import st.rhapsody.voxelengine.terrain.block.Blocks;
import st.rhapsody.voxelengine.terrain.noise.SimplexNoise;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by nicklas on 4/24/14.
 */
public abstract class Chunk {
    public final static byte LIGHT = 1; // 1 is brightest
    private final static byte darkness = 32; // 32 is darkest
    private final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    private final static HashMap<Vector3, byte[]> lightMaps = new HashMap<Vector3, byte[]>();
    protected static Random random;
    protected static Vector3 landscapeRandomOffset;
    private final byte[] map;
    private final Vector3 position;
    private final Object syncToken = new Object();
    protected List<VoxelMesh> meshes = new ArrayList<VoxelMesh>();
    private byte[] lightMap;
    private int blockCounter = 0;
    private boolean active = true;
    private boolean lightNeedsRecalculation = true;
    private boolean isRecalculating;

    public Chunk(final Vector3 position) {
        this.position = position;
        map = new byte[(Terrain.WIDTH * Terrain.WIDTH) * Terrain.HEIGHT];
        // If there already is a lightmap created it means a neighouring chunk added some lights for us
        if (!lightMaps.containsKey(position)) {
            lightMap = new byte[(Terrain.WIDTH * Terrain.WIDTH) * Terrain.HEIGHT];
            lightMaps.put(position, lightMap);
        } else {
            lightMap = lightMaps.get(position);
        }

        // This will prepare all Voxel meshes for this chunk
        // It will use a new mesh for every 16 block in height. So if the chunk is 128 blocks high it will end up with 8 meshes.
        for (int i = 0; i < Terrain.HEIGHT / 16; i++) {
            meshes.add(new VoxelMesh());
        }

        if (random == null) {
            random = new Random();
            if (Terrain.SEED != 0) {
                random.setSeed(Terrain.SEED);
            }
            landscapeRandomOffset = new Vector3((float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000, (float) random.nextDouble() * 10000);
        }

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    isRecalculating = true;
                    calculateChunk(position);
                    recalculate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        executorService.submit(runnable);

    }

    abstract void calculateChunk(Vector3 position);

    abstract byte getByteAtWorldPosition(int x, int y, int z, Vector3 worldPositionOfXYZ);


    double getNoiseValue(Vector3 pos, Vector3 offset, double scale) {
        double noiseX = Math.abs((double) (pos.x + offset.x) * scale);
        double noiseY = Math.abs((double) (pos.y + offset.y) * scale);
        double noiseZ = Math.abs((double) (pos.z + offset.z) * scale);

        return Math.max(0, SimplexNoise.noise(noiseX, noiseY, noiseZ));
    }


    public Collection<VoxelMesh> getMeshes() {
        return meshes;
    }

    public Vector3 getPosition() {
        return position;
    }


    void setBlock(int x, int y, int z, byte blockId) {

        map[getLocationInArray(x, y, z)] = blockId;
        blockCounter++;
    }

    private int getLocationInArray(int x, int y, int z) {
        int loc = (x * Terrain.WIDTH + z) + (y * Terrain.WIDTH * Terrain.WIDTH);
        return loc;
    }

    public int getBlockCounter() {
        return blockCounter;
    }

    /**
     * Takes all the blocks stored in this chunk and adds it to the voxelMesh.
     * Removed blocks isn't taken care of yet.
     */
    protected void recalculate() {
        isRecalculating = true;

        Set<VoxelMesh> toRebuild = new HashSet<VoxelMesh>();
        Set<Chunk> chunkLightRebuild = new HashSet<Chunk>();

        if (lightNeedsRecalculation) {
            lightNeedsRecalculation = false;
            recalculateSun();
            Set<Chunk> chunks = calculateLight();
            chunkLightRebuild.addAll(chunks);
        }
        synchronized (syncToken) {

            for (int y = 0; y < Terrain.HEIGHT; y++) {

                VoxelMesh voxelMesh = meshes.get((int) Math.floor(y / 16));

                for (int x = 0; x < Terrain.WIDTH; x++) {
                    for (int z = 0; z < Terrain.WIDTH; z++) {
                        byte block = map[getLocationInArray(x, y, z)];
                        if (block == 0) continue;
                        byte light = lightMap[getLocationInArray(x, y, z)];

                        voxelMesh.addBlock(position, x, y, z, Chunk.this, Blocks.getBlockById(block), light);
                        toRebuild.add(voxelMesh);
                    }
                }
            }
        }


        for (Chunk chunk : chunkLightRebuild) {
            if (chunk != null && chunk != Chunk.this) {
                chunk.recalculate();

            }
        }
        synchronized (syncToken) {
            for (BoxMesh voxelMesh : toRebuild) {
                voxelMesh.setNeedsRebuild();
            }
        }

        isRecalculating = false;
    }

    private Set<Chunk> calculateLight() {
        Set<Chunk> chunkLightRebuild = new HashSet<Chunk>();
        for (int y = 0; y < Terrain.HEIGHT; y++) {
            for (int x = 0; x < Terrain.WIDTH; x++) {
                for (int z = 0; z < Terrain.WIDTH; z++) {
                    byte block = map[getLocationInArray(x, y, z)];
                    if (block != 0 && Blocks.getBlockById(block).doesBlockShine()) { // A block that lights
                        chunkLightRebuild.addAll(drawCircleLight(x, y, z));
                        chunkLightRebuild.add(setLight(x, y, z, LIGHT));
                    } else {
                        setLight(x, y, z, darkness);
                    }
                }
            }
        }
        return chunkLightRebuild;
    }

    private void recalculateSun() {
        for (int y = Terrain.HEIGHT - 1; y > -1; y--) {
            for (int x = 0; x < Terrain.WIDTH; x++) {
                for (int z = 0; z < Terrain.WIDTH; z++) {

                    if (y == Terrain.HEIGHT - 1) {
                        setLight(x, y, z, (byte) 18);

                    } else {

                        byte blockAbove = getByte(x, y + 1, z);


                        if (blockAbove == 0 && lightMap[getLocationInArray(x, y + 1, z)] == 18) {
                            lightMap[getLocationInArray(x, y, z)] = 18;
                        } else {
                            int above = lightMap[getLocationInArray(x, y + 1, z)];
                            int light = above + 1;
                            if (light > 24) {
                                light = 24;
                            }

                            setLight(x, y, z, (byte) light);
                        }
                    }
                }
            }
        }
    }

    public Set<Chunk> drawCircleLight(int x, int y, int z) {
        Set<Chunk> chunkLightRebuild = new HashSet<Chunk>();
        final int baseradius = 15;
        int radius = 0;
        boolean negativePass = true;
        for (int yc = -baseradius; yc < baseradius; yc++) {
            for (int xc = -radius; xc <= radius; ++xc) {
                for (int zc = -radius; zc <= radius; ++zc) {
                    if (xc * xc + zc * zc <= radius * radius) {
                        Vector3 source = new Vector3(x, y, z);
                        Vector3 dest = new Vector3(x - xc, y - yc, z - zc);

                        double v = dest.dst(source);
                        byte strength = (byte) (v * 3);
                        chunkLightRebuild.add(setLight(x + xc, y + yc, z + zc, strength));
                    }
                }
            }

            if (negativePass) {
                radius++;
            } else {
                radius--;
            }

            if (radius == baseradius) {
                negativePass = false;
            }
        }
        return chunkLightRebuild;
    }

    private Chunk setLight(int x, int y, int z, byte light) {
        if (outsideHeightBounds(y)) {
            return null;
        }

        if (outsideThisChunkBounds(x, z)) {
            Vector3 positionToFind = position.cpy().add(x, 0, z);
            positionToFind.x = (float) Math.floor(positionToFind.x / Terrain.WIDTH) * Terrain.WIDTH;
            positionToFind.z = (float) Math.floor(positionToFind.z / Terrain.WIDTH) * Terrain.WIDTH;
            Chunk chunk = Terrain.findChunk(positionToFind);


            // Normalize x and z to be inside the other chunk
            if (x >= Terrain.WIDTH) {
                x = x - Terrain.WIDTH;
            } else if (x < 0) {
                x = Terrain.WIDTH + x;
            }

            if (z >= Terrain.WIDTH) {
                z = z - Terrain.WIDTH;
            } else if (z < 0) {
                z = Terrain.WIDTH + z;
            }

            if (chunk != null) {
                chunk.setLight(x, y, z, light);
                return chunk;
            } else {
                byte[] nonExistingChunkLightMap = lightMaps.get(positionToFind);
                if (nonExistingChunkLightMap == null) {
                    byte[] newLightMap = new byte[Terrain.HEIGHT * (Terrain.WIDTH * Terrain.WIDTH)];
                    lightMaps.put(positionToFind.cpy(), newLightMap);
                    nonExistingChunkLightMap = newLightMap;
                }
                setLightAtMap(x, y, z, light, nonExistingChunkLightMap);
            }

            return null;
        }
        setLightAtMap(x, y, z, light, lightMap);


        return null;
    }

    private void setLightAtMap(int x, int y, int z, byte light, byte[] lightMap) {
        if (light > darkness) {

            return;
        }
        int existingLight = lightMap[getLocationInArray(x, y, z)];

        if ((existingLight > light || existingLight == 0)) {
            lightMap[getLocationInArray(x, y, z)] = light;
        }
    }

    /**
     * Check if the block at the specified position exists or not. This is used by the VoxelMesh
     * to determinate if it needs to draw a mesh face or not depending on if it will be visible
     * or hidden by another block.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isBlockTransparent(int x, int y, int z) {

        if (y < 0) {
            return false; //Bottom should be false since we will never see it.
        }
        byte b = getByte(x, y, z);

        switch (b) {
            case 0:
                return true;
            default:
                return false;
        }
    }

    private byte getByte(int x, int y, int z) {
        if (outsideHeightBounds(y)) {
            return 0;
        }
        if (outsideThisChunkBounds(x, z)) {
            Vector3 positionToFind = position.cpy().add(x, y, z);
            Chunk chunk = Terrain.findChunk(positionToFind);
            if (chunk != null) {
                return chunk.getByte(x, y, z);
            } else {
                Vector3 worldPosition = position.cpy().add(x, y, z);
                return getByteAtWorldPosition(x, y, z, worldPosition);
            }
        }
        return map[getLocationInArray(x, y, z)];
    }

    private boolean outsideThisChunkBounds(int x, int z) {
        return x < 0 || z < 0 || x >= Terrain.WIDTH || z >= Terrain.WIDTH;
    }

    private boolean outsideHeightBounds(int y) {
        return y < 0 || y >= Terrain.HEIGHT;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRecalculating() {
        return isRecalculating;
    }
}
