package st.rhapsody.voxelengine;

import com.badlogic.gdx.Game;
import st.rhapsody.voxelengine.screen.VoxelScreen;

public class VoxelEngine extends Game {

    @Override
    public void create() {
        setScreen(new VoxelScreen());
    }
}
