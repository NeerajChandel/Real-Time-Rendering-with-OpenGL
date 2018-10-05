/*
*   Program:    Three Lights in WebGL 2.0
*/

//  global variables
var canvas      = null; 
var gl          = null; //  This is our context
var bFullscreen = false;
var canvas_original_width;
var canvas_original_height;

//  OpenGL variables

const WebGLMacros = 
{
    NRC_ATTRIBUTE_VERTEX : 0,
    NRC_ATTRIBUTE_COLOR : 1,
    NRC_ATTRIBUTE_NORMAL : 2,
    NRC_ATTRIBUTE_TEXTURE0 : 3,
};

var bLKeyPressed = false;

var vertexShaderObject;
var fragmentShaderObject;
var shaderProgramObject;

//  projection matrix variable
var perspectiveProjectionMatrix;

var light_ambient_red = [0.0, 0.0, 0.0];
var light_diffuse_red = [1.0, 0.0, 0.0];
var light_specular_red = [1.0, 0.0, 0.0];
var light_position_red = [0.0, 0.0, 0.0, 0.0];

var light_ambient_blue = [0.0, 0.0, 0.0];
var light_diffuse_blue = [0.0, 0.0, 1.0];
var light_specular_blue = [0.0, 0.0, 1.0];
var light_position_blue = [0.0, 0.0, 0.0, 0.0];

var light_ambient_green = [0.0, 0.0, 0.0];
var light_diffuse_green = [0.0, 1.0, 0.0];
var light_specular_green = [0.0, 1.0, 0.0];
var light_position_green = [0.0, 0.0, 0.0, 0.0];

var material_ambient = [0.0, 0.0, 0.0];
var material_diffuse = [1.0, 1.0, 1.0];
var material_specular = [1.0, 1.0, 1.0];
var material_shininess = 100.0;

var degrees;
var angle_red = 0.0;
var angle_blue = 0.0;
var angle_green = 0.0;

var sphere = null;

var modelMatrixUniform, viewMatrixUniform, projectionMatrixUniform;
var laUniform_red, ldUniform_red, lsUniform_red, lightPositionUniform_red;
var laUniform_blue, ldUniform_blue, lsUniform_blue, lightPositionUniform_blue;
var kaUniform, kdUniform, ksUniform, materialShininessUniform;
var LKeyPressedUniform;

//  To make requestAnimationFrame cross-browser compatible
//  This is to start animation
//  the var requestAnimationFrame is a function pointer
var requestAnimationFrame = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame;

//  To make cancelAnimationFrame cross-browser compatible
//  This is to stop animation
//  This var is a function pointer as well
var cancelAnimationFrame = window.cancelAnimationFrame || window.webkitCancelRequestAnimationFrame || window.webkitCancelAnimationFrame || window.mozCancelRequestAnimationFrame || window.mozCancelAnimationFrame || window.oCancelRequestAnimationFrame || window.oCancelAnimationFrame || window.msCancelRequestAnimationFrame || msCancelAnimationFrame;

//  onload function
function main()
{
    //  get canvas element
    canvas = document.getElementById("NRC");
    //  error checking
    if(!canvas)
        console.log("Could not obtain canvas.\n");
    else
        console.log("Canvas was obtained successfully.\n");

    canvas_original_width   = canvas.width;
    canvas_original_height  = canvas.height;
    
    //  register keydown event handler
    window.addEventListener("keydown", keyDownEvent, false);
    window.addEventListener("click", mouseDownEvent, true);
    window.addEventListener("resize", resize, false);

    //  initialize WebGL
    init();
    //  start drawing here as a warm-up
    resize();
    draw();

}

function toggleFullscreen()
{
    // get fullscreen element of whichever browser you're using
    var fullscreen_element = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement || null;

    // if not fullscreen
    if(fullscreen_element == null)
    {
        if(canvas.requestFullscreen)
            canvas.requestFullscreen();
        else if(canvas.mozRequestFullScreen)
            canvas.mozRequestFullScreen();
        else if(canvas.webkitRequestFullscreen)
            canvas.webkitRequestFullscreen();
        else if(canvas.msRequestFullscreen)
            canvas.msRequestFullscreen();
        //  set bFullscreen to true
        bFullscreen = true;
    }
    // if already fullscreen
    else 
    {
        if(document.exitFullscreen)
            document.exitFullscreen();
        else if(document.mozCancelFullScreen)
            document.mozCancelFullScreen();
        else if(document.webkitExitFullscreen)
            document.webkitExitFullscreen();
        else if(document.msExitFullscreen)
            document.msExitFullscreen();
        //  set bFullscreen to false
        bFullscreen = false;
    }
}

