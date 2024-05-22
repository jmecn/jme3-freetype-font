package io.github.jmecn.font.editor;

import com.jme3.app.Application;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import com.jme3.system.lwjgl.LwjglWindow;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImInt;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public final class ImGuiJme3 {

    static Logger logger = LoggerFactory.getLogger(ImGuiJme3.class);

    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static boolean initialized = false;
    private static long windowHandle = 0L;

    private ImGuiJme3() {}

    public static void initialize(Application application) {
        JmeContext context = application.getContext();

        if (initialized) {
            logger.info("imgui has already been initialized.");
            return;
        }

        // init imgui
        windowHandle = ((LwjglWindow)context).getWindowHandle();
        ImGui.createContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init(decideGlslVersion());

        initialized = true;

        logger.debug("imgui is initialized");
    }

    /**
     * Method to load generated font textures to GL.
     */
    public static void refreshFontTexture() {
        if (!initialized) {
            return;
        }

        ImInt fontW = new ImInt();
        ImInt fontH = new ImInt();
        ImGuiIO imGuiIO = ImGui.getIO();
        ByteBuffer fontData = imGuiIO.getFonts().getTexDataAsRGBA32(fontW, fontH);
        int originalTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
        int fontTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, fontW.get(), fontH.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, fontData);
        imGuiIO.getFonts().setTexID(fontTexture);
        glBindTexture(GL_TEXTURE_2D, originalTexture);
    }

    /**
     * Decides the glsl version.
     * @return the glsl version string
     */
    public static String decideGlslVersion() {
        return JmeSystem.getPlatform().getOs() == Platform.Os.MacOS ? "#version 150" : "#version 130";
    }

    /**
     * Method called at the beginning of the main cycle.
     * It starts a new ImGui frame.
     */
    public static void startFrame() {
        if (!initialized) {
            logger.warn("imgui is not initialized");
            return;
        }
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    /**
     * Method called in the end of the main cycle.
     * It renders ImGui to prepare an updated frame.
     */
    public static void endFrame() {
        if (!initialized) {
            logger.warn("imgui is not initialized");
            return;
        }
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }

    }
    /**
     * Method to dispose all used ImGui resources.
     * Can be called more than once, with the additional call nothing effects.
     */
    public static void dispose() {
        if (!initialized) {
            logger.debug("imgui is not initialized.");
            return;
        }
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();

        initialized = false;
        windowHandle = 0L;

        logger.debug("imgui is disposed");
    }

}
