/*
*   Program:    Diffuse Light in WebGL 2.0
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

//  variables for rotation
var angle_cube = 0.0;
var modelViewMatrixUniform;
var projectionMatrixUniform;
var ldUniform, kdUniform, lightPositionUniform;
var LKeyPressedUniform;

var bLKeyPressed = false;
var degrees;

var vertexShaderObject;
var fragmentShaderObject;
var shaderProgramObject;

var vao_cube;
var vbo_position;
var vbo_normal;

//  projection matrix variable
var perspectiveProjectionMatrix;

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
    "uniform mat4 u_model_view_matrix;" +
    "uniform mat4 u_projection_matrix;" +
    "uniform mediump int u_LKeyPressed;"    +
    "uniform vec3 u_Ld;"    +
    "uniform vec3 u_Kd;"    +
    "uniform vec4 u_light_position;"    +
    "out vec3 diffuse_light;"   +
    "void main(void)"   +
    "{" +
    "if(u_LKeyPressed == 1)"    +
    "{" +
    "vec4 eyeCoordinates = u_model_view_matrix * vPosition;"    +
    "vec3 tnorm = normalize(mat3(u_model_view_matrix) * vNormal);"  +
    "vec3 s = normalize(vec3(u_light_position - eyeCoordinates));"  +
    "diffuse_light = u_Ld * u_Kd * max(dot(s, tnorm), 0.0);"    +
    "}" +
    "gl_Position = u_projection_matrix * u_model_view_matrix * vPosition;"  +
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
    "in vec3 diffuse_light;"    +
    "out vec4 FragColor;"   +
    "uniform int u_LKeyPressed;"    +
    "void main(void)"   +
    "{" +
    "vec4 color;"   +
    "if(u_LKeyPressed == 1)"    +
    "{" +
    "color = vec4(diffuse_light, 1.0);" +
    "}" +
    "else"  +
    "{" +
    "color = vec4(1.0, 1.0, 1.0, 1.0);" +
    "}" +
    "FragColor = color;"    +
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

    modelViewMatrixUniform = gl.getUniformLocation(shaderProgramObject, "u_model_view_matrix");
    projectionMatrixUniform = gl.getUniformLocation(shaderProgramObject, "u_projection_matrix");
    LKeyPressedUniform = gl.getUniformLocation(shaderProgramObject, "u_LKeyPressed");
    ldUniform = gl.getUniformLocation(shaderProgramObject, "u_Ld");
    kdUniform = gl.getUniformLocation(shaderProgramObject, "u_Kd");
    lightPositionUniform = gl.getUniformLocation(shaderProgramObject, "u_light_position");

    //  array of float for vertices of square
    var cubeVertices = new Float32Array
        ([
            // top surface
            1.0, 1.0, -1.0,  
            -1.0, 1.0, -1.0, 
            -1.0, 1.0, 1.0, 
            1.0, 1.0, 1.0,  
            
            // bottom surface
            1.0, -1.0, 1.0,  
            -1.0, -1.0, 1.0, 
            -1.0, -1.0, -1.0, 
            1.0, -1.0, -1.0,  
            
            // front surface
            1.0, 1.0, 1.0,  
            -1.0, 1.0, 1.0, 
            -1.0, -1.0, 1.0, 
            1.0, -1.0, 1.0, 
            
            // back surface
            1.0, -1.0, -1.0,  
            -1.0, -1.0, -1.0, 
            -1.0, 1.0, -1.0, 
            1.0, 1.0, -1.0,  
            
            // left surface
            -1.0, 1.0, 1.0, 
            -1.0, 1.0, -1.0, 
            -1.0, -1.0, -1.0, 
            -1.0, -1.0, 1.0, 
            
            // right surface
            1.0, 1.0, -1.0,  
            1.0, 1.0, 1.0,  
            1.0, -1.0, 1.0,  
            1.0, -1.0, -1.0 
        ]);

    var cubeNormals = new Float32Array
        ([
            // top
            0.0, 1.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 1.0, 0.0,
            
            // bottom
            0.0, -1.0, 0.0,
            0.0, -1.0, 0.0,
            0.0, -1.0, 0.0,
            0.0, -1.0, 0.0,
            
            // front
            0.0, 0.0, 1.0,
            0.0, 0.0, 1.0,
            0.0, 0.0, 1.0,
            0.0, 0.0, 1.0,
            
            // back
            0.0, 0.0, -1.0,
            0.0, 0.0, -1.0,
            0.0, 0.0, -1.0,
            0.0, 0.0, -1.0,
            
            // left
            -1.0, 0.0, 0.0,
            -1.0, 0.0, 0.0,
            -1.0, 0.0, 0.0,
            -1.0, 0.0, 0.0,
            
            // right
            1.0, 0.0, 0.0,
            1.0, 0.0, 0.0,
            1.0, 0.0, 0.0,
            1.0, 0.0, 0.0
        ]);    

    /*------------------------------- CUBE VAO -----------------------------*/

    //  VAO for cube
    vao_cube = gl.createVertexArray();
    gl.bindVertexArray(vao_cube);

    //  BIND VBO-POSITION
    vbo_position = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_position);

    gl.bufferData(gl.ARRAY_BUFFER, cubeVertices, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_VERTEX, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_VERTEX);

    gl.bindBuffer(gl.ARRAY_BUFFER, null);   //  unbind vbo_position

    //  BIND VBO for normals
    vbo_normal = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_normal);

    gl.bufferData(gl.ARRAY_BUFFER, cubeNormals, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_NORMAL, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_NORMAL);

    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_normal);

    gl.bindVertexArray(null);   //  unbind vao

    /********************************************************************************************/

    //  various initializations
    gl.enable(gl.DEPTH_TEST);
    gl.depthFunc(gl.LEQUAL);

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
    gl.clear(gl.COLOR_BUFFER_BIT);

    //  start using shaderProgramObject
    gl.useProgram(shaderProgramObject);

    if(bLKeyPressed == true)
    {
        gl.uniform1i(LKeyPressedUniform, 1);

        //  set light's properties
        gl.uniform3f(ldUniform, 1.0, 1.0, 1.0);
        //  set material's properties
        gl.uniform3f(kdUniform, 0.5, 0.5, 0.5);

        var lightPosition = [0.0, 0.0, 2.0, 1.0];
        gl.uniform4fv(lightPositionUniform, lightPosition);
    }
    else
    {
        gl.uniform1i(LKeyPressedUniform, 0);
    }

    /*------------------------------- DRAW CUBE ------------------------------*/

    var modelViewMatrix = mat4.create();
    //var modelViewProjectionMatrix = mat4.create();
    var translateMatrix = mat4.create();
    var rotateMatrix = mat4.create();
    var scaleMatrix = mat4.create();

    mat4.translate(translateMatrix, translateMatrix, [0.0, 0.0, -4.0]);
    mat4.scale(scaleMatrix, scaleMatrix, [0.85, 0.85, 0.85]);
    mat4.rotateX(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateY(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateZ(rotateMatrix, rotateMatrix, degToRad(angle_cube));

    mat4.multiply(modelViewMatrix, modelViewMatrix, translateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, rotateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, scaleMatrix);

    gl.uniformMatrix4fv(modelViewMatrixUniform, false, modelViewMatrix);
    gl.uniformMatrix4fv(projectionMatrixUniform, false, perspectiveProjectionMatrix);

    gl.bindVertexArray(vao_cube);
    gl.drawArrays(gl.TRIANGLE_FAN, 0, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 4, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 8, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 12, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 16, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 20, 4);
    gl.bindVertexArray(null);   //  unbind with vao as drawing is finished
    
    gl.useProgram(null);

    update();
    //  animation loop!
    requestAnimationFrame(draw, canvas);
}

function degToRad(degrees)
{
    return(degrees * Math.PI / 180.0);
}

function update()
{
    angle_cube = angle_cube - 1.0;
    if (angle_cube <= -360.0)
        angle_cube = 0.0;
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

    if(vao_cube)
    {
        gl.deleteVertexArray(vao_cube);
        vao_cube = null;
    }
    
    if(vbo_position)
    {
        gl.deleteBuffer(vbo_position);
        vbo_position = null;
    }

    if(vbo_normal)
    {
        gl.deleteBuffer(vbo_normal);
        vbo_normal = null;
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

