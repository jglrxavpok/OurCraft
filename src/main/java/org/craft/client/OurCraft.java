package org.craft.client;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

import org.craft.blocks.*;
import org.craft.client.render.*;
import org.craft.client.render.entity.*;
import org.craft.client.render.fonts.*;
import org.craft.entity.*;
import org.craft.maths.*;
import org.craft.resources.*;
import org.craft.utils.*;
import org.craft.utils.CollisionInfos.CollisionType;
import org.craft.world.*;
import org.craft.world.populators.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.*;

public class OurCraft implements Runnable
{

    private File                          gameFolder;
    private int                           displayWidth  = 960;
    private int                           displayHeight = 540;
    private long                          lastTime      = 0;
    private boolean                       running       = true;
    private RenderEngine                  renderEngine  = null;
    private Matrix4                       modelMatrix;
    private Shader                        basicShader;
    private ClasspathSimpleResourceLoader classpathLoader;
    private RenderBlocks                  renderBlocks;
    private World                         clientWorld;
    private MouseHandler                  mouseHandler;
    private EntityPlayer                  player;
    private static OurCraft               instance;
    private CollisionInfos                objectInFront = null;
    private Matrix4                       projectionHud;
    private Texture                       crosshairTexture;
    private OpenGLBuffer                  crosshairBuffer;
    private FallbackRender<Entity>        fallbackRenderer;
    private Runtime                       runtime;
    private FontRenderer                  fontRenderer;

    public OurCraft(File gameFolder)
    {
        instance = this;

        this.gameFolder = gameFolder;
        this.classpathLoader = new ClasspathSimpleResourceLoader("assets");
        runtime = Runtime.getRuntime();
    }

    public void start()
    {
        new Thread(this).start();
    }

