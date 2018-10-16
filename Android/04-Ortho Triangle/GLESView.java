package com.example.neeraj5.window;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLES31;				//GLES 3.1 * 100 = GLES31

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.Matrix;

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener
{

	private final Context context;
	private GestureDetector gestureDetector;

	private int vertexShaderObject;
	private int fragmentShaderObject;
	private int shaderProgramObject;

	private int[] vao = new int[1];
	private int[] vbo = new int[1];
	private int mvpUniform;

	private float orthographicProjectionMatrix[] = new float[16];

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

		String glesVersion = gl.glGetString(GL10.GL_VERSION);
		System.out.println("NRC: OpenGL-ES Version: " + glesVersion);

		String glslVersion = gl.glGetString(GLES31.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("NRC: GLSL Version: " + glslVersion);

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

    	//	************** VERTEX SHADER *********************************
    	vertexShaderObject = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);

    	final String vertexShaderSourceCode = String.format
    	(
    		"#version 310 es" + 
    		"\n" +
    		"in vec4 vPosition;" +
    		"uniform mat4 u_mvp_matrix;" +
    		"void main(void)" +
    		"{" +
    		"gl_Position = u_mvp_matrix * vPosition;" +
    		"}"
    	);

    	GLES31.glShaderSource(vertexShaderObject, vertexShaderSourceCode);
    	GLES31.glCompileShader(vertexShaderObject);
    	//	error checking
    	int[] iShaderCompiledStatus = new int[1];
    	int[] iInfoLogLength = new int[1];
    	String szInfoLog = null;
    	GLES31.glGetShaderiv(vertexShaderObject, GLES31.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
    	if(iShaderCompiledStatus[0] == GLES31.GL_FALSE)
    	{
    		GLES31.glGetShaderiv(vertexShaderObject, GLES31.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
    		if(iInfoLogLength[0] > 0)
    		{
    			szInfoLog = GLES31.glGetShaderInfoLog(vertexShaderObject);
    			System.out.println("NRC: Vertex Shader Compilation Log: " + szInfoLog);
    			uninitialize();
    			System.exit(0);
    		}
    	}

    	//	************** FRAGMENT SHADER *********************************
    	fragmentShaderObject = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);

    	final String fragmentShaderSourceCode = String.format
    	(
    		"version 310 es" +
    		"\n" +
    		"precision highp float;" +
    		"out vec4 FragColor;" +
    		"void main(void)" +
    		"{" +
    		"FragColor = vec4(1.0, 1.0, 1.0, 1.0);" +
    		"}"
    	);

    	GLES31.glShaderSource(fragmentShaderObject, fragmentShaderSourceCode);
    	GLES31.glCompileShader(fragmentShaderObject);
    	//	error checking
    	iShaderCompiledStatus[0] = 0;
    	iInfoLogLength[0] = 0;
    	szInfoLog = null;
    	GLES31.glGetShaderiv(fragmentShaderObject, GLES31.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
    	if(iShaderCompiledStatus[0] == GLES31.GL_FALSE)
    	{
    		GLES31.glGetShaderiv(fragmentShaderObject, GLES31.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
    		if(iInfoLogLength[0] > 0)
    		{
    			szInfoLog = GLES31.glGetShaderInfoLog(fragmentShaderObject);
                System.out.println("NRC: Fragment Shader Compilation Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
    		}
    	}

    	//	***************** SHADER PROGRAM ********************
    	shaderProgramObject = GLES31.glCreateProgram();

    	GLES31.glAttachShader(shaderProgramObject, vertexShaderObject);
    	GLES31.glAttachShader(shaderProgramObject, fragmentShaderObject);

    	GLES31.glBindAttribLocation(shaderProgramObject, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");

    	GLES31.glLinkProgram(shaderProgramObject);
    	int[] iShaderProgramLinkStatus = new int[1];
    	iInfoLogLength[0] = 0;
    	szInfoLog = null;
    	GLES31.glGetProgramiv(shaderProgramObject, GLES31.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
    	if(iShaderProgramLinkStatus[0] == GLES31.GL_FALSE)
    	{
    		GLES31.glGetProgramiv(shaderProgramObject, GLES31.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
    		if(iInfoLogLength[0] > 0)
    		{
    			szInfoLog = GLES31.glGetProgramInfoLog(shaderProgramObject);
    			System.out.println("NRC: Shader Program Link Log: " + szInfoLog);
    			uninitialize();
    			System.exit(0);
    		}
    	}

    	mvpUniform = GLES31.glGetUniformLocation(shaderProgramObject, "u_mvp_matrix");

    	final float triangleVertices[] = new float[]
    	{
    		0.0f, 50.0f, 0.0f,
    		-50.0f, -50.0f, 0.0f,
    		50.0f, -50.0f, 0.0f
    	};

    	//	binding with VAO
    	GLES31.glGenVertexArrays(1, vao, 0);
    	GLES31.glBindVertexArray(vao[0]);

    	//	binding with VBO
    	GLES31.glGenBuffers(1, vbo, 0);
    	GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);

    	ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleVertices.length * 4);
    	byteBuffer.order(ByteOrder.nativeOrder());
    	FloatBuffer verticesBuffer = byteBuffer.asFloatBuffer();
    	verticesBuffer.put(triangleVertices);
    	verticesBuffer.position(0);

    	GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, triangleVertices.length * 4, verticesBuffer, GLES31.GL_STATIC_DRAW);

    	GLES31.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_VERTEX, 3, GLES31.GL_FLOAT, false, 0, 0);

    	GLES31.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_VERTEX);

    	//	unbinding VBO and then VAO
    	GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0);
    	GLES31.glBindVertexArray(0);

    	GLES31.glEnable(GLES31.GL_DEPTH_TEST);
    	GLES31.glDepthFunc(GLES31.GL_LEQUAL);
    	GLES31.glEnable(GLES31.GL_CULL_FACE);
    	GLES31.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);

    	Matrix.setIdentityM(orthographicProjectionMatrix,0);

    }

    private void resize(int width, int height)
    {

        GLES31.glViewport(0, 0, width, height);
        
        if (width <= height)
            Matrix.orthoM(orthographicProjectionMatrix, 0, -100.0f, 100.0f, (-100.0f * (height / width)), (100.0f * (height / width)), -100.0f, 100.0f);
        else
            Matrix.orthoM(orthographicProjectionMatrix, 0, (-100.0f * (width / height)), (100.0f * (width / height)), -100.0f, 100.0f, -100.0f, 100.0f);

    }

    public void display()
    {

    	GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT);

    	GLES31.glUseProgram(shaderProgramObject);

    	float modelViewMatrix[] = new float[16];
        float modelViewProjectionMatrix[] = new float[16];
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, orthographicProjectionMatrix, 0, modelViewMatrix, 0);

        GLES31.glUniformMatrix4fv(mvpUniform, 1, false, modelViewProjectionMatrix, 0);

        GLES31.glBindVertexArray(vao[0]);
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, 3);
        GLES31.glBindVertexArray(0);

        GLES31.glUseProgram(0);

        requestRender();

    }

    void uninitialize()
    {
      
        if(vao[0] != 0)
        {
            GLES31.glDeleteVertexArrays(1, vao, 0);
            vao[0] = 0;
        }
        
        if(vbo[0] != 0)
        {
            GLES31.glDeleteBuffers(1, vbo, 0);
            vbo[0] = 0;
        }

        if(shaderProgramObject != 0)
        {
            if(vertexShaderObject != 0)
            {
                GLES31.glDetachShader(shaderProgramObject, vertexShaderObject);
                GLES31.glDeleteShader(vertexShaderObject);
                vertexShaderObject = 0;
            }
            
            if(fragmentShaderObject != 0)
            {
                GLES31.glDetachShader(shaderProgramObject, fragmentShaderObject);
                GLES31.glDeleteShader(fragmentShaderObject);
                fragmentShaderObject = 0;
            }
        }

        if(shaderProgramObject != 0)
        {
            GLES31.glDeleteProgram(shaderProgramObject);
            shaderProgramObject = 0;
        }
    }

}

