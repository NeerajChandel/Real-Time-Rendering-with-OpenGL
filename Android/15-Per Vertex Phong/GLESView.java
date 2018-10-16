package com.window.pervertexphong.neeraj;

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
    
    private int vertexShaderObject;
    private int fragmentShaderObject;
    private int shaderProgramObject;

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
    
    private int  modelMatrixUniform, viewMatrixUniform, projectionMatrixUniform;
    private int  laUniform, ldUniform, lsUniform, lightPositionUniform;
    private int  kaUniform, kdUniform, ksUniform, materialShininessUniform;

    private int doubleTapUniform;

    private float perspectiveProjectionMatrix[] = new float[16];
    
    private int doubleTap;
    
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
        /******************************** VERTEX SHADER **************************************/
        vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        
        //  VS Source Code
        final String vertexShaderSourceCode = String.format
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
        
        GLES32.glShaderSource(vertexShaderObject, vertexShaderSourceCode);
        
        // shader compilation and error-checking
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

        /******************************** FRAGMENT SHADER **************************************/
        fragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        
        final String fragmentShaderSourceCode = String.format
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
        
        GLES32.glShaderSource(fragmentShaderObject, fragmentShaderSourceCode);
        
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
        
        // Create a shader program
        shaderProgramObject=GLES32.glCreateProgram();
        
        //  attach shaders to the shader program
        GLES32.glAttachShader(shaderProgramObject, vertexShaderObject);
        GLES32.glAttachShader(shaderProgramObject, fragmentShaderObject);
        
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
        GLES32.glBindAttribLocation(shaderProgramObject, GLESMacros.NRC_ATTRIBUTE_NORMAL, "vNormal");

        //  Linking
        GLES32.glLinkProgram(shaderProgramObject);
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

        //  Uniform Locations
        modelMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_model_matrix");
        viewMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_view_matrix");
        projectionMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_projection_matrix");

        laUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_La");
        ldUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ld");
        lsUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ls");
        lightPositionUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_light_position");

        kaUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ka");
        kdUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Kd");
        ksUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ks");
        materialShininessUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_material_shininess");

        doubleTapUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_double_tap");

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
        
        GLES32.glUseProgram(shaderProgramObject);

        if(doubleTap == 1)
        {
            GLES32.glUniform1i(doubleTapUniform, 1);
            
            GLES32.glUniform3fv(laUniform, 1, light_ambient, 0);
            GLES32.glUniform3fv(ldUniform, 1, light_diffuse, 0);
            GLES32.glUniform3fv(lsUniform, 1, light_specular, 0);
            GLES32.glUniform4fv(lightPositionUniform, 1, light_position, 0);
            
            GLES32.glUniform3fv(kaUniform, 1, material_ambient, 0);
            GLES32.glUniform3fv(kdUniform, 1, material_diffuse, 0);
            GLES32.glUniform3fv(ksUniform, 1, material_specular, 0);
            GLES32.glUniform1f(materialShininessUniform, material_shininess);
        }
        else
        {
            GLES32.glUniform1i(doubleTapUniform, 0);
        }

        float modelMatrix[] = new float[16];
        float viewMatrix[] = new float[16];
        
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.setIdentityM(viewMatrix,0);

        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -1.5f);
        
        GLES32.glUniformMatrix4fv(modelMatrixUniform, 1, false, modelMatrix, 0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform, 1, false, viewMatrix, 0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform, 1, false, perspectiveProjectionMatrix, 0);
        
        //  Bind with VAO to draw
        GLES32.glBindVertexArray(vao_sphere[0]);
        
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vbo_sphere_element[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);
        
        // Unbind VAO
        GLES32.glBindVertexArray(0);

        GLES32.glUseProgram(0);

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

        if(shaderProgramObject != 0)
        {
            GLES32.glDeleteProgram(shaderProgramObject);
            shaderProgramObject = 0;
        }
    }
}