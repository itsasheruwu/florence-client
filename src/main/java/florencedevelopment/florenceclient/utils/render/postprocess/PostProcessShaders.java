package florencedevelopment.florenceclient.utils.render.postprocess;

import florencedevelopment.florenceclient.FlorenceClient;
import florencedevelopment.florenceclient.events.render.Render2DEvent;
import florencedevelopment.florenceclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;

import static florencedevelopment.florenceclient.FlorenceClient.mc;

public class PostProcessShaders {
    public static EntityShader CHAMS;
    public static EntityShader ENTITY_OUTLINE;
    public static PostProcessShader STORAGE_OUTLINE;

    private PostProcessShaders() {}

    @PreInit
    public static void init() {
        CHAMS = new ChamsShader();
        ENTITY_OUTLINE = new EntityOutlineShader();
        STORAGE_OUTLINE = new StorageOutlineShader();

        FlorenceClient.EVENT_BUS.subscribe(PostProcessShaders.class);
    }

    public static void beginRender() {
        CHAMS.clearTexture();
        ENTITY_OUTLINE.clearTexture();
        STORAGE_OUTLINE.clearTexture();
    }

    public static void submitEntityVertices() {
        CHAMS.submitVertices();
        ENTITY_OUTLINE.submitVertices();
    }

    @EventHandler
    private static void onRender(Render2DEvent event) {
        CHAMS.render();
        ENTITY_OUTLINE.render();
    }

    public static void onResized(int width, int height) {
        if (mc == null) return;

        CHAMS.onResized(width, height);
        ENTITY_OUTLINE.onResized(width, height);
        STORAGE_OUTLINE.onResized(width, height);
    }
}
