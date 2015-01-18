package st.rhapsody.voxelengine.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import st.rhapsody.voxelengine.VoxelEngine;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width=1280;
        config.height=1024;
        config.useGL30 = false; // Set to false to use GL20
        config.vSyncEnabled = false;
        //config.backgroundFPS = 0;
        //config.foregroundFPS = 0;
		new LwjglApplication(new VoxelEngine(), config);
	}
}
