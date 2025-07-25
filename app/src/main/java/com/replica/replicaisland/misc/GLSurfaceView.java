/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.replica.replicaisland.misc;

import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLDebugHelper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.replica.replicaisland.debug.DebugLog;

import java.io.Writer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * An implementation of SurfaceView that uses the dedicated surface for
 * displaying OpenGL rendering.
 * <p>
 * A GLSurfaceView provides the following features:
 * <p>
 * <ul>
 * <li>Manages a surface, which is a special piece of memory that can be
 * composited into the Android view system.
 * <li>Manages an EGL display, which enables OpenGL to render into a surface.
 * <li>Accepts a user-provided Renderer object that does the actual rendering.
 * <li>Renders on a dedicated thread to decouple rendering performance from the
 * UI thread.
 * <li>Supports both on-demand and continuous rendering.
 * <li>Optionally wraps, traces, and/or error-checks the renderer's OpenGL calls.
 * </ul>
 *
 * <h3>Using GLSurfaceView</h3>
 * <p>
 * Typically you use GLSurfaceView by subclassing it and overriding one or more
 * of the View system input event methods. If your application does not need to
 * override event methods then GLSurfaceView can be used as-is. For the most
 * part GLSurfaceView behavior is customized by calling "set" methods rather
 * than by subclassing. For example, unlike a regular View, drawing is
 * delegated to a separate Renderer object which is registered with the
 * GLSurfaceView using the {@link #setRenderer(Renderer)} call.
 * <p>
 * <h3>Initializing GLSurfaceView</h3>
 * All you have to do to initialize a GLSurfaceView is call {@link #setRenderer(Renderer)}.
 * However, if desired, you can modify the default behavior of GLSurfaceView by calling one or
 * more of these methods before calling setRenderer:
 * <ul>
 * <li>{@link #setDebugFlags(int)}
 * <li>{@link #setEGLConfigChooser(boolean)}
 * <li>{@link #setEGLConfigChooser(EGLConfigChooser)}
 * <li>{@link #setEGLConfigChooser(int, int, int, int, int, int)}
 * <li>{@link #setGLWrapper(GLWrapper)}
 * </ul>
 * <p>
 * <h4>Choosing an EGL Configuration</h4>
 * A given Android device may support multiple possible types of drawing surfaces.
 * The available surfaces may differ in how may channels of data are present, as
 * well as how many bits are allocated to each channel. Therefore, the first thing
 * GLSurfaceView has to do when starting to render is choose what type of surface to use.
 * <p>
 * By default GLSurfaceView chooses an available surface that's closest to a 16-bit R5G6B5 surface
 * with a 16-bit depth buffer and no stencil. If you would prefer a different surface (for example,
 * if you do not need a depth buffer) you can override the default behavior by calling one of the
 * setEGLConfigChooser methods.
 * <p>
 * <h4>Debug Behavior</h4>
 * You can optionally modify the behavior of GLSurfaceView by calling
 * one or more of the debugging methods {@link #setDebugFlags(int)},
 * and {@link #setGLWrapper}. These methods may be called before and/or after setRenderer, but
 * typically they are called before setRenderer so that they take effect immediately.
 * <p>
 * <h4>Setting a Renderer</h4>
 * Finally, you must call {@link #setRenderer} to register a {@link Renderer}.
 * The renderer is
 * responsible for doing the actual OpenGL rendering.
 * <p>
 * <h3>Rendering Mode</h3>
 * Once the renderer is set, you can control whether the renderer draws
 * continuously or on-demand by calling
 * {@link #setRenderMode}. The default is continuous rendering.
 * <p>
 * <h3>Activity Life-cycle</h3>
 * A GLSurfaceView must be notified when the activity is paused and resumed. GLSurfaceView clients
 * are required to call {@link #onPause()} when the activity pauses and
 * {@link #onResume()} when the activity resumes. These calls allow GLSurfaceView to
 * pause and resume the rendering thread, and also allow GLSurfaceView to release and recreate
 * the OpenGL display.
 * <p>
 * <h3>Handling events</h3>
 * <p>
 * To handle an event you will typically subclass GLSurfaceView and override the
 * appropriate method, just as you would with any other View. However, when handling
 * the event, you may need to communicate with the Renderer object
 * that's running in the rendering thread. You can do this using any
 * standard Java cross-thread communication mechanism. In addition,
 * one relatively easy way to communicate with your renderer is
 * to call
 * {@link #queueEvent(Runnable)}. For example:
 * <pre class="prettyprint">
 * class MyGLSurfaceView extends GLSurfaceView {
 * <p>
 *     private MyRenderer mMyRenderer;
 * <p>
 *     public void start() {
 *         mMyRenderer = ...;
 *         setRenderer(mMyRenderer);
 *     }
 * <p>
 *     public boolean onKeyDown(int keyCode, KeyEvent event) {
 *         if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
 *             queueEvent(new Runnable() {
 *                 // This method will be called on the rendering
 *                 // thread:
 *                 public void run() {
 *                     mMyRenderer.handleDpadCenter();
 *                 }});
 *             return true;
 *         }
 *         return super.onKeyDown(keyCode, event);
 *     }
 * }
 * </pre>
 */
public class GLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    /**
     * The renderer only renders
     * when the surface is created, or when {@link #requestRender} is called.
     *
     * @see #getRenderMode()
     * @see #setRenderMode(int)
     */
    public final static int RENDERMODE_WHEN_DIRTY = 0;
    /**
     * The renderer is called
     * continuously to re-render the scene.
     *
     * @see #getRenderMode()
     * @see #setRenderMode(int)
     * @see #requestRender()
     */
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    /**
     * Check glError() after every GL call and throw an exception if glError indicates
     * that an error has occurred. This can be used to help track down which OpenGL ES call
     * is causing an error.
     *
     * @see #getDebugFlags
     * @see #setDebugFlags
     */
    public final static int DEBUG_CHECK_GL_ERROR = 1;
    /**
     * Log GL calls to the system log at "verbose" level with tag "GLSurfaceView".
     *
     * @see #getDebugFlags
     * @see #setDebugFlags
     */
    public final static int DEBUG_LOG_GL_CALLS = 2;
    private final static boolean LOG_THREADS = false;
    private final static boolean LOG_SURFACE = true;
    private final static boolean LOG_RENDERER = false;
    // Work-around for bug 2263168
    private final static boolean DRAW_TWICE_AFTER_SIZE_CHANGED = true;
    private static final GLThreadManager sGLThreadManager = new GLThreadManager();
    private boolean mSizeChanged = true;
    private GLThread mGLThread;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLWrapper mGLWrapper;
    private int mDebugFlags;
    private int mEGLContextClientVersion;

    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     */
    public GLSurfaceView(Context context) {
        super(context);
        init();
    }

    /**
     * Standard View constructor. In order to render something, you
     * must call {@link #setRenderer} to register a renderer.
     */
    public GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }

    /**
     * Set the glWrapper. If the glWrapper is not null, its
     * {@link GLWrapper#wrap(GL)} method is called
     * whenever a surface is created. A GLWrapper can be used to wrap
     * the GL object that's passed to the renderer. Wrapping a GL
     * object enables examining and modifying the behavior of the
     * GL calls made by the renderer.
     * <p>
     * Wrapping is typically used for debugging purposes.
     * <p>
     * The default value is null.
     *
     * @param glWrapper the new GLWrapper
     */
    public void setGLWrapper(GLWrapper glWrapper) {
        mGLWrapper = glWrapper;
    }

    /**
     * Get the current value of the debug flags.
     *
     * @return the current value of the debug flags.
     */
    public int getDebugFlags() {
        return mDebugFlags;
    }

    /**
     * Set the debug flags to a new value. The value is
     * constructed by OR-together zero or more
     * of the DEBUG_CHECK_* constants. The debug flags take effect
     * whenever a surface is created. The default value is zero.
     *
     * @param debugFlags the new debug flags
     * @see #DEBUG_CHECK_GL_ERROR
     * @see #DEBUG_LOG_GL_CALLS
     */
    public void setDebugFlags(int debugFlags) {
        mDebugFlags = debugFlags;
    }

    /**
     * Set the renderer associated with this view. Also starts the thread that
     * will call the renderer, which in turn causes the rendering to start.
     * <p>This method should be called once and only once in the life-cycle of
     * a GLSurfaceView.
     * <p>The following GLSurfaceView methods can only be called <em>before</em>
     * setRenderer is called:
     * <ul>
     * <li>{@link #setEGLConfigChooser(boolean)}
     * <li>{@link #setEGLConfigChooser(EGLConfigChooser)}
     * <li>{@link #setEGLConfigChooser(int, int, int, int, int, int)}
     * </ul>
     * <p>
     * The following GLSurfaceView methods can only be called <em>after</em>
     * setRenderer is called:
     * <ul>
     * <li>{@link #getRenderMode()}
     * <li>{@link #onPause()}
     * <li>{@link #onResume()}
     * <li>{@link #queueEvent(Runnable)}
     * <li>{@link #requestRender()}
     * <li>{@link #setRenderMode(int)}
     * </ul>
     *
     * @param renderer the renderer to use to perform OpenGL drawing.
     */
    public void setRenderer(Renderer renderer) {
        checkRenderThreadState();
        if (mEGLConfigChooser == null) {
            mEGLConfigChooser = new SimpleEGLConfigChooser(true);
        }
        if (mEGLContextFactory == null) {
            mEGLContextFactory = new DefaultContextFactory();
        }
        if (mEGLWindowSurfaceFactory == null) {
            mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        mGLThread = new GLThread(renderer);
        mGLThread.start();
    }

    /**
     * Install a custom EGLContextFactory.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If this method is not called, then by default
     * a context will be created with no shared context and
     * with a null attribute list.
     */
    public void setEGLContextFactory(EGLContextFactory factory) {
        checkRenderThreadState();
        mEGLContextFactory = factory;
    }

    /**
     * Install a custom EGLWindowSurfaceFactory.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If this method is not called, then by default
     * a window surface will be created with a null attribute list.
     */
    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        mEGLWindowSurfaceFactory = factory;
    }

    /**
     * Install a custom EGLConfigChooser.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose a config as close to 16-bit RGB as possible, with
     * a depth buffer as close to 16 bits as possible.
     *
     * @param configChooser
     */
    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        checkRenderThreadState();
        mEGLConfigChooser = configChooser;
    }

    /**
     * Install a config chooser which will choose a config
     * as close to 16-bit RGB as possible, with or without an optional depth
     * buffer as close to 16-bits as possible.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose a config as close to 16-bit RGB as possible, with
     * a depth buffer as close to 16 bits as possible.
     *
     * @param needDepth
     */
    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
    }

    /**
     * Install a config chooser which will choose a config
     * with at least the specified component sizes, and as close
     * to the specified component sizes as possible.
     * <p>If this method is
     * called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose a config as close to 16-bit RGB as possible, with
     * a depth buffer as close to 16 bits as possible.
     */
    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize,
                                    int alphaSize, int depthSize, int stencilSize) {
        setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize,
                blueSize, alphaSize, depthSize, stencilSize));
    }

    /**
     * Inform the default EGLContextFactory and default EGLConfigChooser
     * which EGLContext client version to pick.
     * <p>Use this method to create an OpenGL ES 2.0-compatible context.
     * Example:
     * <pre class="prettyprint">
     *     public MyView(Context context) {
     *         super(context);
     *         setEGLContextClientVersion(2); // Pick an OpenGL ES 2.0 context.
     *         setRenderer(new MyRenderer());
     *     }
     * </pre>
     * <p>Note: Activities which require OpenGL ES 2.0 should indicate this by
     * setting @lt;uses-feature android:glEsVersion="0x00020000" /> in the activity's
     * AndroidManifest.xml file.
     * <p>If this method is called, it must be called before {@link #setRenderer(Renderer)}
     * is called.
     * <p>This method only affects the behavior of the default EGLContexFactory and the
     * default EGLConfigChooser. If
     * {@link #setEGLContextFactory(EGLContextFactory)} has been called, then the supplied
     * EGLContextFactory is responsible for creating an OpenGL ES 2.0-compatible context.
     * If
     * {@link #setEGLConfigChooser(EGLConfigChooser)} has been called, then the supplied
     * EGLConfigChooser is responsible for choosing an OpenGL ES 2.0-compatible config.
     *
     * @param version The EGLContext client version to choose. Use 2 for OpenGL ES 2.0
     */
    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        mEGLContextClientVersion = version;
    }

    /**
     * Get the current rendering mode. May be called
     * from any thread. Must not be called before a renderer has been set.
     *
     * @return the current rendering mode.
     * @see #RENDERMODE_CONTINUOUSLY
     * @see #RENDERMODE_WHEN_DIRTY
     */
    public int getRenderMode() {
        return mGLThread.getRenderMode();
    }

    /**
     * Set the rendering mode. When renderMode is
     * RENDERMODE_CONTINUOUSLY, the renderer is called
     * repeatedly to re-render the scene. When renderMode
     * is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface
     * is created, or when {@link #requestRender} is called. Defaults to RENDERMODE_CONTINUOUSLY.
     * <p>
     * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance
     * by allowing the GPU and CPU to idle when the view does not need to be updated.
     * <p>
     * This method can only be called after {@link #setRenderer(Renderer)}
     *
     * @param renderMode one of the RENDERMODE_X constants
     * @see #RENDERMODE_CONTINUOUSLY
     * @see #RENDERMODE_WHEN_DIRTY
     */
    public void setRenderMode(int renderMode) {
        mGLThread.setRenderMode(renderMode);
    }

    /**
     * Request that the renderer render a frame.
     * This method is typically used when the render mode has been set to
     * {@link #RENDERMODE_WHEN_DIRTY}, so that frames are only rendered on demand.
     * May be called
     * from any thread. Must not be called before a renderer has been set.
     */
    public void requestRender() {
        mGLThread.requestRender();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        mGLThread.surfaceCreated();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return
        mGLThread.surfaceDestroyed();
    }

    /**
     * This method is part of the SurfaceHolder.Callback interface, and is
     * not normally called or subclassed by clients of GLSurfaceView.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        mGLThread.onWindowResize(w, h);
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    public void onPause() {
        mGLThread.onPause();
    }

    // ----------------------------------------------------------------------

    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    public void onResume() {
        mGLThread.onResume();
    }

    public void flushTextures(TextureLibrary library) {
        mGLThread.flushTextures(library);
    }

    public void loadTextures(TextureLibrary library) {
        mGLThread.loadTextures(library);
    }

    public void flushBuffers(BufferLibrary library) {
        mGLThread.flushBuffers(library);
    }

    public void loadBuffers(BufferLibrary library) {
        mGLThread.loadBuffers(library);
    }

    public void setSafeMode(boolean safeMode) {
        mGLThread.setSafeMode(safeMode);
    }

    /**
     * Queue a runnable to be run on the GL rendering thread. This can be used
     * to communicate with the Renderer on the rendering thread.
     * Must not be called before a renderer has been set.
     *
     * @param r the runnable to be run on the GL rendering thread.
     */
    public void queueEvent(Runnable r) {
        mGLThread.queueEvent(r);
    }

    /**
     * Inform the view that the window focus has changed.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mGLThread.onWindowFocusChanged(hasFocus);
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of GLSurfaceView.
     * Must not be called before a renderer has been set.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mGLThread.requestExitAndWait();
    }

    private void checkRenderThreadState() {
        if (mGLThread != null) {
            throw new IllegalStateException(
                    "setRenderer has already been called for this instance.");
        }
    }

    /**
     * An interface used to wrap a GL interface.
     * <p>Typically
     * used for implementing debugging and tracing on top of the default
     * GL interface. You would typically use this by creating your own class
     * that implemented all the GL methods by delegating to another GL instance.
     * Then you could add your own behavior before or after calling the
     * delegate. All the GLWrapper would do was instantiate and return the
     * wrapper GL instance:
     * <pre class="prettyprint">
     * class MyGLWrapper implements GLWrapper {
     *     GL wrap(GL gl) {
     *         return new MyGLImplementation(gl);
     *     }
     *     static class MyGLImplementation implements GL,GL10,GL11,... {
     *         ...
     *     }
     * }
     * </pre>
     *
     * @see #setGLWrapper(GLWrapper)
     */
    public interface GLWrapper {
        /**
         * Wraps a gl interface in another gl interface.
         *
         * @param gl a GL interface that is to be wrapped.
         * @return either input argument or another GL object that wraps input argument.
         */
        GL wrap(GL gl);
    }

    /**
     * A generic renderer interface.
     * <p>
     * The renderer is responsible for making OpenGL calls to render a frame.
     * <p>
     * GLSurfaceView clients typically create their own classes that implement
     * this interface, and then call {@link GLSurfaceView#setRenderer} to
     * register the renderer with the GLSurfaceView.
     * <p>
     * <h3>Threading</h3>
     * The renderer will be called on a separate thread, so that rendering
     * performance is decoupled from the UI thread. Clients typically need to
     * communicate with the renderer from the UI thread, because that's where
     * input events are received. Clients can communicate using any of the
     * standard Java techniques for cross-thread communication, or they can
     * use the {@link GLSurfaceView#queueEvent(Runnable)} convenience method.
     * <p>
     * <h3>EGL Context Lost</h3>
     * There are situations where the EGL rendering context will be lost. This
     * typically happens when device wakes up after going to sleep. When
     * the EGL context is lost, all OpenGL resources (such as textures) that are
     * associated with that context will be automatically deleted. In order to
     * keep rendering correctly, a renderer must recreate any lost resources
     * that it still needs. The {@link #onSurfaceCreated(GL10, EGLConfig)} method
     * is a convenient place to do this.
     *
     * @see #setRenderer(Renderer)
     */
    public interface Renderer {
        /**
         * Called when the surface is created or recreated.
         * <p>
         * Called when the rendering thread
         * starts and whenever the EGL context is lost. The context will typically
         * be lost when the Android device awakes after going to sleep.
         * <p>
         * Since this method is called at the beginning of rendering, as well as
         * every time the EGL context is lost, this method is a convenient place to
         * put code to create resources that need to be created when the rendering
         * starts, and that need to be recreated when the EGL context is lost.
         * Textures are an example of a resource that you might want to create
         * here.
         * <p>
         * Note that when the EGL context is lost, all OpenGL resources associated
         * with that context will be automatically deleted. You do not need to call
         * the corresponding "glDelete" methods such as glDeleteTextures to
         * manually delete these lost resources.
         * <p>
         *
         * @param gl     the GL interface. Use <code>instanceof</code> to
         *               test if the interface supports GL11 or higher interfaces.
         * @param config the EGLConfig of the created surface. Can be used
         *               to create matching pbuffers.
         */
        void onSurfaceCreated(GL10 gl, EGLConfig config);

        /**
         * Called when the surface changed size.
         * <p>
         * Called after the surface is created and whenever
         * the OpenGL ES surface size changes.
         * <p>
         * Typically you will set your viewport here. If your camera
         * is fixed then you could also set your projection matrix here:
         * <pre class="prettyprint">
         * void onSurfaceChanged(GL10 gl, int width, int height) {
         *     gl.glViewport(0, 0, width, height);
         *     // for a fixed camera, set the projection too
         *     float ratio = (float) width / height;
         *     gl.glMatrixMode(GL10.GL_PROJECTION);
         *     gl.glLoadIdentity();
         *     gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
         * }
         * </pre>
         *
         * @param gl     the GL interface. Use <code>instanceof</code> to
         *               test if the interface supports GL11 or higher interfaces.
         * @param width
         * @param height
         */
        void onSurfaceChanged(GL10 gl, int width, int height);

        /**
         * Called when the OpenGL context has been lost is about
         * to be recreated.  onSurfaceCreated() will be called after
         * onSurfaceLost().
         */
        void onSurfaceLost();

        /**
         * Called to draw the current frame.
         * <p>
         * This method is responsible for drawing the current frame.
         * <p>
         * The implementation of this method typically looks like this:
         * <pre class="prettyprint">
         * void onDrawFrame(GL10 gl) {
         *     gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         *     //... other gl calls to render the scene ...
         * }
         * </pre>
         *
         * @param gl the GL interface. Use <code>instanceof</code> to
         *           test if the interface supports GL11 or higher interfaces.
         */
        void onDrawFrame(GL10 gl);

        void loadTextures(GL10 gl, TextureLibrary library);

        void flushTextures(GL10 gl, TextureLibrary library);

        void loadBuffers(GL10 gl, BufferLibrary library);

        void flushBuffers(GL10 gl, BufferLibrary library);
    }

    /**
     * An interface for customizing the eglCreateContext and eglDestroyContext calls.
     * <p>
     * This interface must be implemented by clients wishing to call
     * {@link GLSurfaceView#setEGLContextFactory(EGLContextFactory)}
     */
    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);

        void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
    }

    /**
     * An interface for customizing the eglCreateWindowSurface and eglDestroySurface calls.
     * <p>
     * This interface must be implemented by clients wishing to call
     * {@link GLSurfaceView#setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory)}
     */
    public interface EGLWindowSurfaceFactory {
        EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config,
                                       Object nativeWindow);

        void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);
    }

    /**
     * An interface for choosing an EGLConfig configuration from a list of
     * potential configurations.
     * <p>
     * This interface must be implemented by clients wishing to call
     * {@link GLSurfaceView#setEGLConfigChooser(EGLConfigChooser)}
     */
    public interface EGLConfigChooser {
        /**
         * Choose a configuration from the list. Implementors typically
         * implement this method by calling
         * {@link EGL10#eglChooseConfig} and iterating through the results. Please
         * consult the EGL specification available from The Khronos Group to learn
         * how to call eglChooseConfig.
         *
         * @param egl     the EGL10 for the current display.
         * @param display the current display.
         * @return the chosen configuration.
         */
        EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                                              EGLConfig config, Object nativeWindow) {
            return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        }

        public void destroySurface(EGL10 egl, EGLDisplay display,
                                   EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    static class LogWriter extends Writer {

        private final StringBuilder mBuilder = new StringBuilder();

        @Override
        public void close() {
            flushBuilder();
        }

        @Override
        public void flush() {
            flushBuilder();
        }

        @Override
        public void write(char[] buf, int offset, int count) {
            for (int i = 0; i < count; i++) {
                char c = buf[offset + i];
                if (c == '\n') {
                    flushBuilder();
                } else {
                    mBuilder.append(c);
                }
            }
        }

        private void flushBuilder() {
            if (mBuilder.length() > 0) {
                DebugLog.v("GLSurfaceView", mBuilder.toString());
                mBuilder.delete(0, mBuilder.length());
            }
        }
    }

    private static class GLThreadManager {

        private static final int kGLES_20 = 0x20000;
        private boolean mGLESVersionCheckComplete;
        private int mGLESVersion;
        private boolean mGLESDriverCheckComplete;
        private boolean mMultipleGLESContextsAllowed;
        private int mGLContextCount;
        private GLThread mEglOwner;

        public synchronized void threadExiting(GLThread thread) {
            if (LOG_THREADS) {
                DebugLog.i("GLThread", "exiting tid=" + thread.getId());
            }
            thread.mExited = true;
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        /*
         * Tries once to acquire the right to use an EGL
         * surface. Does not block. Requires that we are already
         * in the sGLThreadManager monitor when this is called.
         *
         * @return true if the right to use an EGL surface was acquired.
         */
        public boolean tryAcquireEglSurfaceLocked(GLThread thread) {
            if (mEglOwner == thread || mEglOwner == null) {
                mEglOwner = thread;
                notifyAll();
                return true;
            }
            checkGLESVersion();
            return mMultipleGLESContextsAllowed;
        }

        /*
         * Releases the EGL surface. Requires that we are already in the
         * sGLThreadManager monitor when this is called.
         */
        public void releaseEglSurfaceLocked(GLThread thread) {
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        public synchronized void checkGLDriver(GL10 gl) {
            if (!mGLESDriverCheckComplete) {
                checkGLESVersion();
                if (mGLESVersion < kGLES_20) {
                    String renderer = gl.glGetString(GL10.GL_RENDERER);
                    mMultipleGLESContextsAllowed = false;
                    notifyAll();
                }
                mGLESDriverCheckComplete = true;
            }
        }

        private void checkGLESVersion() {
            if (!mGLESVersionCheckComplete) {
                mGLESVersion = ConfigurationInfo.GL_ES_VERSION_UNDEFINED;
                if (mGLESVersion >= kGLES_20) {
                    mMultipleGLESContextsAllowed = true;
                }
                mGLESVersionCheckComplete = true;
            }
        }
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
                    EGL10.EGL_NONE};

            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
                    mEGLContextClientVersion != 0 ? attrib_list : null);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display,
                                   EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {
        protected int[] mConfigSpec;

        public BaseConfigChooser(int[] configSpec) {
            mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                        EGLConfig[] configs);

        private int[] filterConfigSpec(int[] configSpec) {
            if (mEGLContextClientVersion != 2) {
                return configSpec;
            }
            /** We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
            newConfigSpec[len] = 4; /* EGL_OPENGL_ES2_BIT */
            newConfigSpec[len + 1] = EGL10.EGL_NONE;
            return newConfigSpec;
        }
    }

    private class ComponentSizeChooser extends BaseConfigChooser {
        private final int[] mValue;
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
                                    int alphaSize, int depthSize, int stencilSize) {
            super(new int[]{
                    EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE});
            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mStencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                      EGLConfig[] configs) {
            EGLConfig closestConfig = null;
            int closestDistance = 1000;
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);
                if (d >= mDepthSize && s >= mStencilSize) {
                    int r = findConfigAttrib(egl, display, config,
                            EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config,
                            EGL10.EGL_ALPHA_SIZE, 0);
                    int distance = Math.abs(r - mRedSize)
                            + Math.abs(g - mGreenSize)
                            + Math.abs(b - mBlueSize)
                            + Math.abs(a - mAlphaSize);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestConfig = config;
                    }
                }
            }
            return closestConfig;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                     EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }
    }

    /**
     * This class will choose a supported surface as close to
     * RGB565 as possible, with or without a depth buffer.
     */
    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            super(4, 4, 4, 0, withDepthBuffer ? 16 : 0, 0);
            // Adjust target values. This way we'll accept a 4444 or
            // 555 buffer if there's no 565 buffer available.
            mRedSize = 5;
            mGreenSize = 6;
            mBlueSize = 5;
        }
    }

    /**
     * An EGL helper class.
     */
    private class EglHelper {

        EGL10 mEgl;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        EGLConfig mEglConfig;
        EGLContext mEglContext;

        public EglHelper() {
        }

        /**
         * Initialize EGL for a given configuration spec.
         *
         * @param configSpec
         */
        public void start() {
            /*
             * Get an EGL instance
             */
            mEgl = (EGL10) EGLContext.getEGL();

            /*
             * Get to the default display.
             */
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }

            /*
             * We can now initialize EGL for that display
             */
            int[] version = new int[2];
            if (!mEgl.eglInitialize(mEglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed");
            }
            mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);

            /*
             * Create an OpenGL ES context. This must be done only once, an
             * OpenGL context is a somewhat heavy object.
             */
            mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
            if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
                throwEglException("createContext");
            }
            mEglSurface = null;
        }

        /*
         * React to the creation of a new surface by creating and returning an
         * OpenGL interface that renders to that surface.
         */
        public GL createSurface(SurfaceHolder holder) {
            /*
             * The window size has changed, so we need to create a new
             * surface.
             */
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                /*
                 * Unbind and destroy the old EGL surface, if
                 * there is one.
                 */
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
            }

            /*
             * Create an EGL surface we can render into.
             */
            mEglSurface = mEGLWindowSurfaceFactory.createWindowSurface(mEgl,
                    mEglDisplay, mEglConfig, holder);

            if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
                throwEglException("createWindowSurface");
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */
            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                throwEglException("eglMakeCurrent");
            }

            GL gl = mEglContext.getGL();
            if (mGLWrapper != null) {
                gl = mGLWrapper.wrap(gl);
            }

            if ((mDebugFlags & (DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS)) != 0) {
                int configFlags = 0;
                Writer log = null;
                if ((mDebugFlags & DEBUG_CHECK_GL_ERROR) != 0) {
                    configFlags |= GLDebugHelper.CONFIG_CHECK_GL_ERROR;
                }
                if ((mDebugFlags & DEBUG_LOG_GL_CALLS) != 0) {
                    log = new LogWriter();
                }
                gl = GLDebugHelper.wrap(gl, configFlags, log);
            }
            return gl;
        }

        /**
         * Display the current render surface.
         *
         * @return false if the context has been lost.
         */
        public boolean swap() {
            mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
            /*
             * Always check for EGL_CONTEXT_LOST, which means the context
             * and all associated data were lost (For instance because
             * the device went to sleep). We need to sleep until we
             * get a new surface.
             */
            return mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
        }

        public void destroySurface() {
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
                mEglSurface = null;
            }
        }

        public void finish() {
            if (mEglContext != null) {
                mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
                mEglContext = null;
            }
            if (mEglDisplay != null) {
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throw new RuntimeException(function + " failed: " + mEgl.eglGetError());
        }

        /**
         * Checks to see if the current context is valid.
         **/
        public boolean verifyContext() {
            EGLContext currentContext = mEgl.eglGetCurrentContext();
            return currentContext != EGL10.EGL_NO_CONTEXT && mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
        }
    }

    /**
     * A generic GL Thread. Takes care of initializing EGL and GL. Delegates
     * to a Renderer instance to do the actual drawing. Can be configured to
     * render continuously or on request.
     * <p>
     * All potentially blocking synchronization is done through the
     * sGLThreadManager object. This avoids multiple-lock ordering issues.
     */
    private class GLThread extends Thread {
        private final ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();
        // The solution used here is to simply never render when the window surface
        // does not have the focus.  When the lock screen (or menu) is up, rendering
        // will stop.  This resolves the driver bug (as the egl surface won't be created
        // until after the screen size has been fixed), and is generally good practice
        // since you don't want to be doing a lot of CPU intensive work when the lock
        // screen is up (to preserve battery life).
        private final Renderer mRenderer;
        // Once the thread is started, all accesses to the following member
        // variables are protected by the sGLThreadManager monitor
        private boolean mShouldExit;
        private boolean mExited;
        private boolean mPaused;
        private boolean mHasSurface;
        private boolean mWaitingForSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private int mWidth;
        private int mHeight;
        private int mRenderMode;
        private boolean mRequestRender;
        private boolean mRenderComplete;
        private GL10 mGL;
        private boolean mHasFocus;

        // On some Qualcomm devices (such as the HTC Magic running Android 1.6),
        // there's a bug in the graphics driver that will cause glViewport() to
        // do the wrong thing in a very specific situation.  When the screen is
        // rotated, if a surface is created in one layout (say, portrait view)
        // and then rotated to another, subsequent calls to glViewport are clipped.
        // So, if the window is, say, 320x480 when the surface is created, and
        // then the rotation occurs and glViewport() is called with the new
        // size of 480x320, devices with the buggy driver will clip the viewport
        // to the old width (which means 320x320...ugh!).  This is fixed in
        // Android 2.1 Qualcomm devices (like Nexus One) and doesn't affect
        // non-Qualcomm devices (like the Motorola DROID).
        //
        // Unfortunately, under Android 1.6 this exact case occurs when the
        // screen is put to sleep and then wakes up again.  The lock screen
        // comes up in portrait mode, but at the same time the window surface
        // is also created in the backgrounded game.  When the lock screen is closed
        // and the game comes forward, the window is fixed to the correct size
        // which causes the bug to occur.
        private boolean mSafeMode = false;
        private EglHelper mEglHelper;

        public GLThread(Renderer renderer) {
            super();
            mWidth = 0;
            mHeight = 0;
            mRequestRender = true;
            mRenderMode = RENDERMODE_CONTINUOUSLY;
            mRenderer = renderer;
        }

        @Override
        public void run() {
            setName("GLThread " + getId());
            if (LOG_THREADS) {
                DebugLog.i("GLThread", "starting tid=" + getId());
            }

            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this);
            }
        }

        /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
        private void stopEglLocked() {
            if (mHaveEglSurface) {
                mHaveEglSurface = false;
                mEglHelper.destroySurface();
                sGLThreadManager.releaseEglSurfaceLocked(this);
            }
        }

        private void guardedRun() throws InterruptedException {
            mEglHelper = new EglHelper();
            mHaveEglContext = false;
            mHaveEglSurface = false;
            try {
                GL10 gl = null;
                boolean createEglSurface = false;
                boolean sizeChanged = false;
                boolean wantRenderNotification = false;
                boolean doRenderNotification = false;
                int w = 0;
                int h = 0;
                Runnable event = null;
                int framesSinceResetHack = 0;
                while (true) {
                    synchronized (sGLThreadManager) {
                        while (true) {
                            if (mShouldExit) {
                                return;
                            }

                            if (!mEventQueue.isEmpty()) {
                                event = mEventQueue.remove(0);
                                break;
                            }

                            // Do we need to release the EGL surface?
                            if (mHaveEglSurface && mPaused) {
                                if (LOG_SURFACE) {
                                    DebugLog.i("GLThread", "releasing EGL surface because paused tid=" + getId());
                                }
                                stopEglLocked();
                            }

                            // Have we lost the surface view surface?
                            if ((!mHasSurface) && (!mWaitingForSurface)) {
                                if (LOG_SURFACE) {
                                    DebugLog.i("GLThread", "noticed surfaceView surface lost tid=" + getId());
                                }
                                if (mHaveEglSurface) {
                                    stopEglLocked();
                                }
                                mWaitingForSurface = true;
                                sGLThreadManager.notifyAll();
                            }

                            // Have we acquired the surface view surface?
                            if (mHasSurface && mWaitingForSurface) {
                                if (LOG_SURFACE) {
                                    DebugLog.i("GLThread", "noticed surfaceView surface acquired tid=" + getId());
                                }
                                mWaitingForSurface = false;
                                sGLThreadManager.notifyAll();
                            }

                            if (doRenderNotification) {
                                wantRenderNotification = false;
                                doRenderNotification = false;
                                mRenderComplete = true;
                                sGLThreadManager.notifyAll();
                            }

                            // Ready to draw?
                            if ((!mPaused) && mHasSurface
                                    && (mWidth > 0) && (mHeight > 0)
                                    && (mRequestRender || (mRenderMode == RENDERMODE_CONTINUOUSLY))) {

                                if (mHaveEglContext && !mHaveEglSurface) {
                                    // Let's make sure the context hasn't been lost.
                                    if (!mEglHelper.verifyContext()) {
                                        mEglHelper.finish();
                                        mRenderer.onSurfaceLost();
                                        mHaveEglContext = false;
                                    }
                                }
                                // If we don't have an egl surface, try to acquire one.
                                if ((!mHaveEglContext) && sGLThreadManager.tryAcquireEglSurfaceLocked(this)) {
                                    mHaveEglContext = true;
                                    mEglHelper.start();
                                    sGLThreadManager.notifyAll();
                                }

                                if (mHaveEglContext && !mHaveEglSurface) {
                                    mHaveEglSurface = true;
                                    createEglSurface = true;
                                    sizeChanged = true;
                                }

                                if (mHaveEglSurface) {
                                    if (mSizeChanged) {
                                        sizeChanged = true;
                                        w = mWidth;
                                        h = mHeight;
                                        wantRenderNotification = true;

                                        if (DRAW_TWICE_AFTER_SIZE_CHANGED) {
                                            // We keep mRequestRender true so that we draw twice
                                            // after size changes. (Once because of mSizeChanged,
                                            // the second time because of mRequestRender.)
                                            // This forces the updated graphics onto the screen.
                                        } else {
                                            mRequestRender = false;
                                        }
                                        mSizeChanged = false;
                                    } else {
                                        mRequestRender = false;
                                    }
                                    sGLThreadManager.notifyAll();
                                    break;
                                }
                            }

                            // By design, this is the only place in a GLThread thread where we wait().
                            if (LOG_THREADS) {
                                DebugLog.i("GLThread", "waiting tid=" + getId());
                            }
                            sGLThreadManager.wait();
                        }
                    } // end of synchronized(sGLThreadManager)

                    if (event != null) {
                        event.run();
                        event = null;
                        continue;
                    }

                    if (mHasFocus) {
                        if (createEglSurface) {
                            gl = (GL10) mEglHelper.createSurface(getHolder());
                            sGLThreadManager.checkGLDriver(gl);
                            if (LOG_RENDERER) {
                                DebugLog.w("GLThread", "onSurfaceCreated");
                            }
                            mGL = gl;
                            mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
                            createEglSurface = false;
                            framesSinceResetHack = 0;
                        }

                        if (sizeChanged) {
                            if (LOG_RENDERER) {
                                DebugLog.w("GLThread", "onSurfaceChanged(" + w + ", " + h + ")");
                            }
                            mRenderer.onSurfaceChanged(gl, w, h);
                            sizeChanged = false;
                        }

                        if (LOG_RENDERER) {
                            DebugLog.w("GLThread", "onDrawFrame");
                        }

                        // Some phones (Motorola Cliq, Backflip; also the
                        // Huawei Pulse, and maybe the Samsung Behold II), use a
                        // broken graphics driver from Qualcomm.  It fails in a
                        // very specific case: when the EGL context is lost due to
                        // resource constraints, and then recreated, if GL commands
                        // are sent within two frames of the surface being created
                        // then eglSwapBuffers() will hang.  Normally, applications
                        // using the stock GLSurfaceView never run into this problem
                        // because it discards the EGL context explicitly on every
                        // pause.  But I've modified this class to not do that--I only
                        // want to reload textures when the context is actually lost--so
                        // this bug revealed itself as black screens on devices like the
                        // Cliq. Thus, in "safe mode," I force two swaps to occur before
                        // issuing any GL commands.  Don't ask me how long it took
                        // to figure this out.
                        if (framesSinceResetHack > 1 || !mSafeMode) {
                            mRenderer.onDrawFrame(gl);
                        } else {
                            DebugLog.w("GLThread", "Safe Mode Wait...");
                        }

                        framesSinceResetHack++;

                        if (!mEglHelper.swap()) {
                            if (LOG_SURFACE) {
                                DebugLog.i("GLThread", "egl surface lost tid=" + getId());
                            }
                            stopEglLocked();
                        }
                    }
                    if (wantRenderNotification) {
                        doRenderNotification = true;
                    }
                }
            } finally {
                mGL = null;
                /*
                 * clean-up everything...
                 */
                synchronized (sGLThreadManager) {
                    stopEglLocked();
                    mEglHelper.finish();
                }
            }
        }

        public int getRenderMode() {
            synchronized (sGLThreadManager) {
                return mRenderMode;
            }
        }

        public void setRenderMode(int renderMode) {
            if (!((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY))) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized (sGLThreadManager) {
                mRenderMode = renderMode;
                sGLThreadManager.notifyAll();
            }
        }

        public void requestRender() {
            synchronized (sGLThreadManager) {
                mRequestRender = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void surfaceCreated() {
            synchronized (sGLThreadManager) {
                if (LOG_THREADS) {
                    DebugLog.i("GLThread", "surfaceCreated tid=" + getId());
                }
                mHasSurface = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void surfaceDestroyed() {
            synchronized (sGLThreadManager) {
                if (LOG_THREADS) {
                    DebugLog.i("GLThread", "surfaceDestroyed tid=" + getId());
                }
                mHasSurface = false;
                sGLThreadManager.notifyAll();
                while ((!mWaitingForSurface) && (!mExited)) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (sGLThreadManager) {
                mPaused = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void onResume() {
            synchronized (sGLThreadManager) {
                mPaused = false;
                mRequestRender = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (sGLThreadManager) {
                mWidth = w;
                mHeight = h;
                mSizeChanged = true;
                mRequestRender = true;
                mRenderComplete = false;
                sGLThreadManager.notifyAll();

                // Wait for thread to react to resize and render a frame
                while (!mExited && !mPaused && !mRenderComplete) {
                    if (LOG_SURFACE) {
                        DebugLog.i("Main thread", "onWindowResize waiting for render complete.");
                    }
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void loadTextures(TextureLibrary library) {
            synchronized (this) {
                assert mGL != null;
                if (mGL != null && mHasSurface) {
                    mRenderer.loadTextures(mGL, library);
                }
            }
        }

        public void flushTextures(TextureLibrary library) {
            synchronized (this) {
                assert mGL != null;
                if (mGL != null) {
                    mRenderer.flushTextures(mGL, library);
                }
            }
        }

        public void loadBuffers(BufferLibrary library) {
            synchronized (this) {
                assert mGL != null;
                if (mGL != null) {
                    mRenderer.loadBuffers(mGL, library);
                }
            }
        }

        public void flushBuffers(BufferLibrary library) {
            synchronized (this) {
                assert mGL != null;
                if (mGL != null) {
                    mRenderer.flushBuffers(mGL, library);
                }
            }
        }

        public void onWindowFocusChanged(boolean hasFocus) {
            synchronized (sGLThreadManager) {
                mHasFocus = hasFocus;
                sGLThreadManager.notifyAll();
            }
            if (LOG_SURFACE) {
                DebugLog.i("Main thread", "Focus " + (mHasFocus ? "gained" : "lost"));
            }
        }

        public void requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            synchronized (sGLThreadManager) {
                mShouldExit = true;
                sGLThreadManager.notifyAll();
                while (!mExited) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // End of member variables protected by the sGLThreadManager monitor.

        /**
         * Queue an "event" to be run on the GL rendering thread.
         *
         * @param r the runnable to be run on the GL rendering thread.
         */
        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized (sGLThreadManager) {
                mEventQueue.add(r);
                sGLThreadManager.notifyAll();
            }
        }

        public void setSafeMode(boolean on) {
            mSafeMode = on;
        }
    }
}
