package st.rhapsody.voxelengine.input;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.collision.Ray;
import st.rhapsody.voxelengine.terrain.Chunk;
import st.rhapsody.voxelengine.terrain.Terrain;

import java.util.Collection;
import java.util.List;

/**
 * Created by till on 22.02.15.
 */
public class PlayerController extends FirstPersonCameraController {
    private final Terrain terrain;
    private Camera camera;

    public PlayerController(Camera camera, Terrain terrain
    ) {
        super(camera);
        this.camera = camera;
        this.terrain = terrain;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button){
        Ray ray = camera.getPickRay(screenX,screenY);
        Collection<Chunk> chunkList =terrain.getChunks();
        for (Chunk chunk: chunkList){
            if (chunk.isVisible()){
                chunk.getRadius()
            }
        }
        return false;
    }
}
