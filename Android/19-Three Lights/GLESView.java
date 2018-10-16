package com.window.threelights.neeraj;						

import android.content.Context;									
import android.opengl.GLSurfaceView;							
import android.opengl.GLES32;									
import android.opengl.GLES20;									
import android.view.MotionEvent;								
import android.view.GestureDetector;							
import android.view.GestureDetector.OnGestureListener;			
import android.view.GestureDetector.OnDoubleTapListener;		

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;				
import javax.microedition.khronos.egl.EGLConfig;

import android.opengl.Matrix;									

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener
{
	private final Context myContext;
	private GestureDetector myGestureDetector;

	// Vertex shader object:
	private int VertexShaderObject_pv;		// For per vertex shading
	private int VertexShaderObject_pf;		// For per fragment shading
	
	// Fragment shader object:
	private int FragmentShaderObject_pv;	// For per vertex shading
	private int FragmentShaderObject_pf;	// For per fragment shading
	
	// Shader program object:
	private int ShaderProgramObject_pv;		// For per vertex lighting
	private int ShaderProgramObject_pf;		// For per fragment lighting
	
	private int numElements;
	private int numVertices;

	// vao and vbo:
	private int[] vaoSphere = new int[1];
	private int[] vboSpherePosition = new int[1];
	private int[] vboSphereNormal = new int[1];
	private int[] vboSphereElement = new int[1];

	// Light rotation variables:
	private float angle_RedLight = 0.0f;
	private float angle_GreenLight = 0.0f;
	private float angle_BlueLight = 0.0f;

	// 1. RED LIGHT:
	private int LaUniformRed_pv;				
	private int LdUniformRed_pv;				
	private int LsUniformRed_pv;				
	private int RedLightPositionUniform_pv;		

	// 2. GREEN LIGHT:
	private int LaUniformGreen_pv;				
	private int LdUniformGreen_pv;				
	private int LsUniformGreen_pv;				
	private int GreenLightPositionUniform_pv;	

	// 3. BLUE LIGHT:
	private int LaUniformBlue_pv;				
	private int LdUniformBlue_pv;				
	private int LsUniformBlue_pv;				
	private int BlueLightPositionUniform_pv;	

	// Material uniforms:
	private int KaUniform_pv;					
	private int KdUniform_pv;					
	private int KsUniform_pv;					
	private int MaterialShininessUniform_pv;	
	
	private int modelMatrixUniform_pv, viewMatrixUniform_pv, projectionMatrixUniform_pv;
	
	private int doubleTapUniform_pv;			

	private int LaUniformRed_pf;				
	private int LdUniformRed_pf;				
	private int LsUniformRed_pf;				
	private int RedLightPositionUniform_pf;		

	private int LaUniformGreen_pf;				
	private int LdUniformGreen_pf;				
	private int LsUniformGreen_pf;				
	private int GreenLightPositionUniform_pf;	

	private int LaUniformBlue_pf;				
	private int LdUniformBlue_pf;				
	private int LsUniformBlue_pf;				
	private int BlueLightPositionUniform_pf;	

	private int KaUniform_pf;					
	private int KdUniform_pf;					
	private int KsUniform_pf;					
	private int MaterialShininessUniform_pf;	
	
	private int modelMatrixUniform_pf, viewMatrixUniform_pf, projectionMatrixUniform_pf;
	
	private int doubleTapUniform_pf;		
	
	private int doubleTap;
	private int singleTap;
	
	private float PerspectiveProjectionMatrix[] = new float[16];		// 4x4 matrix
	
	// RED LIGHT:
	private float light_Red_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float light_Red_diffuse[] = { 1.0f, 0.0f, 0.0f, 1.0f };		
	private float light_Red_specular[] = { 1.0f, 0.0f, 0.0f, 1.0f };	
	private float light_Red_position[] = { 0.0f, 0.0f, 0.0f, 0.0f };	

	// GREEN LIGHT:	
	private float light_Green_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float light_Green_diffuse[] = { 0.0f, 1.0f, 0.0f, 1.0f };	
	private float light_Green_specular[] = { 0.0f, 1.0f, 0.0f, 1.0f };	
	private float light_Green_position[] = { 0.0f, 0.0f, 0.0f, 0.0f };	

	// BLUE LIGHT:
	private float light_Blue_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float light_Blue_diffuse[] = { 0.0f, 0.0f, 1.0f, 1.0f };	
	private float light_Blue_specular[] = { 0.0f, 0.0f, 1.0f, 1.0f };	
	private float light_Blue_position[] = { 0.0f, 0.0f, 0.0f, 0.0f };	
	
	// MATERIAL:
	private float material_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float material_diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float material_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float material_shininess = 50.0f;

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
		System.out.println("NRC : OpenGL ES Version : " + glesVersion);
		String glslVersion = gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("NRC : OpenGL Shading Language Version : " + glslVersion);
		
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
		singleTap++;

		if(singleTap > 1)
			singleTap = 0;
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
		/* PER VERTEX SHADING */
		/*------------------------- VERTEX SHADER ------------------------------------*/
		
		VertexShaderObject_pv = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
		
		final String VertexShaderSourceCode_pv = String.format
        (
         "#version 320 es"	+
         "\n"	+
         "in vec4 vPosition;"	+
         "in vec3 vNormal;"	+
         "uniform mat4 u_model_matrix;"	+
         "uniform mat4 u_view_matrix;"	+
         "uniform mat4 u_projection_matrix;"	+
         "uniform int u_double_tap;"	+
         "uniform vec3 u_La_Red;"	+
         "uniform vec3 u_Ld_Red;"	+
         "uniform vec3 u_Ls_Red;"	+
         "uniform vec4 u_light_position_Red;"	+
		 "uniform vec3 u_La_Green;"	+
         "uniform vec3 u_Ld_Green;"	+
         "uniform vec3 u_Ls_Green;"	+
         "uniform vec4 u_light_position_Green;"	+
		 "uniform vec3 u_La_Blue;"	+
         "uniform vec3 u_Ld_Blue;"	+
         "uniform vec3 u_Ls_Blue;"	+
         "uniform vec4 u_light_position_Blue;"	+
         "uniform vec3 u_Ka;"	+
         "uniform vec3 u_Kd;"	+
         "uniform vec3 u_Ks;"	+
         "uniform float u_material_shininess;"	+
		 "vec3 phong_ads_color_Red;"	+
		 "vec3 phong_ads_color_Green;"	+
		 "vec3 phong_ads_color_Blue;"	+
         "out vec3 vPhong_ads_color;"	+
         "void main(void)"	+
         "{"	+
         "if (u_double_tap == 1)"	+
         "{"	+
		 "vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	+	
		 "vec3 transformed_normals = normalize(mat3(u_view_matrix * u_model_matrix) * vNormal);"	+
		 "vec3 light_direction_Red = normalize(vec3(u_light_position_Red) - eye_coordinates.xyz);"	+
		 "vec3 light_direction_Green = normalize(vec3(u_light_position_Green) - eye_coordinates.xyz);"	+
		 "vec3 light_direction_Blue = normalize(vec3(u_light_position_Blue) - eye_coordinates.xyz);"	+
		 "float tn_dot_ld_Red = max(dot(transformed_normals, light_direction_Red), 0.0);"	+
		 "float tn_dot_ld_Green = max(dot(transformed_normals, light_direction_Green), 0.0);"	+
		 "float tn_dot_ld_Blue = max(dot(transformed_normals, light_direction_Blue), 0.0);"	+
		 "vec3 ambient_Red = u_La_Red * u_Ka;"	+
		 "vec3 ambient_Green = u_La_Green * u_Ka;"	+
		 "vec3 ambient_Blue = u_La_Blue * u_Ka;"	+
		 "vec3 diffuse_Red = u_Ld_Red * u_Kd * tn_dot_ld_Red;"	+
		 "vec3 diffuse_Green = u_Ld_Green * u_Kd * tn_dot_ld_Green;"	+
		 "vec3 diffuse_Blue = u_Ld_Blue * u_Kd * tn_dot_ld_Blue;"	+
		 "vec3 reflection_vector_Red = reflect(-light_direction_Red, transformed_normals);"	+
		 "vec3 reflection_vector_Green = reflect(-light_direction_Green, transformed_normals);"	+
		 "vec3 reflection_vector_Blue = reflect(-light_direction_Blue, transformed_normals);"	+
		 "vec3 viewer_vector = normalize(-eye_coordinates.xyz);"	+
		 "vec3 specular_Red = u_Ls_Red * u_Ks * pow(max(dot(reflection_vector_Red, viewer_vector), 0.0), u_material_shininess);"	+
		 "vec3 specular_Green = u_Ls_Green * u_Ks * pow(max(dot(reflection_vector_Green, viewer_vector), 0.0), u_material_shininess);"	+
		 "vec3 specular_Blue = u_Ls_Blue * u_Ks * pow(max(dot(reflection_vector_Blue, viewer_vector), 0.0), u_material_shininess);"	+
		 "phong_ads_color_Red = ambient_Red + diffuse_Red + specular_Red;"	+
		 "phong_ads_color_Green = ambient_Green + diffuse_Green + specular_Green;"	+
		 "phong_ads_color_Blue = ambient_Blue + diffuse_Blue + specular_Blue;"	+
		 "vPhong_ads_color = phong_ads_color_Red + phong_ads_color_Green + phong_ads_color_Blue;"	+
         "}"	+
         "else"	+
         "{"	+
         "vPhong_ads_color = vec3(1.0, 1.0, 1.0);"	+
         "}"	+
         "gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"	+
         "}"
        );

		GLES32.glShaderSource(VertexShaderObject_pv, VertexShaderSourceCode_pv);
		GLES32.glCompileShader(VertexShaderObject_pv);

		int[] iShaderCompilationStatus = new int[1];	
		int[] iInfoLogLength = new int[1];				
		String szInfoLog = null;						
		GLES32.glGetShaderiv(VertexShaderObject_pv, GLES32.GL_COMPILE_STATUS, iShaderCompilationStatus, 0);
		if(iShaderCompilationStatus[0] == GLES32.GL_FALSE)	
		{
			GLES32.glGetShaderiv(VertexShaderObject_pv, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetShaderInfoLog(VertexShaderObject_pv);
				System.out.println("NRC : (Per Vertex) Vertex Shader Compilation Log = "+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		/*------------------------------------------ FRAGMENT SHADER ---------------------------------*/

		FragmentShaderObject_pv = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
		
        final String FragmentShaderSourceCode_pv =String.format
        (
         "#version 320 es"	+
         "\n"+
         "precision highp float;"	+
         "in vec3 vPhong_ads_color;"	+
         "out vec4 FragColor;"	+
         "void main(void)"	+
         "{"	+
         "FragColor = vec4(vPhong_ads_color, 1.0);"	+
         "}"
        );

		GLES32.glShaderSource(FragmentShaderObject_pv, FragmentShaderSourceCode_pv);
		GLES32.glCompileShader(FragmentShaderObject_pv);

		iShaderCompilationStatus[0] = 0;			
		iInfoLogLength[0] = 0;					
		szInfoLog = null;						
		GLES32.glGetShaderiv(FragmentShaderObject_pv, GLES32.GL_COMPILE_STATUS, iShaderCompilationStatus, 0);
		if(iShaderCompilationStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(FragmentShaderObject_pv, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetShaderInfoLog(FragmentShaderObject_pv);
				System.out.println("NRC : (Per Vertex) Fragment shader compilation log = " + szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		ShaderProgramObject_pv = GLES32.glCreateProgram();

		GLES32.glAttachShader(ShaderProgramObject_pv, VertexShaderObject_pv);
		GLES32.glAttachShader(ShaderProgramObject_pv, FragmentShaderObject_pv);
		GLES32.glBindAttribLocation(ShaderProgramObject_pv, GLESMacros.	NRC_ATTRIBUTE_VERTEX, "vPosition");
		GLES32.glBindAttribLocation(ShaderProgramObject_pv, GLESMacros.NRC_ATTRIBUTE_NORMAL, "vNormal");
		
		GLES32.glLinkProgram(ShaderProgramObject_pv);

		int[] iShaderProgramLinkStatus = new int[1];		
		iInfoLogLength[0] = 0;							
		szInfoLog = null;									
		GLES32.glGetProgramiv(ShaderProgramObject_pv, GLES32.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
		if(iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetProgramiv(ShaderProgramObject_pv, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetProgramInfoLog(ShaderProgramObject_pv);
				System.out.println("NRC : Shader Program link log = " + szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		/* PER FRAGMENT SHADING */
		/*------------------------------------- VERTEX SHADER --------------------------------------*/

		VertexShaderObject_pf = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

		final String VertexShaderSourceCode_pf = String.format
        (
         "#version 320 es"	+
         "\n"	+
         "in vec4 vPosition;"	+
         "in vec3 vNormal;"	+
         "uniform mat4 u_model_matrix;"	+
         "uniform mat4 u_view_matrix;"	+
         "uniform mat4 u_projection_matrix;"	+
         "uniform mediump int u_double_tap;"	+
         "uniform vec4 u_light_position_Red;"	+
		 "uniform vec4 u_light_position_Green;"	+
         "uniform vec4 u_light_position_Blue;"	+
         "out vec3 transformed_normals;"	+
         "out vec3 light_direction_Red;"	+
		 "out vec3 light_direction_Green;"	+
         "out vec3 light_direction_Blue;"	+
         "out vec3 viewer_vector;"	+
         "void main(void)"	+
         "{"	+
         "if (u_double_tap == 1)"	+
         "{"	+
         "vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	+
         "transformed_normals = mat3(u_view_matrix * u_model_matrix) * vNormal;"	+
         "light_direction_Red = vec3(u_light_position_Red) - eye_coordinates.xyz;"	+
         "light_direction_Green = vec3(u_light_position_Green) - eye_coordinates.xyz;"	+
         "light_direction_Blue = vec3(u_light_position_Blue) - eye_coordinates.xyz;"	+
         "viewer_vector = -eye_coordinates.xyz;"	+
         "}"	+
         "gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"	+
         "}"
        );
		
		GLES32.glShaderSource(VertexShaderObject_pf, VertexShaderSourceCode_pf);
		GLES32.glCompileShader(VertexShaderObject_pf);
		
		iShaderCompilationStatus[0] = 0;			
		iInfoLogLength[0] = 0;						
		szInfoLog = null;							
		GLES32.glGetShaderiv(VertexShaderObject_pf, GLES32.GL_COMPILE_STATUS, iShaderCompilationStatus, 0);
		if(iShaderCompilationStatus[0] == GLES32.GL_FALSE)	
		{
			GLES32.glGetShaderiv(VertexShaderObject_pf, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetShaderInfoLog(VertexShaderObject_pf);
				System.out.println("NRC : (Per Fragment) Vertex Shader Compilation Log = " + szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		/*-------------------------- FRAGMENT SHADER ---------------------------*/

		FragmentShaderObject_pf = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

        final String FragmentShaderSourceCode_pf = String.format
        (
         "#version 320 es"	+
         "\n"	+
         "precision highp float;"	+
         "in vec3 transformed_normals;"	+
         "in vec3 light_direction_Red;"	+
         "in vec3 light_direction_Green;"	+
         "in vec3 light_direction_Blue;"	+		 
         "in vec3 viewer_vector;"	+
         "out vec4 FragColor;"	+
         "uniform vec3 u_La_Red;"	+
         "uniform vec3 u_Ld_Red;"	+
         "uniform vec3 u_Ls_Red;"	+
         "uniform vec4 u_light_position_Red;"	+
		 "uniform vec3 u_La_Green;"	+
         "uniform vec3 u_Ld_Green;"	+
         "uniform vec3 u_Ls_Green;"	+
         "uniform vec4 u_light_position_Green;"	+
		 "uniform vec3 u_La_Blue;"	+
         "uniform vec3 u_Ld_Blue;"	+
         "uniform vec3 u_Ls_Blue;"	+
         "uniform vec4 u_light_position_Blue;"	+
         "uniform vec3 u_Ka;"	+
         "uniform vec3 u_Kd;"	+
         "uniform vec3 u_Ks;"	+
         "uniform float u_material_shininess;"	+
		 "vec3 phong_ads_color_Red;"	+
		 "vec3 phong_ads_color_Green;"	+
		 "vec3 phong_ads_color_Blue;"	+
         "out vec3 fPhong_ads_color;"	+
         "uniform int u_double_tap;"	+
         "void main(void)"	+
         "{"	+
         "if(u_double_tap == 1)"	+
         "{"	+
         "vec3 normalized_transformed_normals = normalize(transformed_normals);"	+
		 "vec3 normalized_light_direction_Red = normalize(light_direction_Red);"	+
		 "vec3 normalized_light_direction_Green = normalize(light_direction_Green);"	+
		 "vec3 normalized_light_direction_Blue = normalize(light_direction_Blue);"	+
		 "vec3 normalized_viewer_vector = normalize(viewer_vector);"	+
		 "vec3 ambient_Red = u_La_Red * u_Ka;"	+
		 "vec3 ambient_Green = u_La_Green * u_Ka;"	+
		 "vec3 ambient_Blue = u_La_Blue * u_Ka;"	+
		 "float tn_dot_ld_Red = max(dot(normalized_transformed_normals, normalized_light_direction_Red), 0.0);"	+
		 "float tn_dot_ld_Green = max(dot(normalized_transformed_normals, normalized_light_direction_Green), 0.0);"	+
		 "float tn_dot_ld_Blue = max(dot(normalized_transformed_normals, normalized_light_direction_Blue), 0.0);"	+
		 "vec3 diffuse_Red = u_Ld_Red * u_Kd * tn_dot_ld_Red;"	+
		 "vec3 diffuse_Green = u_Ld_Green * u_Kd * tn_dot_ld_Green;"	+
		 "vec3 diffuse_Blue = u_Ld_Blue * u_Kd * tn_dot_ld_Blue;"	+
		 "vec3 reflection_vector_Red = reflect(-normalized_light_direction_Red, normalized_transformed_normals);"	+
		 "vec3 reflection_vector_Green = reflect(-normalized_light_direction_Green, normalized_transformed_normals);"	+
		 "vec3 reflection_vector_Blue = reflect(-normalized_light_direction_Blue, normalized_transformed_normals);"	+
		 "vec3 specular_Red = u_Ls_Red * u_Ks * pow(max(dot(reflection_vector_Red, normalized_viewer_vector), 0.0), u_material_shininess);"	+
		 "vec3 specular_Green = u_Ls_Green * u_Ks * pow(max(dot(reflection_vector_Green, normalized_viewer_vector), 0.0), u_material_shininess);"	+
		 "vec3 specular_Blue = u_Ls_Blue * u_Ks * pow(max(dot(reflection_vector_Blue, normalized_viewer_vector), 0.0), u_material_shininess);"	+
		 "vec3 phong_ads_color_Red = ambient_Red + diffuse_Red + specular_Red;"	+
		 "vec3 phong_ads_color_Green = ambient_Green + diffuse_Green + specular_Green;"	+
		 "vec3 phong_ads_color_Blue = ambient_Blue + diffuse_Blue + specular_Blue;"	+
		 "fPhong_ads_color = phong_ads_color_Red + phong_ads_color_Green + phong_ads_color_Blue;"	+      
		 "}"	+
         "else"	+
         "{"	+
         "fPhong_ads_color = vec3(1.0, 1.0, 1.0);"	+
         "}"	+
         "FragColor = vec4(fPhong_ads_color, 1.0);"	+
         "}"
        );
		
		GLES32.glShaderSource(FragmentShaderObject_pf, FragmentShaderSourceCode_pf);
		GLES32.glCompileShader(FragmentShaderObject_pf);

		iShaderCompilationStatus[0] = 0;			
		iInfoLogLength[0] = 0;						
		szInfoLog = null;							
		GLES32.glGetShaderiv(FragmentShaderObject_pf, GLES32.GL_COMPILE_STATUS, iShaderCompilationStatus, 0);
		if(iShaderCompilationStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetShaderiv(FragmentShaderObject_pf, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetShaderInfoLog(FragmentShaderObject_pf);
				System.out.println("NRC : (Per Fragment) Fragment shader compilation log = " + szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}

		ShaderProgramObject_pf = GLES32.glCreateProgram();

		GLES32.glAttachShader(ShaderProgramObject_pf, VertexShaderObject_pf);
		GLES32.glAttachShader(ShaderProgramObject_pf, FragmentShaderObject_pf);
		GLES32.glBindAttribLocation(ShaderProgramObject_pf, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
		GLES32.glBindAttribLocation(ShaderProgramObject_pf, GLESMacros.NRC_ATTRIBUTE_NORMAL, "vNormal");
		
		GLES32.glLinkProgram(ShaderProgramObject_pf);
		iShaderProgramLinkStatus[0] = 0;					
		iInfoLogLength[0] = 0;								
		szInfoLog = null;								
		GLES32.glGetProgramiv(ShaderProgramObject_pf, GLES32.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
		if(iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
		{
			GLES32.glGetProgramiv(ShaderProgramObject_pf, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
			if(iInfoLogLength[0] > 0)
			{
				szInfoLog = GLES32.glGetProgramInfoLog(ShaderProgramObject_pf);
				System.out.println("NRC : Shader Program link log = "+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		//	Per Vertex Uniforms
        modelMatrixUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_model_matrix");
        viewMatrixUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_view_matrix");
        projectionMatrixUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_projection_matrix");
        doubleTapUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_double_tap");
        LaUniformRed_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_La_Red");
        LdUniformRed_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ld_Red");
        LsUniformRed_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ls_Red");
        RedLightPositionUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_light_position_Red");
		LaUniformGreen_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_La_Green");
        LdUniformGreen_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ld_Green");
        LsUniformGreen_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ls_Green");
        GreenLightPositionUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_light_position_Green");
		LaUniformBlue_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_La_Blue");
        LdUniformBlue_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ld_Blue");
        LsUniformBlue_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ls_Blue");
        BlueLightPositionUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_light_position_Blue");
        KaUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ka");
        KdUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Kd");
        KsUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_Ks");
        MaterialShininessUniform_pv = GLES32.glGetUniformLocation(ShaderProgramObject_pv, "u_material_shininess");

		//	Per Fragment Uniforms
        modelMatrixUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_model_matrix");
        viewMatrixUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_view_matrix");
        projectionMatrixUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_projection_matrix");
        doubleTapUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_double_tap");
        LaUniformRed_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_La_Red");
        LdUniformRed_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ld_Red");
        LsUniformRed_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ls_Red");
        RedLightPositionUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_light_position_Red");
		LaUniformGreen_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_La_Green");
        LdUniformGreen_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ld_Green");
        LsUniformGreen_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ls_Green");
        GreenLightPositionUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_light_position_Green");
		LaUniformBlue_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_La_Blue");
        LdUniformBlue_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ld_Blue");
        LsUniformBlue_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ls_Blue");
        BlueLightPositionUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_light_position_Blue");
        KaUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ka");
        KdUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Kd");
        KsUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_Ks");
        MaterialShininessUniform_pf = GLES32.glGetUniformLocation(ShaderProgramObject_pf, "u_material_shininess");

		
		// Sphere VAO and VBO
        Sphere sphere=new Sphere();
        float sphereVertices[]=new float[1146];
        float sphereNormals[]=new float[1146];
        float sphereTextures[]=new float[764];
        short sphereElements[]=new short[2280];
        sphere.getSphereVertexData(sphereVertices, sphereNormals, sphereTextures, sphereElements);
        numVertices = sphere.getNumberOfSphereVertices();
        numElements = sphere.getNumberOfSphereElements();

		//	Sphere VAO									
		GLES32.glGenVertexArrays(1, vaoSphere, 0);		
		GLES32.glBindVertexArray(vaoSphere[0]);			
		//	VBO Position
		GLES32.glGenBuffers(1, vboSpherePosition, 0);						
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboSpherePosition[0]);	
		
		ByteBuffer SpherePosByteBuffer = ByteBuffer.allocateDirect(sphereVertices.length * 4);
		SpherePosByteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer SphereVerticesBuffer = SpherePosByteBuffer.asFloatBuffer();
		SphereVerticesBuffer.put(sphereVertices);
		SphereVerticesBuffer.position(0);
		
		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphereVertices.length * 4, SphereVerticesBuffer, GLES32.GL_STATIC_DRAW);			
		GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_VERTEX, 3, GLES32.GL_FLOAT, false, 0, 0);						
		GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_VERTEX);
		
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
		
		//	VBO Normal
		GLES32.glGenBuffers(1, vboSphereNormal, 0);							
		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboSphereNormal[0]);	

		ByteBuffer SphereNormalByteBuffer = ByteBuffer.allocateDirect(sphereNormals.length * 4);
		SphereNormalByteBuffer.order(ByteOrder.nativeOrder());
		FloatBuffer SphereNormalBuffer = SphereNormalByteBuffer.asFloatBuffer();
		SphereNormalBuffer.put(sphereNormals);
		SphereNormalBuffer.position(0);
		
		GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphereNormals.length * 4, SphereNormalBuffer, GLES32.GL_STATIC_DRAW);			
		GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_NORMAL, 3, GLES32.GL_FLOAT, false, 0, 0);						
		GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_NORMAL);

		GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
		
		
		//	VBO Elements
        GLES32.glGenBuffers(1,vboSphereElement,0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vboSphereElement[0]);
        
		ByteBuffer SphereElementByteBuffer = ByteBuffer.allocateDirect(sphereElements.length * 4);
		SphereElementByteBuffer.order(ByteOrder.nativeOrder());
		ShortBuffer SphereElementBuffer = SphereElementByteBuffer.asShortBuffer();
		SphereElementBuffer.put(sphereElements);
		SphereElementBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, sphereElements.length * 2, SphereElementBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,0);

		// Unbind VAO Sphere
		GLES32.glBindVertexArray(0);
		
		GLES32.glEnable(GLES32.GL_DEPTH_TEST);
		GLES32.glDepthFunc(GLES32.GL_LEQUAL);
		GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		doubleTap = 0;	
		singleTap = 0;

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
		
		if(singleTap == 0)
		{
			GLES32.glUseProgram(ShaderProgramObject_pv);
		
			if(doubleTap == 1)
			{
				GLES32.glUniform1i(doubleTapUniform_pv, 1);
		
				GLES32.glUniform3fv(LaUniformRed_pv, 1, light_Red_ambient, 0);
				GLES32.glUniform3fv(LdUniformRed_pv, 1, light_Red_diffuse, 0);
				GLES32.glUniform3fv(LsUniformRed_pv, 1, light_Red_specular, 0);
				GLES32.glUniform4fv(RedLightPositionUniform_pv, 1, light_Red_position, 0);
            
				GLES32.glUniform3fv(LaUniformGreen_pv, 1, light_Green_ambient, 0);
				GLES32.glUniform3fv(LdUniformGreen_pv, 1, light_Green_diffuse, 0);
				GLES32.glUniform3fv(LsUniformGreen_pv, 1, light_Green_specular, 0);
				GLES32.glUniform4fv(GreenLightPositionUniform_pv, 1, light_Green_position, 0);

				GLES32.glUniform3fv(LaUniformBlue_pv, 1, light_Blue_ambient, 0);
				GLES32.glUniform3fv(LdUniformBlue_pv, 1, light_Blue_diffuse, 0);
				GLES32.glUniform3fv(LsUniformBlue_pv, 1, light_Blue_specular, 0);
				GLES32.glUniform4fv(BlueLightPositionUniform_pv, 1, light_Blue_position, 0);

				GLES32.glUniform3fv(KaUniform_pv, 1, material_ambient, 0);
				GLES32.glUniform3fv(KdUniform_pv, 1, material_diffuse, 0);
				GLES32.glUniform3fv(KsUniform_pv, 1, material_specular, 0);
				GLES32.glUniform1f(MaterialShininessUniform_pv, material_shininess);
			}
			else
			{
				GLES32.glUniform1i(doubleTapUniform_pv, 0);
			}
			
			light_Red_position[1] = (float)Math.cos((double)angle_RedLight) * 100.0f;
			light_Red_position[2] = (float)Math.sin((double)angle_RedLight) * 100.0f;
	
			light_Green_position[0] = (float)Math.sin((double)angle_GreenLight) * 100.0f;
			light_Green_position[2] = (float)Math.cos((double)angle_GreenLight) * 100.0f;

			light_Blue_position[0] = (float)Math.cos((double)angle_BlueLight) * 100.0f;
			light_Blue_position[1] = (float)Math.sin((double)angle_BlueLight) * 100.0f;
	
		}
		else if(singleTap == 1)
		{

			GLES32.glUseProgram(ShaderProgramObject_pf);
		
			if(doubleTap == 1)
			{
				GLES32.glUniform1i(doubleTapUniform_pf, 1);

				GLES32.glUniform3fv(LaUniformRed_pf, 1, light_Red_ambient, 0);
				GLES32.glUniform3fv(LdUniformRed_pf, 1, light_Red_diffuse, 0);
				GLES32.glUniform3fv(LsUniformRed_pf, 1, light_Red_specular, 0);
				GLES32.glUniform4fv(RedLightPositionUniform_pf, 1, light_Red_position, 0);

				GLES32.glUniform3fv(LaUniformGreen_pf, 1, light_Green_ambient, 0);
				GLES32.glUniform3fv(LdUniformGreen_pf, 1, light_Green_diffuse, 0);
				GLES32.glUniform3fv(LsUniformGreen_pf, 1, light_Green_specular, 0);
				GLES32.glUniform4fv(GreenLightPositionUniform_pf, 1, light_Green_position, 0);

				GLES32.glUniform3fv(LaUniformBlue_pf, 1, light_Blue_ambient, 0);
				GLES32.glUniform3fv(LdUniformBlue_pf, 1, light_Blue_diffuse, 0);
				GLES32.glUniform3fv(LsUniformBlue_pf, 1, light_Blue_specular, 0);
				GLES32.glUniform4fv(BlueLightPositionUniform_pf, 1, light_Blue_position, 0);

				GLES32.glUniform3fv(KaUniform_pf, 1, material_ambient, 0);
				GLES32.glUniform3fv(KdUniform_pf, 1, material_diffuse, 0);
				GLES32.glUniform3fv(KsUniform_pf, 1, material_specular, 0);
				GLES32.glUniform1f(MaterialShininessUniform_pf, material_shininess);
			}
			else
			{
				GLES32.glUniform1i(doubleTapUniform_pf, 0);
			}

			light_Red_position[1] = (float)Math.cos((double)angle_RedLight) * 100.0f;
			light_Red_position[2] = (float)Math.sin((double)angle_RedLight) * 100.0f;
	
			light_Green_position[0] = (float)Math.sin((double)angle_GreenLight) * 100.0f;
			light_Green_position[2] = (float)Math.cos((double)angle_GreenLight) * 100.0f;

			light_Blue_position[0] = (float)Math.cos((double)angle_BlueLight) * 100.0f;
			light_Blue_position[1] = (float)Math.sin((double)angle_BlueLight) * 100.0f;
			
		}
		
        float modelMatrix[]=new float[16];
        float viewMatrix[]=new float[16];
        
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);
        Matrix.translateM(modelMatrix,0,0.0f,0.0f,-2.0f);
        
		if(singleTap == 0)
		{
			GLES32.glUniformMatrix4fv(modelMatrixUniform_pv,1,false,modelMatrix,0);
			GLES32.glUniformMatrix4fv(viewMatrixUniform_pv,1,false,viewMatrix,0);
			GLES32.glUniformMatrix4fv(projectionMatrixUniform_pv,1,false,PerspectiveProjectionMatrix,0);
        }
		else if(singleTap == 1)
		{
			GLES32.glUniformMatrix4fv(modelMatrixUniform_pf,1,false,modelMatrix,0);
			GLES32.glUniformMatrix4fv(viewMatrixUniform_pf,1,false,viewMatrix,0);
			GLES32.glUniformMatrix4fv(projectionMatrixUniform_pf,1,false,PerspectiveProjectionMatrix,0);
		}

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);

		GLES32.glUseProgram(0);
		
		update();

		requestRender();
	}
	
	private void update()
	{
		angle_RedLight = angle_RedLight - 0.05f;
		if (angle_RedLight <= -360.0f)
			angle_RedLight = 0.0f;

		angle_GreenLight = angle_GreenLight - 0.05f;
		if (angle_GreenLight <= -360.0f)
			angle_GreenLight = 0.0f;

		angle_BlueLight = angle_BlueLight - 0.05f;
		if (angle_BlueLight <= -360.0f)
			angle_BlueLight = 0.0f;
	}

	void uninitialize()
	{
		if(vaoSphere[0] != 0)
		{
			GLES32.glDeleteVertexArrays(1, vaoSphere, 0);
			vaoSphere[0] = 0;
		}

		if(vboSpherePosition[0] != 0)
		{
			GLES32.glDeleteBuffers(1, vboSpherePosition, 0);
			vboSpherePosition[0] = 0;
		}

		if(vboSphereNormal[0] != 0)
		{
			GLES32.glDeleteBuffers(1, vboSphereNormal, 0);
			vboSphereNormal[0] = 0;
		}

		if(vboSphereElement[0] != 0)
		{
			GLES32.glDeleteBuffers(1, vboSphereElement, 0);
			vboSphereElement[0] = 0;
		}
		
		if(ShaderProgramObject_pv != 0)
		{
			if(VertexShaderObject_pv != 0)
			{
				GLES32.glDetachShader(ShaderProgramObject_pv, VertexShaderObject_pv);
				GLES32.glDeleteShader(VertexShaderObject_pv);
				VertexShaderObject_pv = 0;
			}
			
			if(FragmentShaderObject_pv != 0)
			{
				GLES32.glDetachShader(ShaderProgramObject_pv, FragmentShaderObject_pv);
				GLES32.glDeleteShader(FragmentShaderObject_pv);
				FragmentShaderObject_pv = 0;
			}
		}

		if(ShaderProgramObject_pv != 0)
		{
			GLES32.glDeleteProgram(ShaderProgramObject_pv);
			ShaderProgramObject_pv = 0;
		}

		if(ShaderProgramObject_pf != 0)
		{
			if(VertexShaderObject_pf != 0)
			{
				GLES32.glDetachShader(ShaderProgramObject_pf, VertexShaderObject_pf);
				GLES32.glDeleteShader(VertexShaderObject_pf);
				VertexShaderObject_pf = 0;
			}
			
			if(FragmentShaderObject_pf != 0)
			{
				GLES32.glDetachShader(ShaderProgramObject_pf, FragmentShaderObject_pf);
				GLES32.glDeleteShader(FragmentShaderObject_pf);
				FragmentShaderObject_pf = 0;
			}
		}

		if(ShaderProgramObject_pf != 0)
		{
			GLES32.glDeleteProgram(ShaderProgramObject_pf);
			ShaderProgramObject_pf = 0;
		}
	}
}
		