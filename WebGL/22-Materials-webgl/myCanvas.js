/*
*   Program:    Materials in WebGL 2.0
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

var light_ambient = [0.0, 0.0, 0.0];
var light_diffuse = [1.0, 1.0, 1.0];
var light_specular = [1.0, 1.0, 1.0];
var light_position = [100.0, 100.0, 100.0, 0.0];

//  material components
//emerald
var emerald_ambient = [ 0.0215, 0.1745, 0.0215 ];
var emerald_diffuse = [ 0.07568, 0.61424, 0.07568 ];
var emerald_specular = [ 0.633, 0.727811, 0.633 ];
var emeraldShininess = 0.6 * 128.0;

//jade
var jade_ambient = [ 0.135, 0.2225, 0.155 ];
var jade_diffuse = [ 0.54, 0.89, 0.63 ];
var jade_specular = [ 0.316228, 0.316228, 0.316228 ];
var jadeShininess = 0.21794872 * 128.0;

//obsidian
var obsidian_ambient = [ 0.05375, 0.05, 0.06625 ];
var obsidian_diffuse = [ 0.18275, 0.17, 0.22525 ];
var obsidian_specular = [ 0.332741, 0.328634, 0.346435 ];
var obsidianShininess = 0.3 * 128;

//pearl
var pearl_ambient = [ 0.25, 0.20725, 0.20725 ];
var pearl_diffuse = [ 1.0, 0.829, 0.829 ];
var pearl_specular = [ 0.296648, 0.296648, 0.296648 ];
var pearlShininess = 0.088 * 128;

//ruby
var ruby_ambient = [ 0.1745, 0.01175, 0.01175 ];
var ruby_diffuse = [ 0.61424, 0.04136, 0.04136 ];
var ruby_specular = [ 0.727811, 0.626959, 0.626959 ];
var rubyShininess = 0.6 * 128;

//turquoise
var turquoise_ambient = [ 0.1, 0.18725, 0.1745 ];
var turquoise_diffuse = [ 0.396, 0.74151, 0.69102 ];
var turquoise_specular = [ 0.297254, 0.30829, 0.306678 ];
var turquoiseShininess = 0.1 * 128;

//brass
var brass_ambient = [ 0.329412, 0.223529, 0.027451 ];
var brass_diffuse = [ 0.780392, 0.568627, 0.113725 ];
var brass_specular = [ 0.992157, 0.941176, 0.807843 ];
var brassShininess = 0.21794872 * 128;

//bronze
var bronze_ambient = [ 0.2125, 0.1275, 0.054 ];
var bronze_diffuse = [ 0.714, 0.4284, 0.18144 ];
var bronze_specular = [ 0.393548, 0.271906, 0.166721 ];
var bronzeShininess = 0.2 * 128;

//chrome
var chrome_ambient = [ 0.25, 0.25, 0.25 ];
var chrome_diffuse = [ 0.4, 0.4, 0.4 ];
var chrome_specular = [ 0.774597, 0.774597, 0.774597 ];
var chromeShininess = 0.6 * 128;

//copper
var copper_ambient = [ 0.19125, 0.0735, 0.0225 ];
var copper_diffuse = [ 0.7038, 0.27048, 0.0828 ];
var copper_specular = [ 0.256777, 0.137622, 0.0860140 ];
var copperShininess = 0.1 * 128;

//gold
var gold_ambient = [ 0.24725, 0.1995, 0.0745 ];
var gold_diffuse = [ 0.75164, 0.60648, 0.22648 ];
var gold_specular = [ 0.628281, 0.555802, 0.366065 ];
var goldShininess = 0.4 * 128;

//silver
var silver_ambient = [ 0.19225, 0.19225, 0.19225 ];
var silver_diffuse = [ 0.50754, 0.50754, 0.50754 ];
var silver_specular = [ 0.508273, 0.508273, 0.508273 ];
var silverShininess = 0.4 * 128;

//black
var black_ambient = [ 0.0, 0.0, 0.0 ];
var black_diffuse = [ 0.01, 0.01, 0.01 ];
var black_specular = [ 0.50, 0.50, 0.50 ];
var blackShininess = 0.25 * 128;

//cyan
var cyan_ambient = [ 0.0, 0.1, 0.06 ];
var cyan_diffuse = [ 0.0, 0.50980392, 0.50980392 ];
var cyan_specular = [ 0.50196078, 0.50196078, 0.50196078 ];
var cyanShininess = 0.25 * 128;

//green
var green_ambient = [ 0.0, 0.0, 0.0 ];
var green_diffuse = [ 0.1, 0.35, 0.1 ];
var green_specular = [ 0.45, 0.55, 0.45 ];
var greenShininess = 0.25 * 128;

//red
var red_ambient = [ 0.0, 0.0, 0.0 ];
var red_diffuse = [ 0.5, 0.0, 0.0 ];
var red_specular = [ 0.7, 0.6, 0.6 ];
var redShininess = 0.25 * 128;

//white
var white_ambient = [ 0.0, 0.0, 0.0 ];
var white_diffuse = [ 0.55, 0.55, 0.55 ];
var white_specular = [ 0.70, 0.70, 0.70 ];
var whiteShininess = 0.25 * 128;

//yellow-plastic
var yellow_ambient = [ 0.0, 0.0, 0.0 ];
var yellow_diffuse = [ 0.5, 0.5, 0.0 ];
var yellow_specular = [ 0.60, 0.60, 0.50 ];
var yellowShininess = 0.25 * 128;

//black
var black2_ambient = [ 0.02, 0.02, 0.02 ];
var black2_diffuse = [ 0.01, 0.01, 0.01 ];
var black2_specular = [ 0.4, 0.4, 0.4 ];
var black2Shininess = 0.078125 * 128;

//cyan
var cyan2_ambient = [ 0.0, 0.05, 0.05 ];
var cyan2_diffuse = [ 0.4, 0.5, 0.5 ];
var cyan2_specular = [ 0.04, 0.7, 0.7 ];
var cyan2Shininess = 0.078125 * 128;

//green
var green2_ambient = [ 0.0, 0.05, 0.0 ];
var green2_diffuse = [ 0.4, 0.5, 0.4 ];
var green2_specular = [ 0.04, 0.7, 0.04 ];
var green2Shininess = 0.078125 * 128;

//red
var red2_ambient = [ 0.05, 0.0, 0.0 ];
var red2_diffuse = [ 0.5, 0.4, 0.4 ];
var red2_specular = [ 0.7, 0.04, 0.04 ];
var red2Shininess = 0.078125 * 128;

//white
var white2_ambient = [ 0.05, 0.05, 0.05 ];
var white2_diffuse = [ 0.5, 0.5, 0.5 ];
var white2_specular = [ 0.7, 0.7, 0.7 ];
var white2Shininess = 0.078125 * 128;

//yellow-rubber
var yellow2_ambient = [ 0.05, 0.05, 0.0 ];
var yellow2_diffuse = [ 0.5, 0.5, 0.4 ];
var yellow2_specular = [ 0.7, 0.7, 0.04 ];
var yellow2Shininess = 0.078125 * 128;


var degrees;
var angle_light = 0.0;
var x_rotation = 0;
var y_rotation = 0;
var z_rotation = 0;

var sphere = null;

var modelMatrixUniform, viewMatrixUniform, projectionMatrixUniform;
var laUniform, ldUniform, lsUniform, lightPositionUniform;
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
         "uniform vec4 u_light_position;"	+
         "out vec3 transformed_normals;"	+
         "out vec3 light_direction;"	+
         "out vec3 viewer_vector;"	+
         "void main(void)"	+
         "{"	+
         "if(u_LKeyPressed == 1)"	+
         "{"	+
         "vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	+
         "transformed_normals = mat3(u_view_matrix * u_model_matrix) * vNormal;"	+
         "light_direction = vec3(u_light_position) - eye_coordinates.xyz;"	+
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
    "uniform int u_LKeyPressed;"	+
    "void main(void)"	+
    "{"	+
    "vec3 phong_ads_color;"	+
    "if(u_LKeyPressed == 1)"	+
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
    
    laUniform = gl.getUniformLocation(shaderProgramObject, "u_La");
    ldUniform = gl.getUniformLocation(shaderProgramObject, "u_Ld");
    lsUniform = gl.getUniformLocation(shaderProgramObject, "u_Ls");
    lightPositionUniform = gl.getUniformLocation(shaderProgramObject, "u_light_position");
    
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
    gl.clearColor(0.25, 0.25, 0.25, 1.0);

    //  initialization of projection-matrix(orthographic)
    perspectiveProjectionMatrix = mat4.create();   //  create() creates a 16-member array and fills it with identity matrix

}

function resize()
{
    //  code
    if(bFullscreen == true)
    {
        //  remember to use screen.width and screen.height for 24 Spheres!
        canvas.width    = screen.innerWidth;
        canvas.height   = screen.innerHeight; 
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

    //--------------------------------------------- 1st row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-1.8, 2.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, emerald_ambient);
        gl.uniform3fv(kdUniform, emerald_diffuse);
        gl.uniform3fv(ksUniform, emerald_specular);
        gl.uniform1f(materialShininessUniform, emeraldShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw(); 

     //--------------------------------------------- 1st row ------------------------------------------------------------//
    //	matrix stuff
    modelMatrix = mat4.create();
    viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-0.6, 2.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, brass_ambient);
        gl.uniform3fv(kdUniform, brass_diffuse);
        gl.uniform3fv(ksUniform, brass_specular);
        gl.uniform1f(materialShininessUniform, brassShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw(); 
    
     //--------------------------------------------- 1st row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.6, 2.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, black_ambient);
        gl.uniform3fv(kdUniform, black_diffuse);
        gl.uniform3fv(ksUniform, black_specular);
        gl.uniform1f(materialShininessUniform, blackShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw(); 

     //--------------------------------------------- 1st row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [1.8, 2.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, black2_ambient);
        gl.uniform3fv(kdUniform, black2_diffuse);
        gl.uniform3fv(ksUniform, black2_specular);
        gl.uniform1f(materialShininessUniform, black2Shininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw(); 

    //--------------------------------------------- 2nd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-1.8, 1.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, jade_ambient);
        gl.uniform3fv(kdUniform, jade_diffuse);
        gl.uniform3fv(ksUniform, jade_specular);
        gl.uniform1f(materialShininessUniform, jadeShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw(); 

    //--------------------------------------------- 2nd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-0.6, 1.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, bronze_ambient);
        gl.uniform3fv(kdUniform, bronze_diffuse);
        gl.uniform3fv(ksUniform, bronze_specular);
        gl.uniform1f(materialShininessUniform, bronzeShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 2nd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.6, 1.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, cyan_ambient);
        gl.uniform3fv(kdUniform, cyan_diffuse);
        gl.uniform3fv(ksUniform, cyan_specular);
        gl.uniform1f(materialShininessUniform, cyanShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 2nd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [1.8, 1.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, cyan2_ambient);
        gl.uniform3fv(kdUniform, cyan2_diffuse);
        gl.uniform3fv(ksUniform, cyan2_specular);
        gl.uniform1f(materialShininessUniform, cyan2Shininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 3rd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-1.8, 0.5, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, obsidian_ambient);
        gl.uniform3fv(kdUniform, obsidian_diffuse);
        gl.uniform3fv(ksUniform, obsidian_specular);
        gl.uniform1f(materialShininessUniform, obsidianShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 3rd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-0.6, 0.5, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, chrome_ambient);
        gl.uniform3fv(kdUniform, chrome_diffuse);
        gl.uniform3fv(ksUniform, chrome_specular);
        gl.uniform1f(materialShininessUniform, chromeShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 3rd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.6, 0.5, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, green_ambient);
        gl.uniform3fv(kdUniform, green_diffuse);
        gl.uniform3fv(ksUniform, green_specular);
        gl.uniform1f(materialShininessUniform, greenShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 3rd row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [1.8, 0.5, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, green2_ambient);
        gl.uniform3fv(kdUniform, green2_diffuse);
        gl.uniform3fv(ksUniform, green2_specular);
        gl.uniform1f(materialShininessUniform, green2Shininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 4th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-1.8, -0.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, pearl_ambient);
        gl.uniform3fv(kdUniform, pearl_diffuse);
        gl.uniform3fv(ksUniform, pearl_specular);
        gl.uniform1f(materialShininessUniform, pearlShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 4th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-0.6, -0.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, copper_ambient);
        gl.uniform3fv(kdUniform, copper_diffuse);
        gl.uniform3fv(ksUniform, copper_specular);
        gl.uniform1f(materialShininessUniform, copperShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 4th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.6, -0.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, red_ambient);
        gl.uniform3fv(kdUniform, red_diffuse);
        gl.uniform3fv(ksUniform, red_specular);
        gl.uniform1f(materialShininessUniform, redShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 4th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [1.8, -0.25, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, red2_ambient);
        gl.uniform3fv(kdUniform, red2_diffuse);
        gl.uniform3fv(ksUniform, red2_specular);
        gl.uniform1f(materialShininessUniform, red2Shininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 5th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-1.8, -1.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, ruby_ambient);
        gl.uniform3fv(kdUniform, ruby_diffuse);
        gl.uniform3fv(ksUniform, ruby_specular);
        gl.uniform1f(materialShininessUniform, rubyShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 5th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-0.6, -1.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, gold_ambient);
        gl.uniform3fv(kdUniform, gold_diffuse);
        gl.uniform3fv(ksUniform, gold_specular);
        gl.uniform1f(materialShininessUniform, goldShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 5th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.6, -1.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, white_ambient);
        gl.uniform3fv(kdUniform, white_diffuse);
        gl.uniform3fv(ksUniform, white_specular);
        gl.uniform1f(materialShininessUniform, whiteShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 5th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [1.8, -1.0, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, white2_ambient);
        gl.uniform3fv(kdUniform, white2_diffuse);
        gl.uniform3fv(ksUniform, white2_specular);
        gl.uniform1f(materialShininessUniform, white2Shininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 6th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-1.8, -1.75, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, turquoise_ambient);
        gl.uniform3fv(kdUniform, turquoise_diffuse);
        gl.uniform3fv(ksUniform, turquoise_specular);
        gl.uniform1f(materialShininessUniform, turquoiseShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 6th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [-0.6, -1.75, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, silver_ambient);
        gl.uniform3fv(kdUniform, silver_diffuse);
        gl.uniform3fv(ksUniform, silver_specular);
        gl.uniform1f(materialShininessUniform, silverShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 6th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.6, -1.75, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, yellow_ambient);
        gl.uniform3fv(kdUniform, yellow_diffuse);
        gl.uniform3fv(ksUniform, yellow_specular);
        gl.uniform1f(materialShininessUniform, yellowShininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    sphere.draw();

    //--------------------------------------------- 6th row ------------------------------------------------------------//
    //	matrix stuff
    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [1.8, -1.75, -6.0]);
    mat4.scale(modelMatrix, modelMatrix, [0.15, 0.15, 0.15]);
    
    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  setting red light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

        //  setting material properties
        gl.uniform3fv(kaUniform, yellow2_ambient);
        gl.uniform3fv(kdUniform, yellow2_diffuse);
        gl.uniform3fv(ksUniform, yellow2_specular);
        gl.uniform1f(materialShininessUniform, yellow2Shininess);

    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

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

        case 88:    //  for 'X'
            light_position[0] = 0.0;
            light_position[1] = 0.0;
            light_position[2] = 0.0;
            x_rotation = 1;
			y_rotation = 0;
			z_rotation = 0;
            break;
        
        case 89:    //  for 'Y'
            light_position[0] = 0.0;
            light_position[1] = 0.0;
            light_position[2] = 0.0;
            x_rotation = 0;
			y_rotation = 1;
			z_rotation = 0;
            break;

        case 90:    //  for 'Z'
            light_position[0] = 0.0;
            light_position[1] = 0.0;
            light_position[2] = 0.0;
            x_rotation = 0;
			y_rotation = 0;
			z_rotation = 1;
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
    if(x_rotation == 1)
    {
        angle_light = angle_light + 0.05;
        {
            light_position[0] = 0.0;
            light_position[1] = 50 * Math.sin(angle_light);
            light_position[2] = 50 * Math.cos(angle_light);
            if(angle_light >= 360.0)
                angle_light = 0.0;
        }
    }

    if(y_rotation == 1)
    {
        angle_light = angle_light + 0.05;
        {
            light_position[1] = 0.0;
            light_position[0] = 50 * Math.sin(angle_light);
            light_position[2] = 50 * Math.cos(angle_light);
            if(angle_light >= 360.0)
                angle_light = 0.0;
        }
    }

    if(z_rotation == 1)
    {
        angle_light = angle_light + 0.05;
        {
            light_position[2] = 0.0;
            light_position[1] = 50 * Math.sin(angle_light);
            light_position[0] = 50 * Math.cos(angle_light);
            if(angle_light >= 360.0)
                angle_light = 0.0;
        }
    }
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

