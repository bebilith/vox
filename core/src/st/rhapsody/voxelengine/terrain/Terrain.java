package st.rhapsody.voxelengine.terrain;

import com.badlogic.gdx.math.Vector3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by nicklas on 4/24/14.
 */
public class Terrain {
    public static int WIDTH = 16;
    public static int HEIGHT = 64;
    public static int VIEWRANGE = 100;
    //public static final long SEED = -8675041922514645086l;
    public static final long SEED = 0; // Will generate a new landscape every time
    private final Vector3 position = new Vector3();
    private final Vector3 previousCameraPosition = new Vector3();
    // Store chunks in HashMap with Start position as key. Will not allow lookups on exakt position on ranges but fast lookup
    // on exact start position
    private static HashMap<Vector3,Chunk> chunks = new HashMap<Vector3,Chunk>();

    public Terrain() {

    }

    public void update(Vector3 camPos) {
        if (camPos.equals(previousCameraPosition)){
            return;
        }

        previousCameraPosition.set(camPos);

        for (float x = camPos.x - VIEWRANGE; x < camPos.x + VIEWRANGE; x += WIDTH) {
            for (float z = camPos.z - VIEWRANGE; z < camPos.z + VIEWRANGE; z += WIDTH) {
                position.set(x, 0, z);

                position.x = (float) Math.floor(position.x / WIDTH) * WIDTH;
                position.z = (float) Math.floor(position.z / WIDTH) * WIDTH;

                Chunk chunk = findChunk(position);

                if (chunk == null) {
                    Vector3 chunkPosition = position.cpy();
                    Chunk c = createRandomChunk(chunkPosition);
                    chunks.put(chunkPosition, c);
                }
            }
        }
    }

    public static Chunk findChunk(Vector3 pos) {
        if (chunks.containsKey(pos)){
            return chunks.get(pos);
        }
        return null;
    }

    private Chunk createRandomChunk(Vector3 position) {
        return new LandscapeChunk(position);
    }

    public Collection<Chunk> getChunks() {
        return chunks.values();
    }
}
