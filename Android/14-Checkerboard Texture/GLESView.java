package com.window.procedural.neeraj;

import android.content.Context;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES32;
import android.opengl.GLES20;
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

    static final int checkImageWidth = 64;
    static final int checkImageHeight = 64;

    private int[] vao_quad = new int[1];
    private int[] vbo_position = new int[1];
    private int[] vbo_texture = new int[1];
    
    private int mvpUniform;
    private int texture0_sampler_uniform;

    private int[] texture = new int[1];

    private float perspectiveProjectionMatrix[] = new float[16];
    private float quadVertices[] = new float[12];
    private float quadVertices2[] = new float[12];

    private byte[][][] checkImage = new byte[checkImageWidth][checkImageHeight][4];
    private byte[] array_buffer = new byte[16384];
	private int n = 0;

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
         "uniform mat4 u_mvp_matrix;" +
         "out vec2 out_texture0_coord;" +
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

        
        //  **************************** DATA ****************************

        //  Giving pyramid vertices
        /*
        final float quadVertices[] = new float[]
        {
            -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f
        };
        */

        final float quadTexCoords[] = new float[]
        {
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        };        


        //  **************************** VAO QUAD ****************************
        GLES32.glGenVertexArrays(1, vao_quad, 0);
        GLES32.glBindVertexArray(vao_quad[0]);

        //  Binding VBO-POSITION
        GLES32.glGenBuffers(1, vbo_position, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_position[0]);
        
        /*
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(quadVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(quadVertices);
        verticesBuffer.position(0);
        */

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, 48, null, GLES32.GL_DYNAMIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_VERTEX, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_VERTEX);
        //  Unbind VBO-POSITION
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
       
       //  Bind VBO - color
        GLES32.glGenBuffers(1, vbo_texture, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_texture[0]);
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(quadTexCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer colorBuffer = byteBuffer.asFloatBuffer();
        colorBuffer.put(quadTexCoords);
        colorBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, quadTexCoords.length * 4, colorBuffer, GLES32.GL_STATIC_DRAW);
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
        //GLES32.glEnable(GLES32.GL_CULL_FACE);
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        LoadProceduralTexture();

        Matrix.setIdentityM(perspectiveProjectionMatrix, 0);
    }
    
    private void resize(int width, int height)
    {
        GLES32.glViewport(0, 0, width, height);
        
        Matrix.perspectiveM(perspectiveProjectionMatrix, 0, 45.0f, (float)width/(float)height, 0.1f, 100.0f);
    }

    
    /*
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
    */
    
    private void LoadProceduralTexture()
    {

    	ByteBuffer byteArrayBuffer;
        MakeCheckImage();
        arrayGen();

        byteArrayBuffer = ByteBuffer.allocateDirect(array_buffer.length * 4);
		byteArrayBuffer.order(ByteOrder.nativeOrder());
		byteArrayBuffer.put(array_buffer);
		byteArrayBuffer.position(0);

        GLES32.glPixelStorei(GLES32.GL_UNPACK_ALIGNMENT, 1);
        GLES32.glGenTextures(1, texture, 0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture[0]);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_REPEAT);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_REPEAT);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);

        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA, checkImageWidth, checkImageHeight, 0, GLES32.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteArrayBuffer);

        //GLES10.glTexEnvf(GLES10.GL_TEXTURE_ENV, GLES10.GL_TEXTURE_ENV_MODE, GLES10.GL_REPLACE);
    }

    private void MakeCheckImage()
    {
        int i, j, c = 0;

        boolean firstop, secondop;

        for (i = 0; i < checkImageHeight; i++)
        {
            for (j = 0; j < checkImageWidth; j++)
            {

            	firstop = ((i & 0x8) == 0);
            	secondop = ((j & 0x8) == 0);

            	int first = firstop ? 0 : 1;
				int second = secondop ? 0 : 1;

                c = (first ^ second) * 255;

                checkImage[i][j][0] = (byte)c;
                checkImage[i][j][1] = (byte)c;
                checkImage[i][j][2] = (byte)c;
                checkImage[i][j][3] = (byte)255;

            }
        }
    }

    public void arrayGen()
	{
		System.out.println("OGLES: In arrayGen");
		for(int i = 0; i < 64; i++)
		{
			for(int j = 0; j < 64; j++)
			{
				for(int k = 0; k< 4; k++)
				{
					array_buffer[n++] = checkImage[i][j][k];
				}
			//n = n + 1;
			}
		}	
	}

    public void display()
    {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        //  use shader object
        GLES32.glUseProgram(shaderProgramObject);

        float modelViewMatrix[] = new float[16];
        float modelViewProjectionMatrix[] = new float[16];
        //float rotationMatrix[] = new float[16];

        //  **************************** DRAW TRIANGLE ****************************
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);
        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, -5.0f);
       // Matrix.scaleM(modelViewMatrix, 0, 0.5f, 0.5f, 0.5f);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, perspectiveProjectionMatrix, 0, modelViewMatrix, 0);
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vao_quad[0]);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture[0]);
        GLES32.glUniform1i(texture0_sampler_uniform, 0);

        quadVertices[0] = 0.0f;
        quadVertices[1] = 1.0f;
        quadVertices[2] = 0.0f;
        quadVertices[3] = -2.0f;
        quadVertices[4] = 1.0f;
        quadVertices[5] = 0.0f;
        quadVertices[6] = -2.0f;
        quadVertices[7] = -1.0f;
        quadVertices[8] = 0.0f;
        quadVertices[9] = 0.0f;
        quadVertices[10] = -1.0f;
        quadVertices[11] = 0.0f;

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_position[0]);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(quadVertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(quadVertices);
        verticesBuffer.position(0);  

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, quadVertices.length * 4, verticesBuffer, GLES32.GL_DYNAMIC_DRAW);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        
        GLES32.glBindVertexArray(0);

        //----------------------------------------------------------------------------------------
        
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);
        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, -5.0f);
       // Matrix.scaleM(modelViewMatrix, 0, 0.5f, 0.5f, 0.5f);

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, perspectiveProjectionMatrix, 0, modelViewMatrix, 0);
        GLES32.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES32.glBindVertexArray(vao_quad[0]);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texture[0]);
        GLES32.glUniform1i(texture0_sampler_uniform, 0);

        quadVertices2[0] = 2.41421f;
        quadVertices2[1] = 1.0f;
        quadVertices2[2] = -1.41421f;
        quadVertices2[3] = 1.0f;
        quadVertices2[4] = 1.0f;
        quadVertices2[5] = 0.0f;
        quadVertices2[6] = 1.0f;
        quadVertices2[7] = -1.0f;
        quadVertices2[8] = 0.0f;
        quadVertices2[9] = 2.41421f;
        quadVertices2[10] = -1.0f;
        quadVertices2[11] = -1.41421f;

        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_position[0]);
        byteBuffer = ByteBuffer.allocateDirect(quadVertices2.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(quadVertices2);
        verticesBuffer.position(0);  
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, quadVertices2.length * 4, verticesBuffer, GLES32.GL_DYNAMIC_DRAW);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        
        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 4);
        
        GLES32.glBindVertexArray(0);

        //  unuse shader object
        GLES32.glUseProgram(0);

        requestRender();
    }
    
    void uninitialize()
    {

        if(vao_quad[0] != 0)
        {
            GLES32.glDeleteVertexArrays(1, vao_quad, 0);
            vao_quad[0] = 0;
        }
        
        if(vbo_position[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_position, 0);
            vbo_position[0] = 0;
        }

        if(vbo_texture[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_texture, 0);
            vbo_texture[0] = 0;
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
