package com.window.materials.neeraj;						

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

	private int VertexShaderObject;
	private int FragmentShaderObject;
	private int ShaderProgramObject;
	
	private int numElements;
	private int numVertices;

	// vao and vbo:
	private int[] vaoSphere = new int[1];
	private int[] vboSpherePosition = new int[1];
	private int[] vboSphereNormal = new int[1];
	private int[] vboSphereElement = new int[1];

	// Rotate light
	private float angle_x_Light = 0.0f;
	private float angle_y_Light = 0.0f;
	private float angle_z_Light = 0.0f;

	private int x_rotation = 0;
	private int y_rotation = 0;
	private int z_rotation = 0;

	private float light_ambient[] = {0.0f, 0.0f, 0.0f, 1.0f};
	private float light_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
	private float light_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
	private float light_position[] = {100.0f, 100.0f, 100.0f, 1.0f};
	
	//MATERIALS OF THE SPHERES:
	//1. EMERALD:
	private float emerald_material_ambient[] = { 0.0215f, 0.1745f, 0.0215f, 1.0f };
	private float emerald_material_diffuse[] = { 0.07568f, 0.61424f, 0.07568f, 1.0f };
	private float emerald_material_specular[] = { 0.633f, 0.727811f, 0.633f, 1.0f };
	private float emerald_material_shininess = 0.6f * 128.0f;

	//2. JADE:
	private float jade_material_ambient[] = { 0.135f, 0.2225f, 0.1575f, 1.0f };
	private float jade_material_diffuse[] = { 0.54f, 0.89f, 0.63f, 1.0f };
	private float jade_material_specular[] = { 0.316228f, 0.316228f, 0.316228f, 1.0f };
	private float jade_material_shininess = 0.1f * 128.0f;

	//3. OBSIDIAN:
	private float obsidian_material_ambient[] = { 0.05375f, 0.05f, 0.06625f, 1.0f };
	private float obsidian_material_diffuse[] = { 0.18275f, 0.17f, 0.22525f, 1.0f };
	private float obsidian_material_specular[] = { 0.332741f, 0.328634f, 0.346435f, 1.0f };
	private float obsidian_material_shininess = 0.3f * 128.0f;

	//4. PEARL:
	private float pearl_material_ambient[] = { 0.25f, 0.20725f, 0.20725f, 1.0f };
	private float pearl_material_diffuse[] = { 1.0f, 0.829f, 0.829f, 1.0f };
	private float pearl_material_specular[] = { 0.296648f, 0.296648f, 0.296648f, 1.0f };
	private float pearl_material_shininess = 0.088f * 128.0f;

	//5. RUBY:
	private float ruby_material_ambient[] = { 0.1745f, 0.01175f, 0.01175f, 1.0f };
	private float ruby_material_diffuse[] = { 0.61424f, 0.04136f, 0.04136f, 1.0f };
	private float ruby_material_specular[] = { 0.727811f, 0.626959f, 0.626959f, 1.0f };
	private float ruby_material_shininess = 0.6f * 128.0f;

	//6. TURQUOISE:
	private float turquoise_material_ambient[] = { 0.1f, 0.18725f, 0.1745f, 1.0f };
	private float turquoise_material_diffuse[] = { 0.396f, 0.74151f, 0.69102f, 1.0f };
	private float turquoise_material_specular[] = { 0.297254f, 0.30829f, 0.306678f, 1.0f };
	private float turquoise_material_shininess = 0.1f * 128.0f;

	//7. BRASS:
	private float brass_material_ambient[] = { 0.329412f, 0.223529f, 0.027451f, 1.0f };
	private float brass_material_diffuse[] = { 0.780392f, 0.568627f, 0.113725f, 1.0f };
	private float brass_material_specular[] = { 0.992157f, 0.941176f, 0.807843f, 1.0f };
	private float brass_material_shininess = 0.21794872f * 128.0f;

	//8. BRONZE:
	private float bronze_material_ambient[] = { 0.2125f, 0.1275f, 0.054f, 1.0f };
	private float bronze_material_diffuse[] = { 0.714f, 0.4284f, 0.18144f, 1.0f };
	private float bronze_material_specular[] = { 0.393548f, 0.271906f, 0.166721f, 1.0f };
	private float bronze_material_shininess = 0.2f * 128.0f ;

	//9. CHROME:
	private float chrome_material_ambient[] = { 0.25f, 0.25f, 0.25f, 1.0f };
	private float chrome_material_diffuse[] = { 0.4f, 0.4f, 0.4f, 1.0f };
	private float chrome_material_specular[] = { 0.774597f, 0.774597f, 0.774597f, 1.0f };
	private float chrome_material_shininess = 0.6f * 128.0f;

	//10. COPPER:
	private float copper_material_ambient[] = { 0.19125f, 0.0735f, 0.0225f, 1.0f };
	private float copper_material_diffuse[] = { 0.7038f, 0.27048f, 0.0828f, 1.0f };
	private float copper_material_specular[] = { 0.256777f, 0.137622f, 0.086014f, 1.0f };
	private float copper_material_shininess = 0.1f * 128.0f;

	//11. GOLD:
	private float gold_material_ambient[] = { 0.24725f, 0.1995f, 0.0745f, 1.0f };
	private float gold_material_diffuse[] = { 0.75164f, 0.60648f, 0.22648f, 1.0f };
	private float gold_material_specular[] = { 0.628281f, 0.555802f, 0.366065f, 1.0f };
	private float gold_material_shininess = 0.4f * 128.0f;

	//12. SILVER:
	private float silver_material_ambient[] = { 0.19225f, 0.19225f, 0.19225f, 1.0f };
	private float silver_material_diffuse[] = { 0.50754f, 0.50754f, 0.50754f, 1.0f };
	private float silver_material_specular[] = { 0.508273f, 0.508273f, 0.508273f, 1.0f };
	private float silver_material_shininess = 0.4f * 128.0f;

	//13. BLACK:
	private float black_material_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float black_material_diffuse[] = { 0.01f, 0.01f, 0.01f, 1.0f };
	private float black_material_specular[] = { 0.50f, 0.50f, 0.50f, 1.0f };
	private float black_material_shininess = 0.25f * 128.0f;

	//14. CYAN:
	private float cyan_material_ambient[] = { 0.0f, 0.1f, 0.06f, 1.0f };
	private float cyan_material_diffuse[] = { 0.0f, 0.50980392f, 0.50980392f, 1.0f };
	private float cyan_material_specular[] = { 0.50196078f, 0.50196078f, 0.50196078f, 1.0f };
	private float cyan_material_shininess = 0.25f * 128.0f;

	//15. GREEN:
	private float green_material_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float green_material_diffuse[] = { 0.1f, 0.35f, 0.1f, 1.0f };
	private float green_material_specular[] = { 0.45f, 0.55f, 0.45f, 1.0f };
	private float green_material_shininess = 0.25f * 128.0f;

	//16. RED:
	private float red_material_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float red_material_diffuse[] = { 0.5f, 0.0f, 0.0f, 1.0f };
	private float red_material_specular[] = { 0.7f, 0.6f, 0.6f, 1.0f };
	private float red_material_shininess = 0.25f * 128.0f;

	//17. WHITE:
	private float white_material_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float white_material_diffuse[] = { 0.55f, 0.55f, 0.55f, 1.0f };
	private float white_material_specular[] = { 0.70f, 0.70f, 0.70f, 1.0f };
	private float white_material_shininess = 0.25f * 128.0f;

	//18. YELLOW PLASTIC:
	private float yellow_plastic_material_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float yellow_plastic_material_diffuse[] = { 0.5f, 0.5f, 0.0f, 1.0f };
	private float yellow_plastic_material_specular[] = { 0.60f, 0.60f, 0.50f, 1.0f };
	private float yellow_plastic_material_shininess = 0.25f * 128.0f;

	//19. BLACK:
	private float black_plastic_material_ambient[] = { 0.02f, 0.02f, 0.02f, 1.0f };
	private float black_plastic_material_diffuse[] = { 0.01f, 0.01f, 0.01f, 1.0f };
	private float black_plastic_material_specular[] = { 0.4f, 0.4f, 0.4f, 1.0f };
	private float black_plastic_material_shininess = 0.078125f * 128.0f;

	//20. CYAN:
	private float cyan_plastic_material_ambient[] = { 0.0f, 0.05f, 0.05f, 1.0f };
	private float cyan_plastic_material_diffuse[] = { 0.4f, 0.5f, 0.5f, 1.0f };
	private float cyan_plastic_material_specular[] = { 0.04f, 0.7f, 0.7f, 1.0f };
	private float cyan_plastic_material_shininess = 0.078125f * 128.0f;

	//21. GREEN:
	private float green_plastic_material_ambient[] = { 0.0f, 0.05f, 0.0f, 1.0f };
	private float green_plastic_material_diffuse[] = { 0.4f, 0.5f, 0.4f, 1.0f };
	private float green_plastic_material_specular[] = { 0.04f, 0.7f, 0.04f, 1.0f };
	private float green_plastic_material_shininess = 0.078125f * 128.0f;

	//22. RED:
	private float red_plastic_material_ambient[] = { 0.05f, 0.0f, 0.0f, 1.0f };
	private float red_plastic_material_diffuse[] = { 0.5f, 0.4f, 0.4f, 1.0f };
	private float red_plastic_material_specular[] = { 0.7f, 0.04f, 0.04f, 1.0f };
	private float red_plastic_material_shininess = 0.078125f * 128.0f;

	//23. WHITE:
	private float white_plastic_material_ambient[] = { 0.05f, 0.05f, 0.05f, 1.0f };
	private float white_plastic_material_diffuse[] = { 0.5f, 0.5f, 0.5f, 1.0f };
	private float white_plastic_material_specular[] = { 0.7f, 0.7f, 0.7f, 1.0f };
	private float white_plastic_material_shininess = 0.078125f * 128.0f;

	//24. YELLOW RUBBER
	private float yellow_rubber_material_ambient[] = { 0.05f, 0.05f, 0.0f, 1.0f };
	private float yellow_rubber_material_diffuse[] = { 0.5f, 0.5f, 0.4f, 1.0f };
	private float yellow_rubber_material_specular[] = { 0.7f, 0.7f, 0.04f, 1.0f };
	private float yellow_rubber_material_shininess = 0.078125f * 128.0f;

	private int modelMatrixUniform, viewMatrixUniform, projectionMatrixUniform;
	private int laUniform, ldUniform, lsUniform, lightPositionUniform;
	private int kaUniform, kdUniform, ksUniform, materialShininessUniform;
	
	private int doubleTapUniform;
	
	private int doubleTap;
	private int singleTap;
	
	private float PerspectiveProjectionMatrix[] = new float[16];		// 4x4 matrix
	
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
		if(singleTap > 3)
			singleTap = 0;
		
		if(singleTap == 0)
		{
			x_rotation = 0;
			y_rotation = 0;
			z_rotation = 0;
		}
		else if(singleTap == 1)
		{
			x_rotation = 1;
			y_rotation = 0;
			z_rotation = 0;
		}
		else if(singleTap == 2)
		{
			x_rotation = 0;
			y_rotation = 1;
			z_rotation = 0;
		}
		else if(singleTap == 3)
		{
			x_rotation = 0;
			y_rotation = 0;
			z_rotation = 1;
		}
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
		/*------------------- VERTEX SHADER -----------------------*/
		
		VertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
		
		final String VertexShaderSourceCode = String.format
        (
         "#version 320 es"	+
         "\n"	+
         "in vec4 vPosition;"	+
         "in vec3 vNormal;"	+
         "uniform mat4 u_model_matrix;"	+
         "uniform mat4 u_view_matrix;"	+
         "uniform mat4 u_projection_matrix;"	+
         "uniform mediump int u_double_tap;"	+
         "uniform vec4 u_light_position;"	+
         "out vec3 transformed_normals;"	+
         "out vec3 light_direction;"	+
         "out vec3 viewer_vector;"	+
         "void main(void)"	+
         "{"	+
         "if (u_double_tap == 1)"	+
         "{"	+
         "vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	+
         "transformed_normals = mat3(u_view_matrix * u_model_matrix) * vNormal;"	+
         "light_direction = vec3(u_light_position) - eye_coordinates.xyz;"	+
         "viewer_vector = -eye_coordinates.xyz;"	+
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
				System.out.println("NRC : (Per Vertex) Vertex Shader Compilation Log = "+szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		/*---------------------------------- FRAGMENT SHADER --------------------------*/
		
		FragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
		
        final String FragmentShaderSourceCode = String.format
        (
         "#version 320 es"	+
         "\n"	+
         "precision highp float;"	+
         "in vec3 transformed_normals;"	+
         "in vec3 light_direction;"	+
         "in vec3 viewer_vector;"	+
         "out vec4 FragColor;"	+
         "uniform vec3 u_La;"	+
         "uniform vec3 u_Ld;"	+
         "uniform vec3 u_Ls;"	+
         "uniform vec3 u_Ka;"	+
         "uniform vec3 u_Kd;"	+
         "uniform vec3 u_Ks;"	+
         "uniform float u_material_shininess;"	+
         "uniform int u_double_tap;"	+
         "void main(void)"	+
         "{"	+
         "vec3 phong_ads_color;"	+
         "if(u_double_tap == 1)"	+
         "{"	+
         "vec3 normalized_transformed_normals = normalize(transformed_normals);"	+
         "vec3 normalized_light_direction = normalize(light_direction);"	+
         "vec3 normalized_viewer_vector = normalize(viewer_vector);"	+
         "vec3 ambient = u_La * u_Ka;"	+
         "float tn_dot_ld = max(dot(normalized_transformed_normals, normalized_light_direction), 0.0);"	+
         "vec3 diffuse = u_Ld * u_Kd * tn_dot_ld;"	+
         "vec3 reflection_vector = reflect(-normalized_light_direction, normalized_transformed_normals);"	+
         "vec3 specular = u_Ls * u_Ks * pow(max(dot(reflection_vector, normalized_viewer_vector), 0.0), u_material_shininess);"	+
         "phong_ads_color=ambient + diffuse + specular;"	+
         "}"	+
         "else"	+
         "{"	+
         "phong_ads_color = vec3(1.0, 1.0, 1.0);"	+
         "}"	+
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
				System.out.println("NRC : (Per Vertex) Fragment shader compilation log = " + szInfoLog);
				uninitialize();
				System.exit(0);
			}
		}
		
		ShaderProgramObject = GLES32.glCreateProgram();

		GLES32.glAttachShader(ShaderProgramObject, VertexShaderObject);
		GLES32.glAttachShader(ShaderProgramObject, FragmentShaderObject);
		GLES32.glBindAttribLocation(ShaderProgramObject, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
		GLES32.glBindAttribLocation(ShaderProgramObject, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vNormal");
		
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
		
		//	Uniforms
        modelMatrixUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_model_matrix");
        viewMatrixUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_view_matrix");
        projectionMatrixUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_projection_matrix");
        doubleTapUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_double_tap");
        laUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_La");
        ldUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ld");
        lsUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ls");
        lightPositionUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_light_position");
        kaUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ka");
        kdUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Kd");
        ksUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_Ks");
        materialShininessUniform = GLES32.glGetUniformLocation(ShaderProgramObject, "u_material_shininess");

        Sphere sphere = new Sphere();
        float sphereVertices[] = new float[1146];
        float sphereNormals[] = new float[1146];
        float sphereTextures[] = new float[764];
        short sphereElements[] = new short[2280];
        sphere.getSphereVertexData(sphereVertices, sphereNormals, sphereTextures, sphereElements);
        numVertices = sphere.getNumberOfSphereVertices();
        numElements = sphere.getNumberOfSphereElements();

										
		// A. VAO for SPHERE:
		GLES32.glGenVertexArrays(1, vaoSphere, 0);		
		GLES32.glBindVertexArray(vaoSphere[0]);		
		
		// 1. BUFFER BLOCK FOR VERTICES:
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
		
		
		// VBO Elements
        GLES32.glGenBuffers(1,vboSphereElement,0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,vboSphereElement[0]);
        
		ByteBuffer SphereElementByteBuffer = ByteBuffer.allocateDirect(sphereElements.length * 4);
		SphereElementByteBuffer.order(ByteOrder.nativeOrder());
		ShortBuffer SphereElementBuffer = SphereElementByteBuffer.asShortBuffer();
		SphereElementBuffer.put(sphereElements);
		SphereElementBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, sphereElements.length * 2, SphereElementBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,0);

		GLES32.glBindVertexArray(0);
		
		GLES32.glEnable(GLES32.GL_DEPTH_TEST);
		GLES32.glDepthFunc(GLES32.GL_LEQUAL);
		GLES32.glEnable(GLES32.GL_CULL_FACE);
		GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
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
		
		GLES32.glUseProgram(ShaderProgramObject);
		
		if(doubleTap == 1)
		{
			GLES32.glUniform1i(doubleTapUniform, 1);

            GLES32.glUniform3fv(laUniform, 1, light_ambient, 0);
            GLES32.glUniform3fv(ldUniform, 1, light_diffuse, 0);
            GLES32.glUniform3fv(lsUniform, 1, light_specular, 0);
            GLES32.glUniform4fv(lightPositionUniform, 1, light_position, 0);
            
		}
		else
		{
			GLES32.glUniform1i(doubleTapUniform, 0);
        }

		if(x_rotation == 1)
		{
			light_position[0] = (float)Math.sin((double)angle_x_Light) * 100.0f;
			light_position[2] = (float)Math.cos((double)angle_x_Light) * 100.0f;
			light_position[1] = 0.0f;
		}
		
		if(y_rotation == 1)
		{
			light_position[1] = (float)Math.cos((double)angle_y_Light) * 100.0f;
			light_position[2] = (float)Math.sin((double)angle_y_Light) * 100.0f;
			light_position[0] = 0.0f;

		}
		
		if(z_rotation == 1)
		{
			light_position[0] = (float)Math.cos((double)angle_z_Light) * 100.0f;
			light_position[1] = (float)Math.sin((double)angle_z_Light) * 100.0f;
			light_position[2] = 0.0f;			
		}
		
		float modelMatrix[] = new float[16];
        float viewMatrix[] = new float[16];
		
		// SPHERES:

        // 1st sphere on 1st column:
		GLES32.glUniform3fv(kaUniform, 1, emerald_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, emerald_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, emerald_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, emerald_material_shininess);
		    
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-3.0f,3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        //	Bind VAO
        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------------*/
		
		// 2nd sphere on 1st column:
		GLES32.glUniform3fv(kaUniform, 1, jade_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, jade_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, jade_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, jade_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-3.0f,1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------------*/
		
		// 3rd sphere on 1st column:

		GLES32.glUniform3fv(kaUniform, 1, obsidian_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, obsidian_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, obsidian_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, obsidian_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-3.0f, 0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*-------------------------------------------------------------------------------------*/
		
		// 4th sphere on 1st column:
		GLES32.glUniform3fv(kaUniform, 1, pearl_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, pearl_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, pearl_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, pearl_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-3.0f, -0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 5th sphere on 1st column:
		GLES32.glUniform3fv(kaUniform, 1, ruby_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, ruby_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, ruby_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, ruby_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-3.0f, -1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 6th sphere on 1st column:

		GLES32.glUniform3fv(kaUniform, 1, turquoise_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, turquoise_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, turquoise_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, turquoise_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-3.0f, -3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        

        GLES32.glBindVertexArray(vaoSphere[0]);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// COLUMN 2:
		// 1st sphere on 2nd column:

		GLES32.glUniform3fv(kaUniform, 1, brass_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, brass_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, brass_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, brass_material_shininess);
		    
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-1.0f,3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        

        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*-----------------------------------------------------------------------*/
		
		// 2nd sphere on 2nd column:

		GLES32.glUniform3fv(kaUniform, 1, bronze_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, bronze_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, bronze_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, bronze_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-1.0f,1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*-----------------------------------------------------------------------*/
		
		// 3rd sphere on 2nd column:
		GLES32.glUniform3fv(kaUniform, 1, chrome_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, chrome_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, chrome_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, chrome_material_shininess);
		    
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-1.0f, 0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 4th sphere on 2nd column:
		GLES32.glUniform3fv(kaUniform, 1, copper_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, copper_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, copper_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, copper_material_shininess);
		    
 
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-1.0f, -0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 5th sphere on 2nd column:
		GLES32.glUniform3fv(kaUniform, 1, gold_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, gold_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, gold_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, gold_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-1.0f, -1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 6th sphere on 2nd column:

		GLES32.glUniform3fv(kaUniform, 1, silver_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, silver_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, silver_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, silver_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,-1.0f, -3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
 
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// COLUMN 3:
		// 1st sphere on 3rd column:
		GLES32.glUniform3fv(kaUniform, 1, black_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, black_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, black_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, black_material_shininess);
		    
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,1.0f,3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*-----------------------------------------------------------------------*/
		
		// 2nd sphere on 3rd column:
		GLES32.glUniform3fv(kaUniform, 1, cyan_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, cyan_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, cyan_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, cyan_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,1.0f,1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
 
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*-----------------------------------------------------------------------*/
		
		// 3rd sphere on 3rd column:
		GLES32.glUniform3fv(kaUniform, 1, green_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, green_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, green_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, green_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0, 1.0f, 0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);
 
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 4th sphere on 3rd column:
		GLES32.glUniform3fv(kaUniform, 1, red_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, red_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, red_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, red_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,1.0f, -0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
 
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 5th sphere on 3rd column:
		GLES32.glUniform3fv(kaUniform, 1, white_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, white_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, white_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, white_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,1.0f, -1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 6th sphere on 3rd column:
		GLES32.glUniform3fv(kaUniform, 1, yellow_plastic_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, yellow_plastic_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, yellow_plastic_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, yellow_plastic_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,1.0f, -3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// COLUMN 4:
		// 1st sphere on 4th column:
		GLES32.glUniform3fv(kaUniform, 1, black_plastic_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, black_plastic_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, black_plastic_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, black_plastic_material_shininess);
		    
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0, 3.0f,3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*-----------------------------------------------------------------------*/
		
		// 2nd sphere on 4th column:
		GLES32.glUniform3fv(kaUniform, 1, cyan_plastic_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, cyan_plastic_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, cyan_plastic_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, cyan_plastic_material_shininess);
		    
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0, 3.0f,1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*-----------------------------------------------------------------------*/
		
		// 3rd sphere on 4th column:
		GLES32.glUniform3fv(kaUniform, 1, green_plastic_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, green_plastic_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, green_plastic_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, green_plastic_material_shininess);
		    
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0, 3.0f, 0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);

        GLES32.glBindVertexArray(vaoSphere[0]);

        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 4th sphere on 4th column:
		GLES32.glUniform3fv(kaUniform, 1, red_plastic_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, red_plastic_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, red_plastic_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, red_plastic_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0,3.0f, -0.6f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 5th sphere on 4th column:
		GLES32.glUniform3fv(kaUniform, 1, white_plastic_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, white_plastic_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, white_plastic_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, white_plastic_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0, 3.0f, -1.8f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/
		
		// 6th sphere on 4th column:
		GLES32.glUniform3fv(kaUniform, 1, yellow_rubber_material_ambient, 0);
		GLES32.glUniform3fv(kaUniform, 1, yellow_rubber_material_diffuse, 0);
		GLES32.glUniform3fv(ksUniform, 1, yellow_rubber_material_specular, 0);
		GLES32.glUniform1f(materialShininessUniform, yellow_rubber_material_shininess);

        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix,0, 3.0f, -3.0f,-10.0f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform,1,false,modelMatrix,0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform,1,false,viewMatrix,0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform,1,false,PerspectiveProjectionMatrix,0);
        
        GLES32.glBindVertexArray(vaoSphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vboSphereElement[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        GLES32.glBindVertexArray(0);
		/*----------------------------------------------------------------------*/

		GLES32.glUseProgram(0);
		
		update();
		
		requestRender();
	}
	
	private void update()
	{

		if (x_rotation == 1)
		{
			angle_x_Light = angle_x_Light - 0.05f;
			angle_y_Light = 0.0f;
			angle_z_Light = 0.0f;
		}

		if (y_rotation == 1)
		{
			angle_y_Light = angle_y_Light - 0.05f;
			angle_x_Light = 0.0f;
			angle_z_Light = 0.0f;
		}

		if (z_rotation == 1)
		{
			angle_z_Light = angle_z_Light - 0.05f;
			angle_x_Light = 0.0f;
			angle_y_Light = 0.0f;
		}
		
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
		