//  Global Variables
var canvas = null;
var gl = null;
var bFullscreen = false;
var canvas_original_width;
var canvas_original_height;

//  Macros for binding
const WebGLMacros = 
{
    NRC_ATTRIBUTE_VERTEX : 0,
    NRC_ATTRIBUTE_COLOR : 1,
    NRC_ATTRIBUTE_NORMAL : 2,
    NRC_ATTRIBUTE_TEXTURE0 : 3,
};

//  Shader Objects
var vertexShaderObject;
var fragmentShaderObject;
var shaderProgramObject;

//  VAOs' and VBOs' variables
var vao_pyramid;
var vao_cube;
var vbo_position;
var vbo_texture;

//  ModelViewProjection Matrix Uniform plumbing variable
var mvpUniform;
var perspectiveProjectionMatrix;

//  texture objects
var pyramid_texture = 0;
var cube_texture = 0;
var uniform_texture0_sampler;

var angle_pyr = 0.0;
var angle_cube = 0.0;

var requestAnimationFrame = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame;
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
    //  'gl' is the name of our context
    gl = canvas.getContext("webgl2");
    if(gl == null)
    {
        console.log("Failed to get the rendering context for WebGL 2.0.\n");
        return;
    }

    gl.viewportWidth    = canvas.width;
    gl.viewportHeight   = canvas.height;
    
    /*-------------------------- VERTEX SHADER --------------------------*/
    //  VS source code
    var vertexShaderSourceCode = 
    "#version 300 es"   +
    "\n"    +
    "in vec4 vPosition;"  +
    "in vec2 vTexture0_Coord;"  +
    "out vec2 out_texture0_coord;"  +
    "uniform mat4 u_mvp_matrix;"    +
    "void main(void)"   +
    "{" +
    "gl_Position = u_mvp_matrix * vPosition;"   +
    "out_texture0_coord = vTexture0_Coord;" +
    "}"

    vertexShaderObject = gl.createShader(gl.VERTEX_SHADER);
    gl.shaderSource(vertexShaderObject, vertexShaderSourceCode);
    gl.compileShader(vertexShaderObject);

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

    /*-------------------------- FRAGMENT SHADER --------------------------*/
    //  FS source code
    var fragmentShaderSourceCode = 
    "#version 300 es"   +
    "\n"    +
    "precision highp float;" +
    "in vec2 out_texture0_coord;"   +
    "uniform highp sampler2D u_texture0_sampler;"   +
    "out vec4 FragColor;"   +
    "void main(void)"   +
    "{" +
    "FragColor = texture(u_texture0_sampler, out_texture0_coord);"  +
    "}";

    fragmentShaderObject = gl.createShader(gl.FRAGMENT_SHADER);
    gl.shaderSource(fragmentShaderObject, fragmentShaderSourceCode);
    gl.compileShader(fragmentShaderObject);

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

    /*-------------------------- SHADER PROGRAM OBJECT --------------------------*/
    shaderProgramObject = gl.createProgram();

    //  attach our vertex shader and fragment shader to the program object
    gl.attachShader(shaderProgramObject, vertexShaderObject);
    gl.attachShader(shaderProgramObject, fragmentShaderObject);

    //  pre-link binding of shader program object with vertex shader attributes
    gl.bindAttribLocation(shaderProgramObject, WebGLMacros.NRC_ATTRIBUTE_VERTEX, "vPosition");
    gl.bindAttribLocation(shaderProgramObject, WebGLMacros.NRC_ATTRIBUTE_TEXTURE0, "vTexture0_Coord");

    //  LINKING the shader program object
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

    /* LOAD PYRAMID TEXTURE */
    pyramid_texture             = gl.createTexture();
    pyramid_texture.image       = new Image();
    pyramid_texture.image.src   = "stone.png";

    pyramid_texture.image.onload = function()
    {

        gl.bindTexture(gl.TEXTURE_2D, pyramid_texture);
        gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, pyramid_texture.image);
        gl.bindTexture(gl.TEXTURE_2D, null);

    }

    /* LOAD CUBE TEXTURE */
    cube_texture             = gl.createTexture();
    cube_texture.image       = new Image();
    cube_texture.image.src   = "kundali.png";

    cube_texture.image.onload = function()
    {

        gl.bindTexture(gl.TEXTURE_2D, cube_texture);
        gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, cube_texture.image);
        gl.bindTexture(gl.TEXTURE_2D, null);

    }

    //  get uniform locations
    mvpUniform                  = gl.getUniformLocation(shaderProgramObject, "u_mvp_matrix");
    uniform_texture0_sampler    = gl.getUniformLocation(shaderProgramObject, "u_texture0_sampler");

    //  pyramid vertices
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

    //  pyramid tex coords
    var pyramidTexcoords = new Float32Array
        ([
            0.5, 1.0, 
            0.0, 0.0, 
            1.0, 0.0, 
        
            0.5, 1.0, 
            1.0, 0.0, 
            0.0, 0.0, 
        
            0.5, 1.0, 
            1.0, 0.0, 
            0.0, 0.0, 
        
            0.5, 1.0, 
            0.0, 0.0, 
            1.0, 0.0 
        ]);

    //  cube vertices
    var cubeVertices = new Float32Array
        ([
            // top surface
            1.0, 1.0,-1.0,  // top-right of top
            -1.0, 1.0,-1.0, // top-left of top
            -1.0, 1.0, 1.0, // bottom-left of top
            1.0, 1.0, 1.0,  // bottom-right of top
            
            // bottom surface
            1.0,-1.0, 1.0,  // top-right of bottom
            -1.0,-1.0, 1.0, // top-left of bottom
            -1.0,-1.0,-1.0, // bottom-left of bottom
            1.0,-1.0,-1.0,  // bottom-right of bottom
            
            // front surface
            1.0, 1.0, 1.0,  // top-right of front
            -1.0, 1.0, 1.0, // top-left of front
            -1.0,-1.0, 1.0, // bottom-left of front
            1.0,-1.0, 1.0,  // bottom-right of front
            
            // back surface
            1.0,-1.0,-1.0,  // top-right of back
            -1.0,-1.0,-1.0, // top-left of back
            -1.0, 1.0,-1.0, // bottom-left of back
            1.0, 1.0,-1.0,  // bottom-right of back
            
            // left surface
            -1.0, 1.0, 1.0, // top-right of left
            -1.0, 1.0,-1.0, // top-left of left
            -1.0,-1.0,-1.0, // bottom-left of left
            -1.0,-1.0, 1.0, // bottom-right of left
            
            // right surface
            1.0, 1.0,-1.0,  // top-right of right
            1.0, 1.0, 1.0,  // top-left of right
            1.0,-1.0, 1.0,  // bottom-left of right
            1.0,-1.0,-1.0,  // bottom-right of right
        ]);

    //  cube tex coords
    var cubeTexcoords = new Float32Array
        ([
            0.0, 0.0,
            1.0, 0.0,
            1.0, 1.0,
            0.0, 1.0,
        
            0.0, 0.0,
            1.0, 0.0,
            1.0, 1.0,
            0.0, 1.0,
        
            0.0, 0.0,
            1.0, 0.0,
            1.0, 1.0,
            0.0, 1.0,
        
            0.0, 0.0,
            1.0, 0.0,
            1.0, 1.0,
            0.0, 1.0,
        
            0.0, 0.0,
            1.0, 0.0,
            1.0, 1.0,
            0.0, 1.0,
        
            0.0, 0.0,
            1.0, 0.0,
            1.0, 1.0,
            0.0, 1.0
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

    //  BIND VBO for texture
    vbo_texture = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_texture);
    
    gl.bufferData(gl.ARRAY_BUFFER, pyramidTexcoords, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_TEXTURE0, 2, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_TEXTURE0);

    gl.bindBuffer(gl.ARRAY_BUFFER, null);   //  unbind vbo_texture

    //  unbind vao_pyramid
    gl.bindVertexArray(null);

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

    //  BIND VBO-TEXTURE
    vbo_texture = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, vbo_texture);
    
    gl.bufferData(gl.ARRAY_BUFFER, cubeTexcoords, gl.STATIC_DRAW);
    gl.vertexAttribPointer(WebGLMacros.NRC_ATTRIBUTE_TEXTURE0, 2, gl.FLOAT, false, 0, 0);
    gl.enableVertexAttribArray(WebGLMacros.NRC_ATTRIBUTE_TEXTURE0);

    gl.bindBuffer(gl.ARRAY_BUFFER, null);   //  unbind vbo_texture

    //  unbind vao_cube
    gl.bindVertexArray(null);

    /***********************************************************************************/
    gl.clearColor(0.0, 0.0, 0.0, 1.0);
    gl.enable(gl.DEPTH_TEST);
    gl.enable(gl.CULL_FACE);
    
    perspectiveProjectionMatrix = mat4.create();

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

