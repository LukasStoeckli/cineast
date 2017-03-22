package org.vitrivr.cineast.core.render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.m3d.VoxelGrid;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.GL2GL3.GL_POINT;
import static com.jogamp.opengl.GLContext.CONTEXT_CURRENT;
import static com.jogamp.opengl.GLContext.CONTEXT_CURRENT_NEW;

/**
 * This class can be used to render 3D models (Meshes or Voxel-models) using the JOGL rendering environment. It
 * currently has the following features:
 *
 * - Rendering of single Mesh or VoxelGrid
 * - Free positioning of the camera in terms of either cartesian or polar coordinate
 * - Snapshot of the rendered image can be obtained at any time.
 *
 * The class supports offscreen rendering and can be accessed by multipled Threads. However, the multithreading
 * model of JOGL requires a thread to retain() and release() the JOGLOffscreenRenderer before rendering anything
 * by calling the respective function.
 *
 * @see org.vitrivr.cineast.core.data.m3d.Mesh
 * @see org.vitrivr.cineast.core.data.m3d.VoxelGrid
 *
 * @author rgasser
 * @version 1.0
 * @created 29.12.16
 */
public class JOGLOffscreenRenderer implements Renderer {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Default GLProfile to be used. Should be GL2. */
    private static final GLProfile glprofile = GLProfile.get(GLProfile.GL2);

    /** GLCapabilities. Can be used to enable/disable hardware acceleration etc. */
    private static final GLCapabilities capabilities = new GLCapabilities(glprofile);

    /** OpenGL Utility Library reference */
    private final GLU glu;

    /** OpenGL context reference used for drawing. */
    private final GL2 gl;

    /** Width of the JOGLOffscreenRenderer in pixels. */
    private final int width;

    /** Height of the JOGLOffscreenRenderer in pixels. */
    private final int height;

    /** Aspect-ratio of the JOGLOffscreenRenderer. */
    private final float aspect;

    /** Polygon-mode used during rendering. */
    private int polygonmode = GL_FILL;

    /** Lock that makes sure that only a single Thread is using the classes rendering facility at a time. */
    private ReentrantLock lock = new ReentrantLock(true);

    /** List of object handles that should be rendered. */
    private final List<Integer> objects = new ArrayList<>();

    /*
     * This code-block can be used to configure the off-screen renderer's capabilities.
     */
    static {
        capabilities.setOnscreen(false);
        capabilities.setHardwareAccelerated(true);
    }

    /**
     * Default constructor. Defines the width and the height of this JOGLOffscreenRenderer and
     * initializes all the required OpenGL bindings.
     *
     * @param width Width in pixels.
     * @param height Height in pixels.
     */
    public JOGLOffscreenRenderer(int width, int height) {
        /* Assign width and height. */
        this.width = width;
        this.height = height;
        this.aspect = (float) width / (float) height;

        /* Initialize GLOffscreenAutoDrawable. */
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glprofile);
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(null,capabilities,null,width,height);
        drawable.display();

        /* Initialize GLU and GL2. */
        this.glu = new GLU();
        this.gl = drawable.getGL().getGL2();

        /* Set default color. */
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Getter for width.
     *
     * @return Width of the JOGLOffscreenRenderer.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Getter for height.
     *
     * @return Height of the JOGLOffscreenRenderer.
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Getter for aspect.
     *
     * @return Aspect ratio of the JOGLOffscreenRenderer.
     */
    public final float getAspect() {
        return aspect;
    }

    /**
     * Getter for polygonmode.
     *
     * @return Polygonmode for drawing, either GL_POINT, GL_LINE or GL_FILL.
     */
    public int getPolygonmode() {
        return polygonmode;
    }

    /**
     * Setter for polygonmode.
     *
     * @param polygonmode Polygonmode for drawing, either GL_POINT, GL_LINE or GL_FILL.
     */
    public synchronized void setPolygonmode(int polygonmode) {
        if (polygonmode == GL_POINT || polygonmode == GL_LINE || polygonmode == GL_FILL) {
            this.polygonmode = polygonmode;
        }
    }

    /**
     *
     */
    public void render() {
        /* Clear context. */
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        /* Switch matrix mode to modelview. */
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);
        gl.glLoadIdentity();
        gl.glPolygonMode(GL_FRONT_AND_BACK, this.polygonmode);

