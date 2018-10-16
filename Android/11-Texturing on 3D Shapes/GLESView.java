package com.window.texturing.neeraj;

import android.content.Context;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES32;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.Matrix;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener
{
    private final Context context;
    private GestureDetector gestureDetector;

    //  shader variables
    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;
    
    private int[] vao_pyramid = new int[1];
    private int[] vao_cube = new int[1];
    private int[] vbo_position = new int[1];
    private int[] vbo_pyramid_texture = new int[1];
    private int[] vbo_cube_texture = new int[1];
    
    private int mvpUniform;
    private int texture0_sampler_uniform;

    private int[] texture_kundali = new int[1];
    private int[] texture_stone = new int[1];

    private float perspectiveProjectionMatrix[] = new float[16];
    
    public float angle_pyramid = 0.0f;
    public float angle_cube = 0.0f;

    public GLESView(Context drawingContext)
    {
        super(drawingContext);
        context = drawingContext;

        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        gestureDetector = new GestureDetector(context, this, null, false);
        gestureDetector.setOnDoubleTapListener(this);
    }
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        //  obtaining OpenGL-ES version
        String glesVersion = gl.glGetString(GL10.GL_VERSION);
        System.out.println("NRC: OpenGL-ES Version = " + glesVersion);

        //  obtaining OpenGL Shading Language version
        String glslVersion = gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
        System.out.println("NRC: GLSL Version = " + glslVersion);

        initialize(gl);
    }
 
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        resize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        display();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        int eventaction = e.getAction();
        if(!gestureDetector.onTouchEvent(e))
            super.onTouchEvent(e);
        return(true);
    }
    
    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        return(true);
    }
    

    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        return(true);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        return(true);
    }
    
    @Override
    public boolean onDown(MotionEvent e)
    {
        return(true);
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return(true);
    }
    
    @Override
    public void onLongPress(MotionEvent e)
    {
    
    }
    
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        uninitialize();
        System.exit(0);
        return(true);
    }
    
    @Override
    public void onShowPress(MotionEvent e)
    {

    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return(true);
    }

    private void initialize(GL10 gl)
    {

        //  ********************** VERTEX SHADER **********************
        vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        //  Vertex Shader code:
        final String vertexShaderSourceCode = String.format
        (
         "#version 320 es" +
         "\n" +
         "in vec4 vPosition;" +
         "in vec2 vTexture0_Coord;" +
         "out vec2 out_texture0_coord;" +
         "uniform mat4 u_mvp_matrix;" +
         "void main(void)" +
         "{" +
         "gl_Position = u_mvp_matrix * vPosition;" +
         "out_texture0_coord = vTexture0_Coord;" +
         "}"
        );
        
        GLES32.glShaderSource(vertexShaderObject,vertexShaderSourceCode);
        
        //  Vertex Shader compilation and error checking
        GLES32.glCompileShader(vertexShaderObject);
        int[] iShaderCompiledStatus = new int[1];
        int[] iInfoLogLength = new int[1];
        String szInfoLog = null;
        GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(vertexShaderObject);
                System.out.println("NRC: Vertex Shader Compilation Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
           }
        }

        //  ********************** FRAGMENT SHADER **********************
        fragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        
        //  Fragment Shader code:
        final String fragmentShaderSourceCode = String.format
        (
         "#version 320 es" +
         "\n" +
         "precision highp float;" +
         "in vec2 out_texture0_coord;"	+
         "uniform highp sampler2D u_texture0_sampler;"	+
         "out vec4 FragColor;" +
         "void main(void)" +
         "{" +
         "FragColor = texture(u_texture0_sampler, out_texture0_coord);" +
         "}"
        );
        
        GLES32.glShaderSource(fragmentShaderObject,fragmentShaderSourceCode);
        
        //  Fragment Shader compilation and error checking
        GLES32.glCompileShader(fragmentShaderObject);
        iShaderCompiledStatus[0] = 0;
        iInfoLogLength[0] = 0;
        szInfoLog = null;
        GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(fragmentShaderObject);
                System.out.println("NRC: Fragment Shader Compilation Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }
        
        //  ********************** SHADER PROGRAM OBJECT **********************
        shaderProgramObject = GLES32.glCreateProgram();
        
        GLES32.glAttachShader(shaderProgramObject, vertexShaderObject);
        GLES32.glAttachShader(shaderProgramObject, fragmentShaderObject);
        
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.NRC_ATTRIBUTE_TEXTURE0, "vTexture0_Coord");
        
        GLES32.glLinkProgram(shaderProgramObject);

        //  Shader linking and error checking
        int[] iShaderProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfoLog = null;
        GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
        if (iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetProgramInfoLog(shaderProgramObject);
                System.out.println("NRC: Shader Program Link Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }

        mvpUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_mvp_matrix");
        texture0_sampler_uniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_texture0_sampler");

        //	Load textures:
        texture_stone[0] = loadGLTexture(R.raw.stone);
        texture_kundali[0] = loadGLTexture(R.raw.vijay_kundali_horz_inverted);
        
        //  **************************** DATA ****************************

        //  Giving pyramid vertices
        final float pyramidVertices[] = new float[]
        {
            0, 1, 0,    // front-top
            -1, -1, 1,  // front-left
            1, -1, 1,   // front-right
            
            0, 1, 0,    // right-top
            1, -1, 1,   // right-left
            1, -1, -1,  // right-right
            
            0, 1, 0,    // back-top
            1, -1, -1,  // back-left
            -1, -1, -1, // back-right
            
            0, 1, 0,    // left-top
            -1, -1, -1, // left-left
            -1, -1, 1   // left-right
        };

        //  Giving triangle color values
        final float pyramidTexCoords[] = new float[]
        {
            0.5f, 1.0f, // front-top
            0.0f, 0.0f, // front-left
            1.0f, 0.0f, // front-right
            
            0.5f, 1.0f, // right-top
            1.0f, 0.0f, // right-left
            0.0f, 0.0f, // right-right
            
            0.5f, 1.0f, // back-top
            1.0f, 0.0f, // back-left
            0.0f, 0.0f, // back-right
            
            0.5f, 1.0f, // left-top
            0.0f, 0.0f, // left-left
            1.0f, 0.0f, // left-right
        };

        //  Giving square vertices
        final float cubeVertices[] = new float[]
        {
           // top surface
            1.0f, 1.0f,-1.0f,  // top-right of top
            -1.0f, 1.0f,-1.0f, // top-left of top
            -1.0f, 1.0f, 1.0f, // bottom-left of top
            1.0f, 1.0f, 1.0f,  // bottom-right of top
            
            // bottom surface
            1.0f,-1.0f, 1.0f,  // top-right of bottom
            -1.0f,-1.0f, 1.0f, // top-left of bottom
            -1.0f,-1.0f,-1.0f, // bottom-left of bottom
            1.0f,-1.0f,-1.0f,  // bottom-right of bottom
            
            // front surface
            1.0f, 1.0f, 1.0f,  // top-right of front
            -1.0f, 1.0f, 1.0f, // top-left of front
            -1.0f,-1.0f, 1.0f, // bottom-left of front
            1.0f,-1.0f, 1.0f,  // bottom-right of front
            
            // back surface
            1.0f,-1.0f,-1.0f,  // top-right of back
            -1.0f,-1.0f,-1.0f, // top-left of back
            -1.0f, 1.0f,-1.0f, // bottom-left of back
            1.0f, 1.0f,-1.0f,  // bottom-right of back
            
            // left surface
            -1.0f, 1.0f, 1.0f, // top-right of left
            -1.0f, 1.0f,-1.0f, // top-left of left
            -1.0f,-1.0f,-1.0f, // bottom-left of left
            -1.0f,-1.0f, 1.0f, // bottom-right of left
            
            // right surface
            1.0f, 1.0f,-1.0f,  // top-right of right
            1.0f, 1.0f, 1.0f,  // top-left of right
            1.0f,-1.0f, 1.0f,  // bottom-left of right
            1.0f,-1.0f,-1.0f,  // bottom-right of right
        };

        final float cubeTexCoords[] = new float[]
        {
        	0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
            
            0.0f,0.0f,
            1.0f,0.0f,
            1.0f,1.0f,
            0.0f,1.0f,
        };

        //  **************************** VAO PYRAMID ****************************
        GLES32.glGenVertexArrays(1, vao_pyramid, 0);
        GLES32.glBindVertexArray(vao_pyramid[0]);

        //  Binding VBO-POSITION
        GLES32.glGenBuffers(1, vbo_position, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_position[0]);
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(pyramidVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(pyramidVertices);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, pyramidVertices.length * 4, verticesBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_VERTEX, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_VERTEX);
        //  Unbind VBO-POSITION
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
       
       //  Bind VBO - color
        GLES32.glGenBuffers(1, vbo_pyramid_texture, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_pyramid_texture[0]);
        
        byteBuffer = ByteBuffer.allocateDirect(pyramidTexCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer colorBuffer = byteBuffer.asFloatBuffer();
        colorBuffer.put(pyramidTexCoords);
        colorBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, pyramidTexCoords.length * 4, colorBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_TEXTURE0, 2, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_TEXTURE0);
        //  Unbind VBO - color
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        
        //  Unbind VAO
        GLES32.glBindVertexArray(0);

         //  **************************** VAO CUBE ****************************
        GLES32.glGenVertexArrays(1, vao_cube, 0);
        GLES32.glBindVertexArray(vao_cube[0]);

        //  Binding VBO-POSITION
        GLES32.glGenBuffers(1, vbo_position, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_position[0]);
        
        byteBuffer = ByteBuffer.allocateDirect(cubeVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(cubeVertices);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, cubeVertices.length * 4, verticesBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_VERTEX, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_VERTEX);
        //  Unbind VBO-POSITION
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

        //  for square's color:
        //  Bind VBO - color
        GLES32.glGenBuffers(1, vbo_cube_texture, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_cube_texture[0]);
        
        byteBuffer = ByteBuffer.allocateDirect(cubeTexCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        colorBuffer = byteBuffer.asFloatBuffer();
        colorBuffer.put(cubeTexCoords);
        colorBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, cubeTexCoords.length * 4, colorBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_TEXTURE0, 2, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_TEXTURE0);
        //  Unbind VBO - color
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

        //  Unbind VAO
        GLES32.glBindVertexArray(0);

        //  **********************************************************************

        //  various initializations
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        Matrix.setIdentityM(perspectiveProjectionMatrix, 0);
    }
    
    private void resize(int width, int height)
    {
        GLES32.glViewport(0, 0, width, height);
        
        Matrix.perspectiveM(perspectiveProjectionMatrix, 0, 45.0f, (float)width/(float)height, 0.1f, 100.0f);
    }

    private void update()
    {
        angle_pyramid = angle_pyramid + 0.5f;
	    if (angle_pyramid >= 360.0f)
		    angle_pyramid = 0.0f;

	    angle_cube = angle_cube - 0.5f;
	    if (angle_cube <= -360.0f)
		    angle_cube = 0.0f;
    }
    
    private int loadGLTexture(int imageFileResourceID)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageFileResourceID, options);
        
        int[] texture = new int[1];
       
        GLES32.glGenTextures(1, texture, 0);
        GLES32.glPixelStorei(GLES32.GL_UNPACK_ALIGNMENT, 1);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture[0]);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR_MIPMAP_LINEAR);
        
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D);
        
        return(texture[0]);
    }

    public void display()
    {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        //  use shader object
        GLES32.glUseProgram(shaderProgramObject);

        float modelViewMatrix[] = new float[16];
        float modelViewProjectionMatrix[] = new float[16];
        float rotationMatrix[] = new float[16];

        //  **************************** DRAW TRIANGLE ****************************
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);
        Matrix.translateM(modelViewMatrix, 0, -1.0f, 0.0f, -3.0f);
        Matrix.rotateM(modelViewMatrix, 0, angle_pyramid, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(modelViewMatrix, 0, 0.5f, 0.5f, 0.5f);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, perspectiveProjectionMatrix, 0, modelViewMatrix, 0);
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);
        
        GLES32.glBindVertexArray(vao_pyramid[0]);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture_stone[0]);
        GLES32.glUniform1i(texture0_sampler_uniform, 0);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 12);
        GLES32.glBindVertexArray(0);
        
        //  **************************** DRAW SQUARE ****************************
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.translateM(modelViewMatrix, 0, 1.0f, 0.0f, -3.0f);
        Matrix.rotateM(modelViewMatrix, 0, angle_cube, 1.0f, 1.0f, 1.0f);
        Matrix.scaleM(modelViewMatrix, 0, 0.5f, 0.5f, 0.5f);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, perspectiveProjectionMatrix, 0, modelViewMatrix, 0);
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);
        
        GLES32.glBindVertexArray(vao_cube[0]);
        //GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture_kundali[0]);
        GLES32.glUniform1i(texture0_sampler_uniform, 0);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 4, 4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 8, 4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 12, 4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 16, 4);
		GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 20, 4);
        GLES32.glBindVertexArray(0);

        //  unuse shader object
        GLES32.glUseProgram(0);

        update();
        requestRender();
    }
    
    void uninitialize()
    {

        if(vao_pyramid[0] != 0)
        {
            GLES32.glDeleteVertexArrays(1, vao_pyramid, 0);
            vao_pyramid[0] = 0;
        }

        if(vao_cube[0] != 0)
        {
            GLES32.glDeleteVertexArrays(1, vao_cube, 0);
            vao_cube[0] = 0;
        }
        
        if(vbo_position[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_position, 0);
            vbo_position[0] = 0;
        }

        if(vbo_cube_texture[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_cube_texture, 0);
            vbo_cube_texture[0] = 0;
        }

        if(vbo_pyramid_texture[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_pyramid_texture, 0);
            vbo_pyramid_texture[0] = 0;
        }


        if(shaderProgramObject != 0)
        {
            if(vertexShaderObject != 0)
            {
                GLES32.glDetachShader(shaderProgramObject, vertexShaderObject);

                GLES32.glDeleteShader(vertexShaderObject);
                vertexShaderObject = 0;
            }
            
            if(fragmentShaderObject != 0)
            {
                GLES32.glDetachShader(shaderProgramObject, fragmentShaderObject);

                GLES32.glDeleteShader(fragmentShaderObject);
                fragmentShaderObject = 0;
            }
        }

        if(shaderProgramObject != 0)
        {
            GLES32.glDeleteProgram(shaderProgramObject);
            shaderProgramObject = 0;
        }
    }
}