    public void run()
    {
        try
        {
            JFrame frame = new JFrame();
            frame.setTitle("OurCraft - " + getVersion());
            Canvas canvas = new Canvas();
            frame.add(canvas);
            canvas.setPreferredSize(new Dimension(displayWidth, displayHeight));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            Display.setParent(canvas);
            Display.create();
            mouseHandler = new MouseHandler();
            mouseHandler.grab();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -1, 1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);

            GL11.glLoadIdentity();

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            renderEngine = new RenderEngine();
            projectionHud = new Matrix4().initOrthographic(0, Display.getWidth(), Display.getHeight(), 0, -1, 1);
            //INVALID ENUM START
            basicShader = new Shader(new String(classpathLoader.getResource(new ResourceLocation("ourcraft/shaders", "base.vsh")).getData(), "UTF-8"), new String(classpathLoader.getResource(new ResourceLocation("ourcraft/shaders", "base.fsh")).getData(), "UTF-8"));
            basicShader.bind();
            basicShader.setUniform("projection", projectionHud);
            basicShader.setUniform("modelview", new Matrix4().initIdentity());
            //INVALID ENUM END
            renderEngine.renderSplashScreen();

            fontRenderer = new BaseFontRenderer();
            Display.update();

            Blocks.init();

            WorldGenerator generator = new WorldGenerator();
            generator.addPopulator(new RockPopulator());
            generator.addPopulator(new GrassPopulator());
            generator.addPopulator(new TreePopulator());
            clientWorld = new World(new BaseChunkProvider(), generator);
            renderBlocks = new RenderBlocks(renderEngine);

            modelMatrix = new Matrix4().initIdentity();

            player = new EntityPlayer(clientWorld);
            player.setLocation(0, 160 + 17, 0);
            clientWorld.spawn(player);
            renderEngine.setRenderViewEntity(player);

            this.crosshairTexture = OpenGLHelper.loadTexture(ImageIO.read(OurCraft.class.getResourceAsStream("/assets/ourcraft/textures/crosshair.png")));
            crosshairBuffer = new OpenGLBuffer();
            crosshairBuffer.addVertex(new Vertex(Vector3.get(Display.getWidth() / 2 - 8, Display.getHeight() / 2 - 8, 0), new Vector2(0, 0)));
            crosshairBuffer.addVertex(new Vertex(Vector3.get(Display.getWidth() / 2 + 8, Display.getHeight() / 2 - 8, 0), new Vector2(1, 0)));
            crosshairBuffer.addVertex(new Vertex(Vector3.get(Display.getWidth() / 2 + 8, Display.getHeight() / 2 + 8, 0), new Vector2(1, 1)));
            crosshairBuffer.addVertex(new Vertex(Vector3.get(Display.getWidth() / 2 - 8, Display.getHeight() / 2 + 8, 0), new Vector2(0, 1)));

            crosshairBuffer.addIndex(0);
            crosshairBuffer.addIndex(1);
            crosshairBuffer.addIndex(2);

            crosshairBuffer.addIndex(2);
            crosshairBuffer.addIndex(3);
            crosshairBuffer.addIndex(0);
            crosshairBuffer.upload();
            crosshairBuffer.clearVertices();

            fallbackRenderer = new FallbackRender<Entity>();
            new ThreadGetChunksFromCamera(this).start();
            running = true;
            while(running && !Display.isCloseRequested())
            {
                tick(1000 / 60);
                Display.sync(60);
                Display.update();
            }
            Display.destroy();
            frame.dispose();
            Log.error("BYE");
            System.exit(0);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void tick(final int time)
    {
        render();
        update(time);

    }

    private void update(final int time)
    {
        mouseHandler.update();
        if(player != null)
        {
            objectInFront = player.getObjectInFront(5f);
        }
        boolean canUpdate = (System.currentTimeMillis() - lastTime) >= time;
        if(canUpdate)
        {

            if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
            {
                running = false;
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
            {
                player.jump();
                this.resetTime();
            }

        }
        clientWorld.update(time, canUpdate);

    }

    public void resetTime()
    {
        lastTime = System.currentTimeMillis();
    }

    public long getLastTime()
    {
        return lastTime;
    }

    private void render()
    {
        ArrayList<Chunk> visiblesChunks = new ArrayList<Chunk>();
        if(player != null)
        {
            int renderDistance = 8;
            int ox = (int) renderEngine.getRenderViewEntity().getX();
            int oy = (int) renderEngine.getRenderViewEntity().getY();
            int oz = (int) renderEngine.getRenderViewEntity().getZ();
            for(int x = -renderDistance; x < renderDistance; x++ )
            {
                for(int y = -renderDistance; y < renderDistance; y++ )
                {
                    for(int z = -renderDistance; z < renderDistance; z++ )
                    {
                        int fx = x * 16 + ox;
                        int fy = y * 16 + oy;
                        int fz = z * 16 + oz;
                        if(fy < 0)
                            continue;
                        synchronized(clientWorld)
                        {
                            Chunk c = clientWorld.getChunkProvider().get(clientWorld, (int) Math.floor((float) fx / 16f), (int) Math.floor((float) fy / 16f), (int) Math.floor((float) fz / 16f));
                            if(c != null)
                                visiblesChunks.add(c);
                        }
                    }
                }
            }
        }
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        glClearColor(0, 0.6666667f, 1, 1);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        renderEngine.enableGLCap(GL_DEPTH_TEST);
        basicShader.bind();
        basicShader.setUniform("modelview", this.modelMatrix);
        basicShader.setUniform("projection", this.renderEngine.getProjectionMatrix());
        renderBlocks.render(clientWorld, visiblesChunks);
        for(Entity e : clientWorld.getEntitiesList())
        {
            fallbackRenderer.render(renderEngine, e, e.getX(), e.getY(), e.getZ());
        }

        glClear(GL_DEPTH_BUFFER_BIT);
        renderEngine.disableGLCap(GL_DEPTH_TEST);
        if(objectInFront != null && objectInFront.type == CollisionType.BLOCK)
        {
            glBindTexture(GL_TEXTURE_2D, 0);
            glBegin(GL_LINES);
            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z);

            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z + 1);
            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z + 1);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z);
            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z + 1);

            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z + 1);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z);

            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z);

            glVertex3d(objectInFront.x, objectInFront.y, objectInFront.z + 1);
            glVertex3d(objectInFront.x, objectInFront.y + 1, objectInFront.z + 1);

            glVertex3d(objectInFront.x + 1, objectInFront.y, objectInFront.z + 1);
            glVertex3d(objectInFront.x + 1, objectInFront.y + 1, objectInFront.z + 1);

            glEnd();
        }

        basicShader.setUniform("projection", projectionHud);

        renderEngine.enableGLCap(GL_COLOR_LOGIC_OP);
        glLogicOp(GL_XOR);
        renderEngine.renderBuffer(crosshairBuffer, crosshairTexture);
        renderEngine.disableGLCap(GL_COLOR_LOGIC_OP);

        int errorFlag = glGetError();
        // If an error has occurred...
        if(errorFlag != GL_NO_ERROR)
        {
            // Print the error to System.err.
            Log.error("[GL ERROR] " + gluErrorString(errorFlag));
        }

        fontRenderer.drawString("Free memory: " + (getFreeMemory() / 1000L) + "kb:" + (getFreeMemory() / 1000000L) + "Mb", 0x00FF00, 0, 0, renderEngine);
        fontRenderer.drawString("Used memory: " + (getUsedMemory() / 1000L) + "kb:" + (getUsedMemory() / 1000000L) + "Mb", 0x00FF00, 0, 15, renderEngine);
        fontRenderer.drawString("Total memory: " + (getTotalMemory() / 1000L) + "kb:" + (getTotalMemory() / 1000000L) + "Mb", 0x00FF00, 0, 30, renderEngine);
        fontRenderer.drawString("Max available memory: " + (getMaxMemory() / 1000L) + "kb:" + (getMaxMemory() / 1000000L) + "Mb", 0x00FF00, 0, 45, renderEngine);
    }

    public static OurCraft getOurCraft()
    {
        return instance;
    }

    public ResourceLoader getBaseLoader()
    {
        return classpathLoader;
    }

    public MouseHandler getMouseHandler()
    {
        return mouseHandler;
    }

    public CollisionInfos getObjectInFront()
    {
        return objectInFront;
    }

    public boolean isRunning()
    {
        return running;
    }

    public World getClientWorld()
    {
        return clientWorld;
    }

    public RenderEngine getRenderEngine()
    {
        return renderEngine;
    }

    public static String getVersion()
    {
        return "OurCraft:BuildNumber";
    }

    public int getDisplayWidth()
    {
        return displayWidth;
    }

    public int getDisplayHeight()
    {
        return displayHeight;
    }

    public Runtime getRuntimeInfos()
    {
        return runtime;
    }

    public long getFreeMemory()
    {
        return runtime.freeMemory();
    }

    public long getMaxMemory()
    {
        return runtime.maxMemory();
    }

    public long getTotalMemory()
    {
        return runtime.totalMemory();
    }

    public long getUsedMemory()
    {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }
}