        /* Call list. */
        for (Integer handle : this.objects) {
            gl.glCallList(handle);
        }
    }

    /**
     * Renders a new Mesh object and thereby removes any previously rendered object
     *
     * @param mesh Mesh that should be rendered
     */
    public void assemble(Mesh mesh) {
        int meshList = gl.glGenLists(1);
        this.objects.add(meshList);
        gl.glNewList(meshList, GL_COMPILE);
        {
            for (Mesh.Face face : mesh.getFaces()) {
                /* Extract normals and vertices. */
                java.util.List<Vector3f> vertices = face.getVertices();
                java.util.List<Vector3f> colors = face.getColors();
                java.util.List<Vector3f> normals = face.getNormals();

                /* Determine gl_draw_type. */
                int gl_draw_type = GL_TRIANGLES;
                if (face.getType() == Mesh.FaceType.QUAD) gl_draw_type = GL_QUADS;

                /* Drawing is handled differently depending on whether its a TRI or QUAD mesh. */
                gl.glBegin(gl_draw_type);
                {
                    gl.glColor3f(colors.get(0).x, colors.get(0).y, colors.get(0).z);
                    gl.glVertex3f(vertices.get(0).x, vertices.get(0).y, vertices.get(0).z);
                    gl.glColor3f(colors.get(1).x, colors.get(1).y, colors.get(1).z);
                    gl.glVertex3f(vertices.get(1).x, vertices.get(1).y, vertices.get(1).z);
                    gl.glColor3f(colors.get(2).x, colors.get(2).y, colors.get(2).z);
                    gl.glVertex3f(vertices.get(2).x, vertices.get(2).y, vertices.get(2).z);
                    if (face.getType() == Mesh.FaceType.QUAD) {
                        gl.glColor3f(colors.get(3).x, colors.get(3).y, colors.get(3).z);
                        gl.glVertex3f(vertices.get(3).x, vertices.get(3).y, vertices.get(3).z);
                    }

                    if (normals != null && normals.size() >= 3) {
                        gl.glNormal3f(normals.get(0).x, normals.get(0).y, normals.get(0).z);
                        gl.glNormal3f(normals.get(1).x, normals.get(1).y, normals.get(1).z);
                        gl.glNormal3f(normals.get(2).x, normals.get(2).y, normals.get(2).z);
                        if (face.getType() == Mesh.FaceType.QUAD) gl.glNormal3f(normals.get(3).x, normals.get(3).y, normals.get(3).z);
                    }
                }
                gl.glEnd();
            }
        }
        gl.glEndList();
    }

    /**
     * Assembles a new VoxelGrid object and thereby adds it to the list of objects that
     * should be rendered.
     *
     * @param grid VoxelGrid that should be rendered.
     */
    public void assemble(VoxelGrid grid) {
        int meshList = gl.glGenLists(1);
        this.objects.add(meshList);
        gl.glNewList(meshList, GL_COMPILE);
        {
            boolean[] visible = {true, true, true, true, true, true};

            for (int i = 0; i < grid.getSizeX(); i++) {
                for (int j = 0; j < grid.getSizeY(); j++) {
                    for (int k = 0; k < grid.getSizeZ(); k++) {
                        /* Skip Voxel if its inactive. */
                        if (!grid.get(i,j,k).getVisible()) continue;

                        /* Extract center of the voxel. */
                        float x = grid.getCenter().x + grid.get(i,j,k).getCenter().x;
                        float y = grid.getCenter().y + grid.get(i,j,k).getCenter().y;
                        float z = grid.getCenter().z + grid.get(i,j,k).getCenter().z;

                        /* Determine which faces to draw: Faced that are covered by another active voxel are switched off. */
                        if(i > 0) visible[0] = !grid.get(i-1,j,k).getVisible();
                        if(i < grid.getSizeX()-1) visible[1] = !grid.get(i+1,j,k).getVisible();
                        if(j > 0) visible[2] = !grid.get(i,j-1,k).getVisible();
                        if(j < grid.getSizeY()-1) visible[3] = !grid.get(i,j+1,k).getVisible();
                        if(k > 0) visible[4] = !grid.get(i,j,k-1).getVisible();
                        if(k < grid.getSizeZ()-1) visible[5] = !grid.get(i,j,k+1).getVisible();

                        /* Draw the cube. */
                        gl.glBegin(GL_QUADS);
                        {
                            /* 1 */
                            if (visible[0]) {
                                gl.glVertex3f(x + grid.getHalfResolution(), y - grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y - grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y + grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y + grid.getHalfResolution(), z - grid.getHalfResolution());
                            }

                            /* 2 */
                            if (visible[1]) {
                                gl.glVertex3f(x - grid.getHalfResolution(), y - grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y - grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y + grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y + grid.getHalfResolution(), z + grid.getHalfResolution());
                            }

                            /* 3 */
                            if (visible[2]) {
                                gl.glVertex3f(x + grid.getHalfResolution(), y - grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y - grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y + grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y + grid.getHalfResolution(), z + grid.getHalfResolution());
                            }

                            /* 4 */
                            if (visible[3]) {
                                gl.glVertex3f(x - grid.getHalfResolution(), y - grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y - grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y + grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y + grid.getHalfResolution(), z - grid.getHalfResolution());
                            }

                            /* 5 */
                            if (visible[4]) {
                                gl.glVertex3f(x - grid.getHalfResolution(), y - grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y - grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y - grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y - grid.getHalfResolution(), z + grid.getHalfResolution());
                            }

                            /* 6 */
                            if (visible[5]) {
                                gl.glVertex3f(x + grid.getHalfResolution(), y + grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y + grid.getHalfResolution(), z - grid.getHalfResolution());
                                gl.glVertex3f(x - grid.getHalfResolution(), y + grid.getHalfResolution(), z + grid.getHalfResolution());
                                gl.glVertex3f(x + grid.getHalfResolution(), y + grid.getHalfResolution(), z + grid.getHalfResolution());
                            }
                        }
                        gl.glEnd();
                    }
                }
            }
        }
        gl.glEndList();
    }

    /**
     * Changes the positionCamera of the camera.
     *
     * @param ex x Position of the Camera
     * @param ey y Position of the Camera
     * @param ez z Position of the Camera
     * @param cx x Position of the object of interest (i.e. the point at which the camera looks).
     * @param cy y Position of the object of interest (i.e. the point at which the camera looks).
     * @param cz z Position of the object of interest (i.e. the point at which the camera looks).
     */
    public final void positionCamera(double ex, double ey, double ez, double cx, double cy, double cz) {
        /* Check context. */
        if (!this.checkContext()) return;

        /* Switch matrix mode to projection. */
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        /* Set default perspective. */
        glu.gluPerspective(45.0f, this.aspect, 0.01f, 100.0f);

        /* Update camera position. */
        glu.gluLookAt(ex,ey,ez,cx,cy,cz,0.0,-1.0,0.0);
    }

    /**
     * Clears buffers to preset-values.
     */
    public final void clear() {
        this.clear(Color.BLACK);
    }

    /**
     * Clears buffers to preset-values and applies a user-defined background colour.
     *
     * @param color The background colour to be used.
     */
    public void clear(Color color) {
        if (!this.checkContext()) return;
        for (Integer handle : this.objects) {
            gl.glDeleteLists(handle, 1);
        }
        gl.glClearColorIi(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        this.objects.clear();
    }

    /**
     * Obtains and returns a BufferedImage in AWT orientation from the current JOGLOffscreenRenderer.
     *
     * @return BufferedImage containing a snapshot of the current render-buffer.
     */
    public final BufferedImage obtain() {
        /* Create and return a BufferedImage from buffer. */
        if (!this.checkContext()) return null;
        AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(gl.getGL2().getGLProfile(), false);
        return glReadBufferUtil.readPixelsToBufferedImage(gl.getGL2(), true);
    }

    /**
     * Makes the current thread try to retain the GLContext of this JOGLOffscreenRenderer. The
     * method returns true upon success and false otherwise.
     *
     * <b>Important: </b> Only one thread can retain a GLContext at a time. Relinquish the thread by
     * calling release().
     *
     * @return True if GLContext was retained and false otherwise.
     */
    public final boolean retain() {
        this.lock.lock();
        int result = this.gl.getContext().makeCurrent();
        if (result == CONTEXT_CURRENT_NEW || result == CONTEXT_CURRENT) {
            return true;
        } else {
            this.lock.unlock();
            LOGGER.error("Thread '{}' failed to retain JOGLOffscreenRenderer.", Thread.currentThread().getName());
            return false;
        }

    }

    /**
     * Makes the current thread release its ownership of the current JOGLOffscreenRenderer's GLContext.
     */
    public final void release() {
        if (this.checkContext()) {
            this.gl.getContext().release();
            this.lock.unlock();
        }
    }

    /**
     * Checks if the thread the GLContext is assigned to is equal to the Thread the current
     * code is being executed in.
     *
     * @return True if context-thread is equal to current thread and false otherwise,
     */
    private boolean checkContext() {
        if (!this.lock.isHeldByCurrentThread()) {
            LOGGER.error("Cannot access JOGLOffscreenRenderer because current thread '{}' does not own its GLContext.", Thread.currentThread().getName());
            return false;
        } else {
            return true;
        }
    }
}
