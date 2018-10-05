/*
*   Program:    3D Rotation in WebGL 2.0
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
var angle_pyr = 0.0;
var angle_cube = 0.0;
var degrees;

var vertexShaderObject;
var fragmentShaderObject;
var shaderProgramObject;

//  2 VAOs - for triangle and square; 2 VBOs - for position and color
var vao_pyramid;
var vao_cube;
var vbo_position;
var vbo_color;

var vao_my_cube;
var vbo_my_color;

var mvpUniform;

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
    "in vec4 vColor;"   +
    "uniform mat4 u_mvp_matrix;" +
    "out vec4 out_color;"  +
    "void main(void)"   +
    "{" +
    "gl_Position = u_mvp_matrix * vPosition;"   +
    "out_color = vColor;"  +
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
    "in vec4 out_color;"  +
    "out vec4 FragColor;"   +
    "void main(void)"   +
    "{" +
    "FragColor = out_color;" +
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
    gl.bindAttribLocation(shaderProgramObject, WebGLMacros.NRC_ATTRIBUTE_COLOR, "vColor");

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
    mvpUniform = gl.getUniformLocation(shaderProgramObject, "u_mvp_matrix");

    var pyramidVertices = new Float32Array
        ([
            0.0, 1.0, 0.0,      // front face
            -1.0, -1.0, 1.0, 
            1.0, -1.0, 1.0,  
            0.0, 1.0, 0.0,      // right face
            1.0, -1.0, 1.0,
            1.0, -1.0, -1.0,
            0.0, 1.0, 0.0,      // back face
            1.0, -1.0, -1.0,
            -1.0, -1.0, -1.0,
            0.0, 1.0, 0.0,      // left face
            -1.0, -1.0, -1.0,
            -1.0, -1.0, 1.0
        ]);


    //  array of float for vertices of square
    var cubeVertices = new Float32Array
        ([
            1.0, 1.0, 1.0,      // front face
            -1.0, 1.0, 1.0,
            -1.0, -1.0, 1.0,
            1.0, -1.0, 1.0,
            1.0, 1.0, -1.0,     // right face
            1.0, 1.0, 1.0,
            1.0, -1.0, 1.0,
            1.0, -1.0, -1.0,
            -1.0, 1.0, 1.0,     //left face
            -1.0, 1.0, -1.0,
            -1.0, -1.0, -1.0,
            -1.0, -1.0, 1.0,
            -1.0, 1.0, -1.0,    // back face
            1.0, 1.0, -1.0,
            1.0, -1.0, -1.0,
            -1.0, -1.0, -1.0,
            -1.0, 1.0, 1.0,     // top face
            1.0, 1.0, 1.0,
            1.0, 1.0, -1.0,
            -1.0, 1.0, -1.0,
            1.0, -1.0, 1.0,     // bottom face
            1.0, -1.0, -1.0,
            -1.0, -1.0, -1.0,
            -1.0, -1.0, 1.0
        ]);


    var pyramidColor = new Float32Array
        ([
            0.12, 0.321, 0.4,
            1.0, 0.23, 0.2,
            0.33, 0.33, 0.33,
            1.0, 0.45, 0.44,
            0.52, 0.3, 0.11,
            045, 0.66, 0.4,
            0.4, 0.11, 0.7,
            0.0, 1.0, 0.92,
            0.71, 0.17, 1.0,
            1.0, 0.22, 0.54,
            0.0, 0.0, 0.21,
            0.0, 0.0, 0.0
        ]);


    var cubeColor = new Float32Array
        ([
            1.0, 0.02, 0.0,  
            1.0, 0.26, 0.43,
            1.0, 0.02, 0.43,
            1.0, 0.2, 0.0,  
            0.0, 1.0, 0.2,
            0.2, 0.2, 0.2,
            0.11, 1.0, 0.2,  
            0.99, 0.42, 0.2,
            0.8, 0.55, 1.0,        
            0.23, 0.0, 1.0,
            0.78, 0.63, 1.0,
            0.9, 0.6, 1.0,
            0.20, 0.55, 0.0,
            0.0, 0.91, 0.03,
            0.4, 0.75, 0.0,
            0.7, 0.01, 0.0,
            0.12, 0.0, 0.5,
            0.6, 0.34, 0.25,
            1.0, 0.02, 0.5,
            1.0, 0.0, 0.5,
            0.5, 0.0, 0.0,
            0.5, 0.5, 0.5,
            1.0, 0.1, 0.43,
            0.5, 0.0, 1.0
        ]);

    /*------------------------------- PYRAMID VAO -----------------------------*/

    //  create a Vertex Array Object for pyramid data and bind with it to fill it
    vao_pyramid = gl.createVertexArray();
    gl.bindVertexArray(vao_pyramid);

    //  create a Vertex Buffer Object and bind with it to fill it with triangle vertices
    
    //  BIND VBO for position(triangleVertices)
    vbo_position = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_position);

    gl.bufferData(gl.ARRAY_BUFFER, pyramidVertices, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_VERTEX, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_VERTEX);

    gl.bindBuffer(gl.ARRAY_BUFFER, null);   //  unbind vbo_position

    //  BIND VBO for color
    vbo_color = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_color);

    gl.bufferData(gl.ARRAY_BUFFER, pyramidColor, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_COLOR, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_COLOR);

    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_color);

    gl.bindVertexArray(null);   //  unbind vao

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

    //  BIND VBO for color
    vbo_color = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_color);

    gl.bufferData(gl.ARRAY_BUFFER, cubeColor, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_COLOR, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_COLOR);

    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_color);

    gl.bindVertexArray(null);   //  unbind vao

    /********************************************************************************************/

    /*---------------------------------------- MY CUBE VAO ------------------------------------*/

    var myCubeColor = new Float32Array
    	([
    		1.0, 0.6, 0.0,  
            0.94, 0.0, 0.0,
            0.23, 0.06, 0.0,
            0.11, 0.3, 0.05,  
            0.12, 0.0, 0.03,
            0.23, 0.0, 0.02,
            0.77, 0.0, 0.11,  
            0.92, 0.12, 0.12,
            0.87, 0.0, 0.4,        
            0.64, 0.7, 0.0,
            0.64, 0.7, 0.21,
            0.53, 0.0, 1.0,
            0.53, 0.5, 0.0,
            0.83, 0.2, 0.0,
            1.0, 0.5, 0.0,
            1.0, 0.23, 0.0,
            0.0, 0.0, 0.35,
            1.0, 0.0, 0.0,
            1.0, 0.0, 0.0,
            1.0, 0.40, 0.5,
            0.98, 0.0, 0.0,
            0.0, 0.0, 0.0,
            0.42, 0.34, 0.3,
            0.0, 0.0, 0.0
    	]);

    //  VAO for cube
    vao_my_cube = gl.createVertexArray();
    gl.bindVertexArray(vao_my_cube);

    //  BIND VBO-POSITION
    vbo_position = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_position);

    gl.bufferData(gl.ARRAY_BUFFER, cubeVertices, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_VERTEX, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_VERTEX);

    gl.bindBuffer(gl.ARRAY_BUFFER, null);   //  unbind vbo_position

    //  BIND VBO for color
    vbo_my_color = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_my_color);

    gl.bufferData(gl.ARRAY_BUFFER, myCubeColor, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_COLOR, 3, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_COLOR);

    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_color);

    gl.bindVertexArray(null);   //  unbind vao

    /************************************************************************************/

    //  various initializations
    //gl.enable(gl.DEPTH_TEST);
    //gl.depthFunc(gl.LEQUAL);

    //  set clear color (background) as black
    gl.clearColor(1.0, 1.0, 1.0, 1.0);

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

    gl.lineWidth(7.0);
    
    /*------------------------------- DRAW PYRAMID ONE ------------------------------*/
    //  initialise modelViewMatrix and modelViewProjectionMatrix
    var modelViewMatrix = mat4.create();
    var modelViewProjectionMatrix = mat4.create();
    //  initialise translateMatrix for translation
    var translateMatrix = mat4.create();
    var rotateMatrix = mat4.create();


    //  translate call in WebGL:
    mat4.translate(translateMatrix, translateMatrix, [-2.8, 1.0, -6.0]);
    //  multiply translateMatrix with modelViewMatrix
    mat4.multiply(modelViewMatrix, modelViewMatrix, translateMatrix);
    //  mat4.translate(modelViewMatrix, modelViewMatrix, [0.0, 0.0, -4.0]);

    mat4.rotateY(rotateMatrix, rotateMatrix, degToRad(angle_pyr));
    mat4.multiply(modelViewMatrix, modelViewMatrix, rotateMatrix);
    //  multiply mvp, ortho and mv and put result in modelViewProjectionMatrix
    mat4.multiply(modelViewProjectionMatrix, perspectiveProjectionMatrix, modelViewMatrix);

    //  fill uniform
    gl.uniformMatrix4fv(mvpUniform, false, modelViewProjectionMatrix);

    //  bind with vao to begin drawing
    gl.bindVertexArray(vao_pyramid);
    gl.drawArrays(gl.TRIANGLES, 0, 12);
    gl.bindVertexArray(null);   //  unbind with vao as drawing is finished

    /*------------------------------- DRAW PYRAMID TWO ------------------------------
    //  initialise modelViewMatrix and modelViewProjectionMatrix
    modelViewMatrix = mat4.create();
    modelViewProjectionMatrix = mat4.create();
    //  initialise translateMatrix for translation
    translateMatrix = mat4.create();
    rotateMatrix = mat4.create();


    //  translate call in WebGL:
    mat4.translate(translateMatrix, translateMatrix, [0.0, 2.0, -11.0]);
    //  multiply translateMatrix with modelViewMatrix
    mat4.multiply(modelViewMatrix, modelViewMatrix, translateMatrix);
    //  mat4.translate(modelViewMatrix, modelViewMatrix, [0.0, 0.0, -4.0]);

    mat4.rotateX(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateY(rotateMatrix, rotateMatrix, degToRad(angle_pyr));
    mat4.multiply(modelViewMatrix, modelViewMatrix, rotateMatrix);
    //  multiply mvp, ortho and mv and put result in modelViewProjectionMatrix
    mat4.multiply(modelViewProjectionMatrix, perspectiveProjectionMatrix, modelViewMatrix);

    //  fill uniform
    gl.uniformMatrix4fv(mvpUniform, false, modelViewProjectionMatrix);

    //  bind with vao to begin drawing
    gl.bindVertexArray(vao_pyramid);
    gl.drawArrays(gl.TRIANGLES, 0, 10);
    gl.bindVertexArray(null);   //  unbind with vao as drawing is finished

    /*------------------------------- DRAW PYRAMID (LINES) ------------------------------*/
    //  initialise modelViewMatrix and modelViewProjectionMatrix
    modelViewMatrix = mat4.create();
    modelViewProjectionMatrix = mat4.create();
    //  initialise translateMatrix for translation
    translateMatrix = mat4.create();
    rotateMatrix = mat4.create();


    //  translate call in WebGL:
    mat4.translate(translateMatrix, translateMatrix, [0.0, -2.0, -11.0]);
    //  multiply translateMatrix with modelViewMatrix
    mat4.multiply(modelViewMatrix, modelViewMatrix, translateMatrix);
    //  mat4.translate(modelViewMatrix, modelViewMatrix, [0.0, 0.0, -4.0]);

    mat4.rotateX(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateY(rotateMatrix, rotateMatrix, degToRad(angle_pyr));
    mat4.multiply(modelViewMatrix, modelViewMatrix, rotateMatrix);
    //  multiply mvp, ortho and mv and put result in modelViewProjectionMatrix
    mat4.multiply(modelViewProjectionMatrix, perspectiveProjectionMatrix, modelViewMatrix);

    //  fill uniform
    gl.uniformMatrix4fv(mvpUniform, false, modelViewProjectionMatrix);

    //  bind with vao to begin drawing
    gl.bindVertexArray(vao_pyramid);
    gl.drawArrays(gl.LINE_STRIP, 0, 12);
    gl.bindVertexArray(null);   //  unbind with vao as drawing is finished

    /*------------------------------- DRAW CUBE ONE (PROPER) ------------------------------*/

    modelViewMatrix = mat4.create();
    modelViewProjectionMatrix = mat4.create();
    translateMatrix = mat4.create();
    rotateMatrix = mat4.create();
    var scaleMatrix = mat4.create();

    mat4.translate(translateMatrix, translateMatrix, [1.9, 2.4, -11.0]);
    mat4.scale(scaleMatrix, scaleMatrix, [0.85, 0.85, 0.85]);
    mat4.rotateX(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateY(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateZ(rotateMatrix, rotateMatrix, degToRad(angle_cube));

    mat4.multiply(modelViewMatrix, modelViewMatrix, translateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, rotateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, scaleMatrix);

    //  multiply mvp, ortho and mv and put result in modelViewProjectionMatrix
    mat4.multiply(modelViewProjectionMatrix, perspectiveProjectionMatrix, modelViewMatrix);

    //  fill uniform
    gl.uniformMatrix4fv(mvpUniform, false, modelViewProjectionMatrix);

    gl.bindVertexArray(vao_cube);
    gl.drawArrays(gl.TRIANGLE_FAN, 0, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 4, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 8, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 12, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 16, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 20, 4);
    gl.bindVertexArray(null);   //  unbind with vao as drawing is finished
    

    /*------------------------------- DRAW CUBE TWO (PARALLEL PLANES) ------------------------------*/

    modelViewMatrix = mat4.create();
    modelViewProjectionMatrix = mat4.create();
    translateMatrix = mat4.create();
    rotateMatrix = mat4.create();
    scaleMatrix = mat4.create();

    mat4.translate(translateMatrix, translateMatrix, [2.8, -1.0, -6.0]);
    mat4.scale(scaleMatrix, scaleMatrix, [0.65, 0.65, 0.85]);
    mat4.rotateX(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    //mat4.rotateY(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateZ(rotateMatrix, rotateMatrix, degToRad(angle_cube));

    mat4.multiply(modelViewMatrix, modelViewMatrix, translateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, rotateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, scaleMatrix);

    //  multiply mvp, ortho and mv and put result in modelViewProjectionMatrix
    mat4.multiply(modelViewProjectionMatrix, perspectiveProjectionMatrix, modelViewMatrix);

    //  fill uniform
    gl.uniformMatrix4fv(mvpUniform, false, modelViewProjectionMatrix);

    gl.bindVertexArray(vao_my_cube);
    gl.drawArrays(gl.LINE_STRIP, 0, 4);
    gl.drawArrays(gl.LINE_STRIP, 4, 4);
    gl.drawArrays(gl.LINE_STRIP, 8, 4);
    gl.drawArrays(gl.LINE_STRIP, 12, 4);
    gl.drawArrays(gl.LINE_STRIP, 16, 4);
    gl.drawArrays(gl.LINE_STRIP, 20, 4);
    gl.bindVertexArray(null);

    /*------------------------------- DRAW CUBE THREE ------------------------------*/

    modelViewMatrix = mat4.create();
    modelViewProjectionMatrix = mat4.create();
    translateMatrix = mat4.create();
    rotateMatrix = mat4.create();
    scaleMatrix = mat4.create();

    mat4.translate(translateMatrix, translateMatrix, [-2.8, -1.3, -6.0]);
    mat4.scale(scaleMatrix, scaleMatrix, [0.25, 0.9, 0.85]);
    mat4.rotateX(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    mat4.rotateY(rotateMatrix, rotateMatrix, degToRad(angle_cube));
    //mat4.rotateZ(rotateMatrix, rotateMatrix, degToRad(angle_cube));

    mat4.multiply(modelViewMatrix, modelViewMatrix, translateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, rotateMatrix);
    mat4.multiply(modelViewMatrix, modelViewMatrix, scaleMatrix);

    //  multiply mvp, ortho and mv and put result in modelViewProjectionMatrix
    mat4.multiply(modelViewProjectionMatrix, perspectiveProjectionMatrix, modelViewMatrix);

    //  fill uniform
    gl.uniformMatrix4fv(mvpUniform, false, modelViewProjectionMatrix);

    gl.bindVertexArray(vao_my_cube);
    gl.drawArrays(gl.LINE_STRIP, 0, 4);
    gl.drawArrays(gl.LINE_STRIP, 4, 4);
    gl.drawArrays(gl.LINE_STRIP, 8, 4);
    gl.drawArrays(gl.LINE_STRIP, 12, 4);
    gl.drawArrays(gl.LINE_STRIP, 16, 4);
    gl.drawArrays(gl.LINE_STRIP, 20, 4);
    gl.bindVertexArray(null);

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
    angle_pyr = angle_pyr + 1.0;
    if (angle_pyr >= 360.0)
        angle_pyr = 0.0;

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
        
        case 27:    //  for ESCAPE key
            uninitialize();
            window.close();
            break;
    }

}

function mouseDownEvent()
{
    alert("Mouse is clicked.");
}

function uninitialize()
{
    //  First check if the variable exists; if it does, delete it
    
    if(vao_pyramid)
    {
        gl.deleteVertexArray(vao_pyramid);
        vao_pyramid = null;
    }

    if(vao_cube)
    {
        gl.deleteVertexArray(vao_cube);
        vao_cube = null;
    }
    
    if(vao_my_cube)
    {
    	gl.deleteVertexArray(vao_my_cube);
    	vao_my_cube = null;
    }

    if(vbo_position)
    {
        gl.deleteBuffer(vbo_position);
        vbo_position = null;
    }

    if(vbo_color)
    {
        gl.deleteBuffer(vbo_color);
        vbo_color = null;
    }

    if(vbo_my_color)
    {
    	gl.deleteBuffer(vbo_my_color);
    	vbo_my_color = null;
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