function draw()
{
    gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    
    gl.useProgram(shaderProgramObject);
    
    var modelViewMatrix = mat4.create(); 
    var modelViewProjectionMatrix = mat4.create(); 

    /*------------------------------- DRAW PYRAMID ------------------------------*/
    mat4.translate(modelViewMatrix, modelViewMatrix, [-1.5, 0.0, -5.0]);
    mat4.rotateY(modelViewMatrix ,modelViewMatrix, degToRad(angle_pyr));

    mat4.multiply(modelViewProjectionMatrix, perspectiveProjectionMatrix, modelViewMatrix);

    gl.uniformMatrix4fv(mvpUniform, false, modelViewProjectionMatrix);

    // Bind with pyramid's texture!
    gl.bindTexture(gl.TEXTURE_2D, pyramid_texture);
    gl.uniform1i(uniform_texture0_sampler, 0);
    //  Bind VAO to draw now
    gl.bindVertexArray(vao_pyramid);
    gl.drawArrays(gl.TRIANGLES, 0, 12); 
    gl.bindVertexArray(null);
    
    /*------------------------------- DRAW CUBE ------------------------------*/
    mat4.identity(modelViewMatrix); 
    mat4.identity(modelViewProjectionMatrix); 
    
    mat4.translate(modelViewMatrix, modelViewMatrix, [1.5, 0.0, -5.0]);
    mat4.rotateX(modelViewMatrix ,modelViewMatrix, degToRad(angle_cube));
    mat4.rotateY(modelViewMatrix ,modelViewMatrix, degToRad(angle_cube));
    mat4.rotateZ(modelViewMatrix ,modelViewMatrix, degToRad(angle_cube));
    
    mat4.multiply(modelViewProjectionMatrix,perspectiveProjectionMatrix,modelViewMatrix);
   
    gl.uniformMatrix4fv(mvpUniform,false,modelViewProjectionMatrix);
    
    // Bind with cube's texture
    gl.bindTexture(gl.TEXTURE_2D, cube_texture);
    gl.uniform1i(uniform_texture0_sampler, 0);
    
    //  start drawing cube
    gl.bindVertexArray(vao_cube);
    
    gl.drawArrays(gl.TRIANGLE_FAN, 0, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 4, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 8, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 12, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 16, 4);
    gl.drawArrays(gl.TRIANGLE_FAN, 20, 4);
    
    gl.bindVertexArray(null);
    //  unbind with the shader program
    gl.useProgram(null);
    
    update();
    // animation loop
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

    if(vbo_position)
    {
        gl.deleteBuffer(vbo_position);
        vbo_position = null;
    }

    if(vbo_texture)
    {
        gl.deleteBuffer(vbo_texture);
        vbo_texture = null;
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
