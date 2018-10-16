package com.window.twolights.neeraj;						

import android.content.Context;							

import android.opengl.GLSurfaceView;							
import android.opengl.GLES32;									
import android.opengl.GLES20;	
import android.view.MotionEvent;								
import android.view.GestureDetector;							
import android.view.GestureDetector.OnGestureListener;			
import android.view.GestureDetector.OnDoubleTapListener;								

import javax.microedition.khronos.opengles.GL10;				
import javax.microedition.khronos.egl.EGLConfig;				

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.opengl.Matrix;									

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener
{
	private final Context myContext;
	private GestureDetector myGestureDetector;

	private int VertexShaderObject;
	private int FragmentShaderObject;
	private int ShaderProgramObject;

	private int[] vaoPyramid = new int[1];
	private int[] vboPyramidPosition = new int[1];
	private int[] vboPyramidNormal = new int[1];

	//	light 0 - Red Light
	private float light_0_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float light_0_diffuse[] = { 1.0f, 0.0f, 0.0f, 1.0f };			
	private float light_0_specular[] = { 1.0f, 0.0f, 0.0f, 1.0f };			
	private float light_0_position[] = { 200.0f, 100.0f, 100.0f, 1.0f };	

	//	light 1 - Blue Light
	private float light_1_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float light_1_diffuse[] = { 0.0f, 0.0f, 1.0f, 1.0f };			
	private float light_1_specular[] = { 0.0f, 0.0f, 1.0f, 1.0f };			
	private float light_1_position[] = { -200.0f, 100.0f, 100.0f, 1.0f };	

	private float materialAmbient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float materialDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float materialSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float materialShininess = 50.0f;
	
	private int modelMatrixUniform, viewMatrixUniform, projectionMatrixUniform;

	private int laUniform_0, ldUniform_0, lsUniform_0, lightPositionUniform_0;
	private int laUniform_1, ldUniform_1, lsUniform_1, lightPositionUniform_1;
	private int kaUniform, kdUniform, ksUniform, materialShininessUniform;
	
	private int doubleTapUniform;
	private int doubleTap;
	private float PerspectiveProjectionMatrix[] = new float[16];	
	
	private float anglePyramid = 0.0f;
	
	public GLESView(Context drawingContext)
	{
		super(drawingContext);
		myContext = drawingContext;
		
		setEGLContextClientVersion(3);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		myGestureDetector = new GestureDetector(myContext, this, null, false);
		myGestureDetector.setOnDoubleTapListener(this);
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		String glesVersion = gl.glGetString(GL10.GL_VERSION);
		System.out.println("YSG : OpenGL ES Version : " + glesVersion);
		String glslVersion = gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("YSG : OpenGL Shading Language Version : " + glslVersion);
		
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
		int eventAction = e.getAction();
		if(!myGestureDetector.onTouchEvent(e))
			super.onTouchEvent(e);

		return(true);
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		doubleTap++;
		if(doubleTap > 1)
			doubleTap = 0;

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
	public boolean onSingleTapUp(MotionEvent e)
	{
		return(true);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		return(true);
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		uninitialize();
		System.exit(0);
		return(true);
	}
	
	@Override
	public void onLongPress(MotionEvent e)
	{

	}
	
	@Override
	public void onShowPress(MotionEvent e)
	{

	}
	
	private void initialize(GL10 gl)
	{
		/******************* VERTEX SHADER *************************/
		VertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
		
		// Vertex shader source code:
		final String VertexShaderSourceCode =String.format
        (
        "#version 320 es"	+
		"\n"	+
		"in vec4 vPosition;"	+
		"in vec3 vNormal;"	+
		"uniform mat4 u_model_matrix;"	+
		"uniform mat4 u_view_matrix;"	+
		"uniform mat4 u_projection_matrix;"	+
		"uniform mediump int u_double_tap;"	+
		"uniform vec3 u_La_0;"	+
		"uniform vec3 u_Ld_0;"	+
		"uniform vec3 u_Ls_0;"	+
		"uniform vec4 u_light_position_0;"	+
		"uniform vec3 u_La_1;"	+
		"uniform vec3 u_Ld_1;"	+
		"uniform vec3 u_Ls_1;"	+
		"uniform vec4 u_light_position_1;"	+
		"uniform vec3 u_Ka;"	+
		"uniform vec3 u_Kd;"	+
		"uniform vec3 u_Ks;"	+
		"uniform float u_material_shininess;"	+
		"vec3 phong_ads_color_0;"	+
		"vec3 phong_ads_color_1;"	+
		"out vec3 phong_ads_color;"	+
		"void main(void)"	+
		"{"	+
		"if(u_double_tap == 1)"	+
		"{"	+
		"vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	+
		"vec3 transformed_normals = normalize(mat3(u_view_matrix * u_model_matrix) * vNormal);"	+
		"vec3 light_direction_0 = normalize(vec3(u_light_position_0) - eye_coordinates.xyz);"	+
		"vec3 light_direction_1 = normalize(vec3(u_light_position_1) - eye_coordinates.xyz);"	+
		"float tn_dot_ld_0 = max(dot(transformed_normals, light_direction_0), 0.0);"	+
		"float tn_dot_ld_1 = max(dot(transformed_normals, light_direction_1), 0.0);"	+
		"vec3 ambient_0 = u_La_0 * u_Ka;"	+
		"vec3 diffuse_0 = u_Ld_0 * u_Kd * tn_dot_ld_0;"	+
		"vec3 ambient_1 = u_La_1 * u_Ka;"	+
		"vec3 diffuse_1 = u_Ld_1 * u_Kd * tn_dot_ld_1;"	+
		"vec3 reflection_vector_0 = reflect(-light_direction_0, transformed_normals);"	+
		"vec3 reflection_vector_1 = reflect(-light_direction_1, transformed_normals);"	+
		"vec3 viewer_vector = normalize(-eye_coordinates.xyz);"	+
		"vec3 specular_0 = u_Ls_0 * u_Ks * pow(max(dot(reflection_vector_0, viewer_vector), 0.0), u_material_shininess);"	+
		"vec3 specular_1 = u_Ls_1 * u_Ks * pow(max(dot(reflection_vector_1, viewer_vector), 0.0), u_material_shininess);"	+
		"phong_ads_color_0 = ambient_0 + diffuse_0 + specular_0;"	+
		"phong_ads_color_1 = ambient_1 + diffuse_1 + specular_1;"	+	
		"phong_ads_color = phong_ads_color_0 + phong_ads_color_1;"	+
		"}"	+
		"else"	+
		"{"	+
		"phong_ads_color = vec3(1.0, 1.0, 1.0);"	+
		"}"	+
		"gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"	+
		"}"
        );

		GLES32.glShaderSource(VertexShaderObject, VertexShaderSourceCode);

		GLES32.glCompileShader(VertexShaderObject);
		int[] iShaderCompilationStatus = new int[1];	
		int[] iInfoLogLength = new int[1];				
		String szInfoLog = null;						
		GLES32.glGetShaderiv(VertexShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompilationStatus, 0);
		if(iShaderCompilationStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(VertexShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetShaderInfoLog(VertexShaderObject);
				System.out.println("NRC : Vertex Shader Compilation Log = " + szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		/************************FRAGMENT SHADER*********************************/
		
		FragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
		
        final String FragmentShaderSourceCode = String.format
        (
         "#version 320 es"	+
         "\n"	+
         "precision highp float;"	+
         "in vec3 phong_ads_color;"	+
         "out vec4 FragColor;"	+
         "void main(void)"	+
         "{"	+
         "FragColor = vec4(phong_ads_color, 1.0);"	+
         "}"
        );

		GLES32.glShaderSource(FragmentShaderObject, FragmentShaderSourceCode);

		GLES32.glCompileShader(FragmentShaderObject);
		iShaderCompilationStatus[0] = 0;			
		iInfoLogLength[0] = 0;						
		szInfoLog = null;							
		GLES32.glGetShaderiv(FragmentShaderObject, GLES32.GL_COMPILE_STATUS, iShaderCompilationStatus, 0);
		if(iShaderCompilationStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(FragmentShaderObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetShaderInfoLog(FragmentShaderObject);
				System.out.println("NRC : Fragment shader compilation log = "+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		ShaderProgramObject = GLES32.glCreateProgram();
		
		GLES32.glAttachShader(ShaderProgramObject, VertexShaderObject);
		GLES32.glAttachShader(ShaderProgramObject, FragmentShaderObject);
		
		GLES32.glBindAttribLocation(ShaderProgramObject, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
		GLES32.glBindAttribLocation(ShaderProgramObject, GLESMacros.NRC_ATTRIBUTE_NORMAL, "vNormal");

		GLES32.glLinkProgram(ShaderProgramObject);
		int[] iShaderProgramLinkStatus = new int[1];		
		iInfoLogLength[0] = 0;								
		szInfoLog = null;								
		GLES32.glGetProgramiv(ShaderProgramObject, GLES32.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
		if(iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetProgramiv(ShaderProgramObject, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetProgramInfoLog(ShaderProgramObject);
				System.out.println("NRC : Shader Program link log = "+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
        modelMatrixUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_model_matrix");
        viewMatrixUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_view_matrix");
        projectionMatrixUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_projection_matrix");
        doubleTapUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_double_tap");
        laUniform_0 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_La_0");
        ldUniform_0 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ld_0");
        lsUniform_0 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ls_0");
        lightPositionUniform_0 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_light_position_0");
        laUniform_1 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_La_1");
        ldUniform_1 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ld_1");
        lsUniform_1 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ls_1");
        lightPositionUniform_1 = GLES32.glGetUniformLocation(ShaderProgramObject, "u_light_position_1");
        kaUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ka");
        kdUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Kd");
        ksUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ks");
        materialShininessUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_material_shininess");

		float pyramidVertices[] = 
		{	
			0.0f, 1.0f, 0.0f,		
			-1.0f, -1.0f, 1.0f,		
			1.0f, -1.0f, 1.0f,		
			0.0f, 1.0f, 0.0f,		
			1.0f, -1.0f, 1.0f,		
			1.0f, -1.0f, -1.0f,		
			0.0f, 1.0f, 0.0f,		
			1.0f, -1.0f, -1.0f,		
			-1.0f, -1.0f, -1.0f,	
			0.0f, 1.0f, 0.0f,		
			-1.0f, -1.0f, -1.0f,	
			-1.0f, -1.0f, 1.0f		
		};

		float pyramidNormals[] =
		{
			0.0f, 0.447214f, 0.894427f,			
			0.0f, 0.447214f, 0.894427f,			
			0.0f, 0.447214f, 0.894427f,			
			0.894427f, 0.447214f, 0.0f,			
			0.894427f, 0.447214f, 0.0f,			
			0.894427f, 0.447214f, 0.0f,			
			0.0f, 0.447214f, -0.894427f,		
			0.0f, 0.447214f, -0.894427f,		
			0.0f, 0.447214f, -0.894427f,		
			-0.894427f, 0.447214f, 0.0f,		
			-0.894427f, 0.447214f, 0.0f,		
			-0.894427f, 0.447214f, 0.0f			
		};
									
		GLES32.glGenVertexArrays(1, vaoPyramid, 0);	
		GLES32.glBindVertexArray(vaoPyramid[0]);	

		GLES32.glGenBuffers(1, vboPyramidPosition, 0);						
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPyramidPosition[0]);

		ByteBuffer PyramidPosByteBuffer = ByteBuffer.allocateDirect(pyramidVertices.length * 4);
		PyramidPosByteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer PyramidVerticesBuffer = PyramidPosByteBuffer.asFloatBuffer();
		PyramidVerticesBuffer.put(pyramidVertices);
		PyramidVerticesBuffer.position(0);
		
		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, pyramidVertices.length * 4, PyramidVerticesBuffer, GLES32.GL_STATIC_DRAW);			
		GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_VERTEX, 3, GLES32.GL_FLOAT, false, 0, 0);						
		GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_VERTEX);

		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
		
		GLES32.glGenBuffers(1, vboPyramidNormal, 0);							
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboPyramidNormal[0]);	

		ByteBuffer PyramidNormalByteBuffer = ByteBuffer.allocateDirect(pyramidNormals.length * 4);
		PyramidNormalByteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer PyramidNormalBuffer = PyramidNormalByteBuffer.asFloatBuffer();
		PyramidNormalBuffer.put(pyramidNormals);
		PyramidNormalBuffer.position(0);
		
		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, pyramidNormals.length * 4, PyramidNormalBuffer, GLES32.GL_STATIC_DRAW);			
		GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_NORMAL, 3, GLES32.GL_FLOAT, false, 0, 0);						
		GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_NORMAL);
		
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
		
		GLES32.glBindVertexArray(0);
		

		GLES32.glEnable(GLES32.GL_DEPTH_TEST);
		GLES32.glDepthFunc(GLES32.GL_LEQUAL);
		GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		doubleTap = 0;

		Matrix.setIdentityM(PerspectiveProjectionMatrix, 0);
	}

	private void resize(int width, int height)
	{
		GLES32.glViewport(0, 0, width, height);
		Matrix.perspectiveM(PerspectiveProjectionMatrix, 0, 45.0f, ((float)width / (float)height), 0.1f, 100.0f);
	}
	
	public void display()
	{
		GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

		GLES32.glUseProgram(ShaderProgramObject);
		
		if(doubleTap == 1)
		{
			GLES32.glUniform1i(doubleTapUniform, 1);

            GLES32.glUniform3fv(laUniform_0, 1, light_0_ambient, 0);
            GLES32.glUniform3fv(ldUniform_0, 1, light_0_diffuse, 0);
            GLES32.glUniform3fv(lsUniform_0, 1, light_0_specular, 0);
            GLES32.glUniform4fv(lightPositionUniform_0, 1, light_0_position, 0);

			GLES32.glUniform3fv(laUniform_1, 1, light_1_ambient, 0);
            GLES32.glUniform3fv(ldUniform_1, 1, light_1_diffuse, 0);
            GLES32.glUniform3fv(lsUniform_1, 1, light_1_specular, 0);
            GLES32.glUniform4fv(lightPositionUniform_1, 1, light_1_position, 0);

            GLES32.glUniform3fv(kaUniform, 1, materialAmbient, 0);
            GLES32.glUniform3fv(kdUniform, 1, materialDiffuse, 0);
            GLES32.glUniform3fv(ksUniform, 1, materialSpecular, 0);
            GLES32.glUniform1f(materialShininessUniform, materialShininess);
		}
		else
		{
			GLES32.glUniform1i(doubleTapUniform, 0);
        }

        float modelMatrix[] = new float[16];
        float viewMatrix[] = new float[16];
        float rotationMatrix[] = new float[16];
		
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(viewMatrix, 0);
		Matrix.setIdentityM(rotationMatrix, 0);

        Matrix.translateM(modelMatrix,0,0.0f,0.0f,-5.0f);
		Matrix.rotateM(rotationMatrix, 0, rotationMatrix, 0, anglePyramid, 0.0f, 1.0f, 0.0f);
		Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0);
		
        GLES32.glUniformMatrix4fv(modelMatrixUniform, 1, false, modelMatrix, 0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform, 1, false, viewMatrix, 0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform, 1, false, PerspectiveProjectionMatrix, 0);

        GLES32.glBindVertexArray(vaoPyramid[0]);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 12);
        GLES32.glBindVertexArray(0);

		update();
		
		GLES32.glUseProgram(0);

		requestRender();
	}
	
	private void update()
	{
		anglePyramid = anglePyramid + 0.5f;
		if (anglePyramid >= 360.0f)
			anglePyramid = 0.0f;
	}	
	void uninitialize()
	{
		if(vaoPyramid[0] != 0)
		{
			GLES32.glDeleteVertexArrays(1, vaoPyramid, 0);
			vaoPyramid[0] = 0;
		}

		if(vboPyramidPosition[0] != 0)
		{
			GLES32.glDeleteBuffers(1, vboPyramidPosition, 0);
			vboPyramidPosition[0] = 0;
		}

		if(vboPyramidNormal[0] != 0)
		{
			GLES32.glDeleteBuffers(1, vboPyramidNormal, 0);
			vboPyramidNormal[0] = 0;
		}

		if(ShaderProgramObject != 0)
		{
			if(VertexShaderObject != 0)
			{
				GLES32.glDetachShader(ShaderProgramObject, VertexShaderObject);
				GLES32.glDeleteShader(VertexShaderObject);
				VertexShaderObject = 0;
			}
			
			if(FragmentShaderObject != 0)
			{
				GLES32.glDetachShader(ShaderProgramObject, FragmentShaderObject);
				GLES32.glDeleteShader(FragmentShaderObject);
				FragmentShaderObject = 0;
			}
		}

		if(ShaderProgramObject != 0)
		{
			GLES32.glDeleteProgram(ShaderProgramObject);
			ShaderProgramObject = 0;
		}
	}
}
		