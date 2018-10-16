package com.window.vertexfragment.neeraj;

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
import java.nio.ShortBuffer;

import android.opengl.Matrix;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class GLESView extends GLSurfaceView implements GLSurfaceView.Renderer, OnGestureListener, OnDoubleTapListener
{
    private final Context context;
    
    private GestureDetector gestureDetector;
    
    private int vertexShaderObjectPerVertex;
    private int fragmentShaderObjectPerVertex;
    private int shaderProgramObjectPerVertex;

    private int vertexShaderObjectPerFragment;
    private int fragmentShaderObjectPerFragment;
    private int shaderProgramObjectPerFragment;

    private int numElements;
    private int numVertices;
    
    private int[] vao_sphere = new int[1];
    private int[] vbo_sphere_position = new int[1];
    private int[] vbo_sphere_normal = new int[1];
    private int[] vbo_sphere_element = new int[1];

    private float light_ambient[] = {0.0f, 0.0f, 0.0f, 1.0f};
    private float light_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private float light_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private float light_position[] = {100.0f, 100.0f, 100.0f, 1.0f};
    
    private float material_ambient[] = {0.0f, 0.0f, 0.0f, 1.0f};
    private float material_diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private float material_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
    private float material_shininess = 50.0f;
    
    private int  modelMatrixUniformPerVertex, viewMatrixUniformPerVertex, projectionMatrixUniformPerVertex;
    private int  laUniformPerVertex, ldUniformPerVertex, lsUniformPerVertex, lightPositionUniformPerVertex;
    private int  kaUniformPerVertex, kdUniformPerVertex, ksUniformPerVertex, materialShininessUniformPerVertex;

    private int  modelMatrixUniformPerFragment, viewMatrixUniformPerFragment, projectionMatrixUniformPerFragment;
    private int  laUniformPerFragment, ldUniformPerFragment, lsUniformPerFragment, lightPositionUniformPerFragment;
    private int  kaUniformPerFragment, kdUniformPerFragment, ksUniformPerFragment, materialShininessUniformPerFragment;

    private int doubleTapUniformPerVertex;
    private int doubleTapUniformPerFragment;

    private float perspectiveProjectionMatrix[] = new float[16];
    
    private int doubleTap;
    private int singleTap;
    
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
        System.out.println("NRC: OpenGL-ES Version = " + glesVersion);

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
        /******************************** VERTEX SHADER PER FRAGMENT **************************************/
        vertexShaderObjectPerFragment = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        
        //  VS Source Code
        final String vertexShaderSourceCodePerFragment = String.format
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
        
        GLES32.glShaderSource(vertexShaderObjectPerFragment, vertexShaderSourceCodePerFragment);
        
        // shader compilation and error-checking
        GLES32.glCompileShader(vertexShaderObjectPerFragment);
        int[] iShaderCompiledStatus = new int[1];
        int[] iInfoLogLength = new int[1];
        String szInfoLog = null;
        GLES32.glGetShaderiv(vertexShaderObjectPerFragment, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(vertexShaderObjectPerFragment, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(vertexShaderObjectPerFragment);
                System.out.println("NRC: Vertex Shader Compilation Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
           }
        }

        /******************************** FRAGMENT SHADER PER FRAGMENT **************************************/
        fragmentShaderObjectPerFragment = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        
        final String fragmentShaderSourceCodePerFragment = String.format
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
         "phong_ads_color = ambient + diffuse + specular;"	+
         "}"	+
         "else"	+
         "{"	+
         "phong_ads_color = vec3(1.0, 1.0, 1.0);"	+
         "}"	+
         "FragColor = vec4(phong_ads_color, 1.0);"	+
         "}"
        );
        
        GLES32.glShaderSource(fragmentShaderObjectPerFragment, fragmentShaderSourceCodePerFragment);
        
        GLES32.glCompileShader(fragmentShaderObjectPerFragment);
        iShaderCompiledStatus[0] = 0;
        iInfoLogLength[0] = 0;
        szInfoLog = null;
        GLES32.glGetShaderiv(fragmentShaderObjectPerFragment, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(fragmentShaderObjectPerFragment, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(fragmentShaderObjectPerFragment);
                System.out.println("NRC: Fragment Shader Compilation Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }
        
        // Create a shader program
        shaderProgramObjectPerFragment = GLES32.glCreateProgram();
        
        //  attach shaders to the shader program
        GLES32.glAttachShader(shaderProgramObjectPerFragment, vertexShaderObjectPerFragment);
        GLES32.glAttachShader(shaderProgramObjectPerFragment, fragmentShaderObjectPerFragment);
        
        GLES32.glBindAttribLocation(shaderProgramObjectPerFragment, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObjectPerFragment, GLESMacros.NRC_ATTRIBUTE_NORMAL, "vNormal");

        //  Linking
        GLES32.glLinkProgram(shaderProgramObjectPerFragment);
        int[] iShaderProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfoLog = null;
        GLES32.glGetProgramiv(shaderProgramObjectPerFragment, GLES32.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
        if (iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObjectPerFragment, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetProgramInfoLog(shaderProgramObjectPerFragment);
                System.out.println("NRC: Shader Program Link Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }

        //  Uniform Locations
        modelMatrixUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_model_matrix");
        viewMatrixUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_view_matrix");
        projectionMatrixUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_projection_matrix");

        laUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_La");
        ldUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_Ld");
        lsUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_Ls");
        lightPositionUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_light_position");

        kaUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_Ka");
        kdUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_Kd");
        ksUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_Ks");
        materialShininessUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_material_shininess");

        doubleTapUniformPerFragment = GLES32.glGetUniformLocation(shaderProgramObjectPerFragment, "u_double_tap");

        //	****************************** VERTEX SHADER PER VERTEX **********************************
        vertexShaderObjectPerVertex = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        
        //  VS Source Code
        final String vertexShaderSourceCodePerVertex = String.format
        (
         "#version 320 es"  +
         "\n"   +
         "in vec4 vPosition;"   +
         "in vec3 vNormal;" +
         "uniform mat4 u_model_matrix;" +
         "uniform mat4 u_view_matrix;"  +
         "uniform mat4 u_projection_matrix;"    +
         "uniform int u_double_tap;"    +
         "uniform vec3 u_La;"   +
         "uniform vec3 u_Ld;"   +
         "uniform vec3 u_Ls;"   +
         "uniform vec4 u_light_position;"   +
         "uniform vec3 u_Ka;"   +
         "uniform vec3 u_Kd;"   +
         "uniform vec3 u_Ks;"   +
         "uniform float u_material_shininess;"  +
         "out vec3 phong_ads_color;"    +
         "void main(void)"  +
         "{"    +
         "if (u_double_tap == 1)"   +
         "{"    +
         "vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"   +
         "vec3 transformed_normals = normalize(mat3(u_view_matrix * u_model_matrix) * vNormal);"    +
         "vec3 light_direction = normalize(vec3(u_light_position) - eye_coordinates.xyz);"  +
         "float tn_dot_ld = max(dot(transformed_normals, light_direction), 0.0);"   +
         "vec3 ambient = u_La * u_Ka;"  +
         "vec3 diffuse = u_Ld * u_Kd * tn_dot_ld;"  +
         "vec3 reflection_vector = reflect(-light_direction, transformed_normals);" +
         "vec3 viewer_vector = normalize(-eye_coordinates.xyz);"    +
         "vec3 specular = u_Ls * u_Ks * pow(max(dot(reflection_vector, viewer_vector), 0.0), u_material_shininess);"    +
         "phong_ads_color = ambient + diffuse + specular;"    +
         "}"    +
         "else" +
         "{"    +
         "phong_ads_color = vec3(1.0, 1.0, 1.0);"   +
         "}"    +
         "gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"  +
         "}"
        );
        
        GLES32.glShaderSource(vertexShaderObjectPerVertex, vertexShaderSourceCodePerVertex);
        
        // shader compilation and error-checking
        GLES32.glCompileShader(vertexShaderObjectPerVertex);
        iShaderCompiledStatus = new int[1];
        iInfoLogLength = new int[1];
        szInfoLog = null;
        GLES32.glGetShaderiv(vertexShaderObjectPerVertex, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(vertexShaderObjectPerVertex, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(vertexShaderObjectPerVertex);
                System.out.println("NRC: Vertex Shader Compilation Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
           }
        }

        /******************************** FRAGMENT SHADER **************************************/
        fragmentShaderObjectPerVertex = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        
        final String fragmentShaderSourceCodePerVertex = String.format
        (
         "#version 320 es"  +
         "\n"   +
         "precision highp float;"   +
         "in vec3 phong_ads_color;" +
         "out vec4 FragColor;"  +
         "void main(void)"  +
         "{"    +
         "FragColor = vec4(phong_ads_color, 1.0);"  +
         "}"
        );
        
        GLES32.glShaderSource(fragmentShaderObjectPerVertex, fragmentShaderSourceCodePerVertex);
        
        GLES32.glCompileShader(fragmentShaderObjectPerVertex);
        iShaderCompiledStatus[0] = 0;
        iInfoLogLength[0] = 0;
        szInfoLog = null;
        GLES32.glGetShaderiv(fragmentShaderObjectPerVertex, GLES32.GL_COMPILE_STATUS, iShaderCompiledStatus, 0);
        if (iShaderCompiledStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(fragmentShaderObjectPerVertex, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetShaderInfoLog(fragmentShaderObjectPerVertex);
                System.out.println("NRC: Fragment Shader Compilation Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }
        
        // Create a shader program
        shaderProgramObjectPerVertex = GLES32.glCreateProgram();
        
        //  attach shaders to the shader program
        GLES32.glAttachShader(shaderProgramObjectPerVertex, vertexShaderObjectPerVertex);
        GLES32.glAttachShader(shaderProgramObjectPerVertex, fragmentShaderObjectPerVertex);
        
        GLES32.glBindAttribLocation(shaderProgramObjectPerVertex, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObjectPerVertex, GLESMacros.NRC_ATTRIBUTE_NORMAL, "vNormal");

        //  Linking
        GLES32.glLinkProgram(shaderProgramObjectPerVertex);
        iShaderProgramLinkStatus = new int[1];
        iInfoLogLength[0] = 0;
        szInfoLog = null;
        GLES32.glGetProgramiv(shaderProgramObjectPerVertex, GLES32.GL_LINK_STATUS, iShaderProgramLinkStatus, 0);
        if (iShaderProgramLinkStatus[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObjectPerVertex, GLES32.GL_INFO_LOG_LENGTH, iInfoLogLength, 0);
            if (iInfoLogLength[0] > 0)
            {
                szInfoLog = GLES32.glGetProgramInfoLog(shaderProgramObjectPerVertex);
                System.out.println("NRC: Shader Program Link Log = " + szInfoLog);
                uninitialize();
                System.exit(0);
            }
        }

        //  Uniform Locations
        modelMatrixUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_model_matrix");
        viewMatrixUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_view_matrix");
        projectionMatrixUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_projection_matrix");

        laUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_La");
        ldUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_Ld");
        lsUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_Ls");
        lightPositionUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_light_position");

        kaUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_Ka");
        kdUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_Kd");
        ksUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_Ks");
        materialShininessUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_material_shininess");

        doubleTapUniformPerVertex = GLES32.glGetUniformLocation(shaderProgramObjectPerVertex, "u_double_tap");

        //  Sphere initializations
        Sphere sphere = new Sphere();
        float sphere_vertices[] = new float[1146];
        float sphere_normals[] = new float[1146];
        float sphere_textures[] = new float[764];
        short sphere_elements[] = new short[2280];

        sphere.getSphereVertexData(sphere_vertices, sphere_normals, sphere_textures, sphere_elements);
        
        numVertices = sphere.getNumberOfSphereVertices();
        numElements = sphere.getNumberOfSphereElements();

        // SPHERE VAO
        GLES32.glGenVertexArrays(1, vao_sphere,0);
        GLES32.glBindVertexArray(vao_sphere[0]);
        
        // **********Bind VBO Position*****************
        GLES32.glGenBuffers(1, vbo_sphere_position, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_sphere_position[0]);
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sphere_vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(sphere_vertices);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphere_vertices.length * 4, verticesBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_VERTEX, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_VERTEX);
        //  **************Unbind VBO Position*****************
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
        
        //  *****************Bind VBO Normals********************
        GLES32.glGenBuffers(1, vbo_sphere_normal, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_sphere_normal[0]);
        
        byteBuffer = ByteBuffer.allocateDirect(sphere_normals.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        verticesBuffer = byteBuffer.asFloatBuffer();
        verticesBuffer.put(sphere_normals);
        verticesBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphere_normals.length * 4, verticesBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(GLESMacros.NRC_ATTRIBUTE_NORMAL, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(GLESMacros.NRC_ATTRIBUTE_NORMAL);
        //  *****************Unbind VBO Normals**********************
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);
        
        //  ******************Bind VBO Elements***************************
        GLES32.glGenBuffers(1, vbo_sphere_element, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vbo_sphere_element[0]);
        
        byteBuffer = ByteBuffer.allocateDirect(sphere_elements.length * 2);
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer elementsBuffer = byteBuffer.asShortBuffer();    //  ShortBuffer is used here! because elements are taken as short
        elementsBuffer.put(sphere_elements);
        elementsBuffer.position(0);
        
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, sphere_elements.length * 2, elementsBuffer, GLES32.GL_STATIC_DRAW);
        //  ******************Unbind VBO Elements************************
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);

        //  Unbind VAO
        GLES32.glBindVertexArray(0);

        //  OpenGL initializations
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);
        GLES32.glEnable(GLES32.GL_CULL_FACE);
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        doubleTap = 0;
        singleTap = 0;
        
        Matrix.setIdentityM(perspectiveProjectionMatrix,0);
    }
    
    private void resize(int width, int height)
    {
        GLES32.glViewport(0, 0, width, height);
        Matrix.perspectiveM(perspectiveProjectionMatrix, 0, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
    }
    
    public void display()
    {
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        
        if(singleTap == 0)
        {
        	GLES32.glUseProgram(shaderProgramObjectPerVertex);

        	if(doubleTap == 1)
        	{
            	GLES32.glUniform1i(doubleTapUniformPerVertex, 1);
            
            	GLES32.glUniform3fv(laUniformPerVertex, 1, light_ambient, 0);
            	GLES32.glUniform3fv(ldUniformPerVertex, 1, light_diffuse, 0);
            	GLES32.glUniform3fv(lsUniformPerVertex, 1, light_specular, 0);
            	GLES32.glUniform4fv(lightPositionUniformPerVertex, 1, light_position, 0);
            
            	GLES32.glUniform3fv(kaUniformPerVertex, 1, material_ambient, 0);
            	GLES32.glUniform3fv(kdUniformPerVertex, 1, material_diffuse, 0);
            	GLES32.glUniform3fv(ksUniformPerVertex, 1, material_specular, 0);
            	GLES32.glUniform1f(materialShininessUniformPerVertex, material_shininess);
        	}
        	else
        	{
            	GLES32.glUniform1i(doubleTapUniformPerVertex, 0);
        	}

        	float modelMatrix[] = new float[16];
        	float viewMatrix[] = new float[16];
        
        	Matrix.setIdentityM(modelMatrix,0);
        	Matrix.setIdentityM(viewMatrix,0);

        	Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -1.5f);
        
        	GLES32.glUniformMatrix4fv(modelMatrixUniformPerVertex, 1, false, modelMatrix, 0);
        	GLES32.glUniformMatrix4fv(viewMatrixUniformPerVertex, 1, false, viewMatrix, 0);
        	GLES32.glUniformMatrix4fv(projectionMatrixUniformPerVertex, 1, false, perspectiveProjectionMatrix, 0);
        
        	//  Bind with VAO to draw
        	GLES32.glBindVertexArray(vao_sphere[0]);
        
        	GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vbo_sphere_element[0]);
        	GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        	// Unbind VAO
        	GLES32.glBindVertexArray(0);

        	GLES32.glUseProgram(0);
    }
    else
    {
    	GLES32.glUseProgram(shaderProgramObjectPerFragment);

        	if(doubleTap == 1)
        	{
            	GLES32.glUniform1i(doubleTapUniformPerFragment, 1);
            
            	GLES32.glUniform3fv(laUniformPerFragment, 1, light_ambient, 0);
            	GLES32.glUniform3fv(ldUniformPerFragment, 1, light_diffuse, 0);
            	GLES32.glUniform3fv(lsUniformPerFragment, 1, light_specular, 0);
            	GLES32.glUniform4fv(lightPositionUniformPerFragment, 1, light_position, 0);
            
            	GLES32.glUniform3fv(kaUniformPerFragment, 1, material_ambient, 0);
            	GLES32.glUniform3fv(kdUniformPerFragment, 1, material_diffuse, 0);
            	GLES32.glUniform3fv(ksUniformPerFragment, 1, material_specular, 0);
            	GLES32.glUniform1f(materialShininessUniformPerFragment, material_shininess);
        	}
        	else
        	{
            	GLES32.glUniform1i(doubleTapUniformPerFragment, 0);
        	}

        	float modelMatrix[] = new float[16];
        	float viewMatrix[] = new float[16];
        
        	Matrix.setIdentityM(modelMatrix,0);
        	Matrix.setIdentityM(viewMatrix,0);

        	Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -1.5f);
        
        	GLES32.glUniformMatrix4fv(modelMatrixUniformPerFragment, 1, false, modelMatrix, 0);
        	GLES32.glUniformMatrix4fv(viewMatrixUniformPerFragment, 1, false, viewMatrix, 0);
        	GLES32.glUniformMatrix4fv(projectionMatrixUniformPerFragment, 1, false, perspectiveProjectionMatrix, 0);
        
        	//  Bind with VAO to draw
        	GLES32.glBindVertexArray(vao_sphere[0]);
        
        	GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vbo_sphere_element[0]);
        	GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        	// Unbind VAO
        	GLES32.glBindVertexArray(0);

        	GLES32.glUseProgram(0);
    }

        requestRender();
    }
    
    void uninitialize()
    {
        if(vao_sphere[0] != 0)
        {
            GLES32.glDeleteVertexArrays(1, vao_sphere, 0);
            vao_sphere[0] = 0;
        }
        
        if(vbo_sphere_position[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_sphere_position, 0);
            vbo_sphere_position[0] = 0;
        }

        if(vbo_sphere_normal[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_sphere_normal, 0);
            vbo_sphere_normal[0] = 0;
        }
        
        if(vbo_sphere_element[0] != 0)
        {
            GLES32.glDeleteBuffers(1, vbo_sphere_element, 0);
            vbo_sphere_element[0] = 0;
        }

        if(vertexShaderObjectPerVertex != 0)
        {
            GLES32.glDetachShader(shaderProgramObjectPerVertex, vertexShaderObjectPerVertex);
            GLES32.glDeleteShader(vertexShaderObjectPerVertex);
            vertexShaderObjectPerVertex = 0;
        }
            
        if(fragmentShaderObjectPerVertex != 0)
        {
            GLES32.glDetachShader(shaderProgramObjectPerVertex, fragmentShaderObjectPerVertex);
            GLES32.glDeleteShader(fragmentShaderObjectPerVertex);
            fragmentShaderObjectPerVertex = 0;
        }

        if(shaderProgramObjectPerVertex != 0)
        {
            GLES32.glDeleteProgram(shaderProgramObjectPerVertex);
            shaderProgramObjectPerVertex = 0;
        }

        if(vertexShaderObjectPerFragment != 0)
        {
            GLES32.glDetachShader(shaderProgramObjectPerFragment, vertexShaderObjectPerFragment);
            GLES32.glDeleteShader(vertexShaderObjectPerFragment);
            vertexShaderObjectPerFragment = 0;
        }
            
        if(fragmentShaderObjectPerFragment != 0)
        {
            GLES32.glDetachShader(shaderProgramObjectPerFragment, fragmentShaderObjectPerFragment);
            GLES32.glDeleteShader(fragmentShaderObjectPerFragment);
            fragmentShaderObjectPerFragment = 0;
        }

        if(shaderProgramObjectPerFragment != 0)
        {
            GLES32.glDeleteProgram(shaderProgramObjectPerFragment);
            shaderProgramObjectPerFragment = 0;
        }
    }
}