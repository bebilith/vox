package st.rhapsody.voxelengine.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import st.rhapsody.voxelengine.render.VoxelRender;
import st.rhapsody.voxelengine.terrain.Terrain;

/**
 * Created by nicklas on 4/24/14.
 */
public class VoxelScreen implements Screen {
    private PerspectiveCamera camera;
    private VoxelRender voxelRender;
    private ModelBatch voxelBatch;
    private FirstPersonCameraController firstPersonCameraController;
    private Environment environment;
    private BitmapFont font;
    private SpriteBatch spriteBatch;
    private Terrain terrain;
    private Texture texture;

    @Override
    public void render(float delta) {

        terrain.update(camera.position);

        tickPhysics(delta);

        clearOpenGL();

        renderModelBatches();
        renderSpriteBatches();

    }

    private void clearOpenGL() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0 / 255f, 52 / 255f, 131 / 255f, 0);
    }

    private void renderSpriteBatches() {
        spriteBatch.begin();
        font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() +
                "  -  visible/total chunks: " + VoxelRender.getNumberOfVisibleChunks() +
                "/" + VoxelRender.getNumberOfChunks() + "  -  visible/total blocks: " +
                VoxelRender.getNumberOfVisibleBlocks() + "/" + VoxelRender.getBlockCounter() +
                "  -  visible vertices:" + VoxelRender.getNumberOfVertices() + "  -  visible indicies: " +
                VoxelRender.getNumberOfIndicies(), 0, 20);

        spriteBatch.end();
    }

    private void renderModelBatches() {
        renderVoxelBatch();
    }

    private void renderVoxelBatch() {
        voxelBatch.begin(camera);
        voxelBatch.render(voxelRender, environment);
        voxelBatch.end();
    }

    private void tickPhysics(float delta) {
        firstPersonCameraController.update(delta);
        camera.update(true);
    }

    @Override
    public void resize(int width, int height) {
        createCamera(width, height);
        setup();
    }

    private void setup() {
        terrain = new Terrain();
        Material material = setupMaterialAndEnvironment();
        setupRendering(material);
        setupCameraController();
        font = new BitmapFont();

    }

    private void setupRendering(Material material) {
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        final ShaderProgram shaderProgram = setupShaders();

        voxelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(Renderable renderable) {
                Gdx.app.log("DefaultShaderProvider", "Creating new shader");
                    return new DefaultShader(renderable, new DefaultShader.Config(), shaderProgram);
                }
        });

        voxelRender = new VoxelRender(material, terrain, camera);
        spriteBatch = new SpriteBatch();
    }

    private ShaderProgram setupShaders(){
        ShaderProgram.pedantic = true;
        ShaderProgram shaderProgram = new ShaderProgram(Gdx.files.internal("data/shaders/shader.vs"), Gdx.files.internal("data/shaders/shader.fs"));
        System.out.println(shaderProgram.isCompiled() ? "Shaders compiled ok" : "Shaders didn't compile ok: " + shaderProgram.getLog());
        return shaderProgram;
    }

    private void setupCameraController() {
        firstPersonCameraController = new FirstPersonCameraController(camera);
        firstPersonCameraController.setVelocity(125);
        Gdx.input.setInputProcessor(firstPersonCameraController);
    }

    private Material setupMaterialAndEnvironment() {
        texture = new Texture(Gdx.files.internal("data/textures.png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 234 / 255f, 168 / 255f, 240 / 255f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 234 / 255f, 168 / 255f, 240 / 255f, 1f));
        return new Material("Material1", new TextureAttribute(TextureAttribute.Diffuse, texture));
    }

    private void createCamera(int width, int height) {
        camera = new PerspectiveCamera(67f, width, height);
        camera.near = 1f;
        camera.far = 500;
        camera.position.set(10, 12.5f, 10);
        camera.lookAt(0, 12.5f, 1);
        camera.rotate(camera.up, 182);
        camera.update();
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        texture.dispose();
        voxelBatch.dispose();
    }
}