function init()
{
    //  get WebGL 2.0 context
    //  our context is in the 'gl' variable
    //  The entire OpenGL state machine is handled by the
    //  WebGL context (gl) variable
    gl = canvas.getContext("webgl2");
    //  error checking for gl
    if(gl == null)
    {
        console.log("Could not get the WebGL rendering context.\n");
        return;
    }
    else
    {
    	console.log("WebGL 2.0 Context was obtained successfully.\n");
    }

    gl.viewportWidth    = canvas.width;
    gl.viewportHeight   = canvas.height;

    /*--------------------------- VERTEX SHADER ------------------------*/
    
    //  NOTE: version 320 in OpenGL ES is version 300 in WebGL
    //  write shader source code:
    var vertexShaderSourceCode = 
        "#version 300 es"	+
         "\n"	+
         "in vec4 vPosition;"	+
         "in vec3 vNormal;"	+
         "uniform mat4 u_model_matrix;"	+
         "uniform mat4 u_view_matrix;"	+
         "uniform mat4 u_projection_matrix;"	+
         "uniform mediump int u_LKeyPressed;"	+
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
         "if(u_LKeyPressed == 1)"	+
         "{"	+
         "vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	+
         "transformed_normals = mat3(u_view_matrix * u_model_matrix) * vNormal;"	+
         "light_direction_Red = vec3(u_light_position_Red) - eye_coordinates.xyz;"	+
         "light_direction_Green = vec3(u_light_position_Green) - eye_coordinates.xyz;"	+
         "light_direction_Blue = vec3(u_light_position_Blue) - eye_coordinates.xyz;"	+
         "viewer_vector = -eye_coordinates.xyz;"	+
         "}"	+
         "gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"	+
         "}";

    //  1. create shader object -> 2. send shader source to object -> 3. compile shader
    vertexShaderObject = gl.createShader(gl.VERTEX_SHADER); //  1.
    gl.shaderSource(vertexShaderObject, vertexShaderSourceCode);    //  2.
    gl.compileShader(vertexShaderObject);   //  3.

    //  error checking for compilation of vertex shader
    if(gl.getShaderParameter(vertexShaderObject, gl.COMPILE_STATUS) == false)
    {
        var error = gl.getShaderInfoLog(vertexShaderObject);
        
        if(error.length > 0)
        {
            alert(error);
            uninitialize();
        }
    }

    /*--------------------------- FRAGMENT SHADER ------------------------*/

    //  write shader source
    var fragmentShaderSourceCode = 
    "#version 300 es"	+
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
    //"uniform vec4 u_light_position_Red;"	+
    "uniform vec3 u_La_Green;"	+
    "uniform vec3 u_Ld_Green;"	+
    "uniform vec3 u_Ls_Green;"	+
    //"uniform vec4 u_light_position_Green;"	+
    "uniform vec3 u_La_Blue;"	+
    "uniform vec3 u_Ld_Blue;"	+
    "uniform vec3 u_Ls_Blue;"	+
    //"uniform vec4 u_light_position_Blue;"	+
    "uniform vec3 u_Ka;"	+
    "uniform vec3 u_Kd;"	+
    "uniform vec3 u_Ks;"	+
    "uniform float u_material_shininess;"	+
    "vec3 phong_ads_color_Red;"	+
    "vec3 phong_ads_color_Green;"	+
    "vec3 phong_ads_color_Blue;"	+
    "uniform int u_LKeyPressed;"	+
    "void main(void)"	+
    "{"	+
    "vec3 phong_ads_color;"	+
    "if(u_LKeyPressed == 1)"	+
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
    "phong_ads_color = phong_ads_color_Red + phong_ads_color_Green + phong_ads_color_Blue;"	+      
    "}"	+
    "else"	+
    "{"	+
    "phong_ads_color = vec3(1.0, 1.0, 1.0);"	+
    "}"	+
    "FragColor = vec4(phong_ads_color, 1.0);"	+
    "}";

    //  1. create shader object -> 2. send shader source to object -> 3. compile shader
    fragmentShaderObject = gl.createShader(gl.FRAGMENT_SHADER); //  1.
    gl.shaderSource(fragmentShaderObject, fragmentShaderSourceCode); //  2.
    gl.compileShader(fragmentShaderObject); //  3.

    //  error checking for compilation of fragment shader
    if(gl.getShaderParameter(fragmentShaderObject, gl.COMPILE_STATUS) == false)
    {
        var error = gl.getShaderInfoLog(fragmentShaderObject);
        
        if(error.length > 0)
        {
            alert(error);
            uninit();
        }
    }

    /*--------------------------- SHADER PROGRAM OBJECT ------------------------*/

    //  create program object
    shaderProgramObject = gl.createProgram();
    //  attach vertex shader object and fragment shader object to shader program object
    gl.attachShader(shaderProgramObject, vertexShaderObject);
    gl.attachShader(shaderProgramObject, fragmentShaderObject);

    //  pre-link binding of shaderProgramObject with vertex shader attributes
    gl.bindAttribLocation(shaderProgramObject, WebGLMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
    gl.bindAttribLocation(shaderProgramObject, WebGLMacros.NRC_ATTRIBUTE_NORMAL, "vNormal");

    // linking
    gl.linkProgram(shaderProgramObject);
    //  error checking for linking
    if (!gl.getProgramParameter(shaderProgramObject, gl.LINK_STATUS))
    {
        var error = gl.getProgramInfoLog(shaderProgramObject);

        if(error.length > 0)
        {
            alert(error);
            uninit();
        }
    }

    //  get model-view-projection uniform location
    //  mvpUniform = gl.getUniformLocation(shaderProgramObject, "u_mvp_matrix");

    modelMatrixUniform = gl.getUniformLocation(shaderProgramObject, "u_model_matrix");
    viewMatrixUniform = gl.getUniformLocation(shaderProgramObject, "u_view_matrix");
    projectionMatrixUniform = gl.getUniformLocation(shaderProgramObject, "u_projection_matrix");
    LKeyPressedUniform = gl.getUniformLocation(shaderProgramObject, "u_LKeyPressed");
    laUniform_red = gl.getUniformLocation(shaderProgramObject, "u_La_Red");
    ldUniform_red = gl.getUniformLocation(shaderProgramObject, "u_Ld_Red");
    lsUniform_red = gl.getUniformLocation(shaderProgramObject, "u_Ls_Red");
    lightPositionUniform_red = gl.getUniformLocation(shaderProgramObject, "u_light_position_Red");
    laUniform_blue = gl.getUniformLocation(shaderProgramObject, "u_La_Blue");
    ldUniform_blue = gl.getUniformLocation(shaderProgramObject, "u_Ld_Blue");
    lsUniform_blue = gl.getUniformLocation(shaderProgramObject, "u_Ls_Blue");
    lightPositionUniform_blue = gl.getUniformLocation(shaderProgramObject, "u_light_position_Blue");
    laUniform_green = gl.getUniformLocation(shaderProgramObject, "u_La_Green");
    ldUniform_green = gl.getUniformLocation(shaderProgramObject, "u_Ld_Green");
    lsUniform_green = gl.getUniformLocation(shaderProgramObject, "u_Ls_Green");
    lightPositionUniform_green = gl.getUniformLocation(shaderProgramObject, "u_light_position_Green");
    kaUniform = gl.getUniformLocation(shaderProgramObject, "u_Ka");
    kdUniform = gl.getUniformLocation(shaderProgramObject, "u_Kd");
    ksUniform = gl.getUniformLocation(shaderProgramObject, "u_Ks");
    materialShininessUniform = gl.getUniformLocation(shaderProgramObject, "u_material_shininess");
    
    /* SPHERE STUFF */
    sphere = new Mesh();
    makeSphere(sphere, 2.0, 100, 100);

    //  various initializations
    gl.enable(gl.DEPTH_TEST);
    gl.depthFunc(gl.LEQUAL);
    gl.enable(gl.CULL_FACE);

    //  set clear color (background) as black
    gl.clearColor(0.0, 0.0, 0.0, 1.0);

    //  initialization of projection-matrix(orthographic)
    perspectiveProjectionMatrix = mat4.create();   //  create() creates a 16-member array and fills it with identity matrix

}

function resize()
{
    //  code
    if(bFullscreen == true)
    {
        //  remember to use screen.width and screen.height for 24 Spheres!
        canvas.width    = window.innerWidth;
        canvas.height   = window.innerHeight; 
    }
    else
    {
        canvas.width    = canvas_original_width;
        canvas.height   = canvas_original_height;
    }

    //  set viewport
    gl.viewport(0, 0, canvas.width, canvas.height);

    //  set perspective-projection-matrix
    mat4.perspective(perspectiveProjectionMatrix, 45.0, parseFloat(canvas.width) / parseFloat(canvas.height), 0.1, 100.0);
    
}

//  this is our display() / render()
function draw()
{
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

    //  start using shaderProgramObject
    gl.useProgram(shaderProgramObject);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform_red, light_ambient_red);
        gl.uniform3fv(ldUniform_red, light_diffuse_red);
        gl.uniform3fv(lsUniform_red, light_specular_red);
        gl.uniform4fv(lightPositionUniform_red, light_position_red);

        //  setting blue light's properties
        gl.uniform3fv(laUniform_blue, light_ambient_blue);
        gl.uniform3fv(ldUniform_blue, light_diffuse_blue);
        gl.uniform3fv(lsUniform_blue, light_specular_blue);
        gl.uniform4fv(lightPositionUniform_blue, light_position_blue);

        //  setting green light's properties
        gl.uniform3fv(laUniform_green, light_ambient_green);
        gl.uniform3fv(ldUniform_green, light_diffuse_green);
        gl.uniform3fv(lsUniform_green, light_specular_green);
        gl.uniform4fv(lightPositionUniform_green, light_position_green);

        //  setting material properties
        gl.uniform3fv(kaUniform, material_ambient);
        gl.uniform3fv(kdUniform, material_diffuse);
        gl.uniform3fv(ksUniform, material_specular);
        gl.uniform1f(materialShininessUniform, material_shininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    light_position_red[1] = Math.cos(angle_red) * 100.0;
    light_position_red[2] = Math.sin(angle_red) * 100.0;

    light_position_green[0] = Math.sin(angle_green) * 100.0;
    light_position_green[2] = Math.cos(angle_green) * 100.0;

    light_position_blue[0] = Math.cos(angle_blue) * 100.0;
    light_position_blue[1] = Math.sin(angle_blue) * 100.0;

    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.0, 0.0, -6.0]);

    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    sphere.draw(); 
    
    gl.useProgram(null);

    update();
    //  animation loop!
    requestAnimationFrame(draw, canvas);
}

