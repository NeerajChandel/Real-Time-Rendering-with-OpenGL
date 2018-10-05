/*
*   Program:    Per Vertex Light in WebGL 2.0
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
var light_position = [100.0, 100.0, 100.0, 1.0];

var material_ambient = [0.0, 0.0, 0.0];
var material_diffuse = [1.0, 1.0, 1.0];
var material_specular = [1.0, 1.0, 1.0];
var material_shininess = 50.0;

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
    "#version 300 es"   +
    "\n"    +
    "in vec4 vPosition;"    +
    "in vec3 vNormal;"   +
    "uniform mat4 u_model_matrix;" +
    "uniform mat4 u_view_matrix;"   +
    "uniform mat4 u_projection_matrix;" +
    "uniform mediump int u_LKeyPressed;"    +
    "uniform vec3 u_La;"    +
    "uniform vec3 u_Ld;"    +
    "uniform vec3 u_Ls;"    +
    "uniform vec4 u_light_position;"    +
    "uniform vec3 u_Ka;"    +
    "uniform vec3 u_Kd;"    +
    "uniform vec3 u_Ks;"    +
    "uniform float u_material_shininess;"   +
    "out vec3 phong_ads_color;" +
    "void main(void)"   +
    "{" +
    "if(u_LKeyPressed == 1)"    +
    "{" +
    "vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"    +
    "vec3 transformed_normals = normalize(mat3(u_view_matrix * u_model_matrix) * vNormal);" +
    "vec3 light_direction = normalize(vec3(u_light_position) - eye_coordinates.xyz);"   +
    "float tn_dot_ld = max(dot(transformed_normals, light_direction), 0.0);"    +
    "vec3 ambient = u_La * u_Ka;"   +
    "vec3 diffuse = u_Ld * u_Kd * tn_dot_ld;"   +
    "vec3 reflection_vector = reflect(-light_direction, transformed_normals);"  +
    "vec3 viewer_vector = normalize(-eye_coordinates.xyz);" +
    "vec3 specular = u_Ls * u_Ks * pow(max(dot(reflection_vector, viewer_vector), 0.0), u_material_shininess);" +
    "phong_ads_color = ambient + diffuse + specular;"   +
    "}" +
    "else"  +
    "{" +
    "phong_ads_color = vec3(1.0, 1.0, 1.0);"    +
    "}" +
    "gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"  +
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
    "#version 300 es"   +
    "\n"    +
    "precision highp float;"    +
    "in vec3 phong_ads_color;"    +
    "out vec4 FragColor;"   +
    "void main(void)"   +
    "{" +
    "FragColor = vec4(phong_ads_color, 1.0);"   +
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
            uninitialize();
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
            uninitialize();
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
    makeSphere(sphere, 2.0, 40, 40);

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

        //  setting light's properties
        gl.uniform3fv(laUniform, light_ambient);
        gl.uniform3fv(ldUniform, light_diffuse);
        gl.uniform3fv(lsUniform, light_specular);
        gl.uniform4fv(lightPositionUniform, light_position);

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

    var modelMatrix = mat4.create();
    var viewMatrix = mat4.create();
    
    mat4.translate(modelMatrix, modelMatrix, [0.0, 0.0, -6.0]);

    gl.uniformMatrix4fv(modelMatrixUniform, false, modelMatrix);
    gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    sphere.draw();
    
    gl.useProgram(null);

    //  update();
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