function keyDownEvent(event)
{
    //  other key handlers
    switch(event.keyCode)
    {
        case 70:    //  for 'F' or 'f'; ASCII value of F is 70
            toggleFullscreen();
            break;

        case 76:    //  for 'L' or 'l' key
            if(bLKeyPressed == false)
                bLKeyPressed = true;
            else
                bLKeyPressed = false;
            /* I'm one step closer to the edge, and I'm about to.....*/
            break;
        
        case 27:    //  for ESCAPE key
            uninit();
            window.close();
            break;
    }

}

function degToRad(degrees)
{
    return(degrees * Math.PI / 180.0);
}

function update()
{
    angle_red = angle_red - 0.05;
    if (angle_red <= -360.0)
        angle_red = 0.0;

    angle_blue = angle_blue - 0.05;
    if (angle_blue <= -360.0)
        angle_blue = 0.0;
    
    angle_green = angle_green - 0.05;
    if (angle_green <= -360.0)
        angle_green = 0.0;
}

function mouseDownEvent()
{
    alert("Mouse is clicked.");
}

function uninit()
{
    //  First check if the variable exists; if it does, delete it

    if(sphere)
    {
        sphere.deallocate();
        sphere = null;
    }
    
    if(shaderProgramObject)
    {
        if(fragmentShaderObject)
        {
            gl.detachShader(shaderProgramObject, fragmentShaderObject);
            gl.deleteShader(fragmentShaderObject);
            fragmentShaderObject = null;
        }
        
        if(vertexShaderObject)
        {
            gl.detachShader(shaderProgramObject, vertexShaderObject);
            gl.deleteShader(vertexShaderObject);
            vertexShaderObject = null;
        }
        
        gl.deleteProgram(shaderProgramObject);
        shaderProgramObject = null;
    }
}

