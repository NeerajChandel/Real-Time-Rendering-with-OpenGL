#import <Foundation/Foundation.h>
#import <Cocoa/Cocoa.h>
#import <QuartzCore/CVDisplayLink.h>
#import <OpenGL/gl3.h>
#import <OpenGL/gl3ext.h>
#import "vmath.h"

enum
{
    NRC_ATTRIBUTE_VERTEX = 0,
    NRC_ATTRIBUTE_COLOR,
    NRC_ATTRIBUTE_NORMAL,
    NRC_ATTRIBUTE_TEXTURE0,  
};

CVReturn MyDisplayLinkCallback(CVDisplayLinkRef, const CVTimeStamp *, const CVTimeStamp *, CVOptionFlags, CVOptionFlags *, void *);
void update(void);

FILE *gpFile = NULL;
//  for rotation
GLfloat angle_pyramid = 0.0f;
GLfloat angle_cube = 0.0f;

@interface AppDelegate : NSObject <NSApplicationDelegate, NSWindowDelegate>
@end

@interface GLView: NSOpenGLView
@end

int main(int argc, const char *argv[])
{
    NSAutoreleasePool *pPool = [[NSAutoreleasePool alloc]init];

    NSApp = [NSApplication sharedApplication];

    [NSApp setDelegate:[[AppDelegate alloc]init]];
    
    [NSApp run];

    [pPool release];

    return(0);
}

@implementation AppDelegate
{
    @private
        NSWindow *window;
        GLView *glView;
}

-(void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    NSBundle *mainBundle = [NSBundle mainBundle];
    NSString *appDirName = [mainBundle bundlePath];
    NSString *parentDirPath = [appDirName stringByDeletingLastPathComponent];
    NSString *logFileNameWithPath = [NSString stringWithFormat:@"%@/Log.txt", parentDirPath];
    const char *pszLogFileNameWithPath = [logFileNameWithPath cStringUsingEncoding:NSASCIIStringEncoding];

    gpFile = fopen(pszLogFileNameWithPath, "w");

    if(gpFile == NULL)
    {
        printf("Cannot create log file.\nExiting...\n");
        [self release];
        [NSApp terminate:self];
    }

    fprintf(gpFile, "Program started successfully...\n");

    //  window
    NSRect win_rect;
    win_rect = NSMakeRect(0.0, 0.0, 800.0, 600.0);

    //  create simple window
    window = [[NSWindow alloc]initWithContentRect:win_rect styleMask:NSWindowStyleMaskTitled | NSWindowStyleMaskClosable | NSWindowStyleMaskMiniaturizable | NSWindowStyleMaskResizable backing:NSBackingStoreBuffered defer:NO];
    [window setTitle:@"macOS OpenGL 3D Rotation"];
    [window center];

    glView = [[GLView alloc]initWithFrame:win_rect];

    [window setContentView:glView];
    [window setDelegate:self];
    [window makeKeyAndOrderFront: self];
}

-(void)applicationWillTerminate:(NSNotification *)notification
{
    fprintf(gpFile, "Program terminated successfully...\n");

    if(gpFile)
    {
        fclose(gpFile);
        gpFile = NULL;
    }
}

-(void)windowWillClose:(NSNotification *)notification
{
    [NSApp terminate:self];
}

-(void)dealloc
{
    [glView release];

    [window release];

    [super dealloc];
}
@end

@implementation GLView
{
    @private
        CVDisplayLinkRef displayLink;
        /* *******Shader Objects********* */
        GLuint gVertexShaderObject;
        GLuint gFragmentShaderObject;
        GLuint gShaderProgramObject;

        /* *VAO and VBO* */
        GLuint gVaoPyramid;
        GLuint gVaoCube;
        GLuint gVboPosition;
        GLuint gVboColor;
        GLuint gMVPUniform;

        vmath::mat4 gPerspectiveProjectionMatrix;
}

-(id)initWithFrame:(NSRect)frame
{
    self = [super initWithFrame:frame];

    if(self)
    {
        [[self window]setContentView:self];
        NSOpenGLPixelFormatAttribute attrs[] = 
        {
            NSOpenGLPFAOpenGLProfile,
            NSOpenGLProfileVersion4_1Core,
            NSOpenGLPFAScreenMask, CGDisplayIDToOpenGLDisplayMask(kCGDirectMainDisplay),
            NSOpenGLPFANoRecovery,
            NSOpenGLPFAAccelerated,
            NSOpenGLPFAColorSize, 24,
            NSOpenGLPFADepthSize, 24,
            NSOpenGLPFAAlphaSize, 8,
            NSOpenGLPFADoubleBuffer,
            0
        };

        NSOpenGLPixelFormat *pixelFormat = [[[NSOpenGLPixelFormat alloc]initWithAttributes:attrs]autorelease];    

        if(pixelFormat == nil)
        {
            fprintf(gpFile, "No valid OpenGL Pixel Format is available. Exiting...\n");
            [self release];
            [NSApp terminate: self];
        }

        NSOpenGLContext *glContext = [[[NSOpenGLContext alloc]initWithFormat:pixelFormat shareContext:nil]autorelease];

        [self setPixelFormat:pixelFormat];
        [self setOpenGLContext:glContext];
    }
    return(self);
}

-(CVReturn)getFrameForTime:(const CVTimeStamp *)pOutputTime
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc]init];

    [self drawView];
    [pool release];

    return(kCVReturnSuccess);
}

-(void)prepareOpenGL
{
    fprintf(gpFile, "OpenGL Version :   %s\n", glGetString(GL_VERSION));
    fprintf(gpFile, "GLSL Version   :   %s\n", glGetString(GL_SHADING_LANGUAGE_VERSION));

    [[self openGLContext]makeCurrentContext];

    GLint swapInt = 1;

    [[self openGLContext]setValues:&swapInt forParameter:NSOpenGLCPSwapInterval];

    /*-----------------------------------------------------------------------------------------------------*/

    /* **** VERTEX SHADER **** */

    gVertexShaderObject = glCreateShader(GL_VERTEX_SHADER);

    const GLchar *vertexShaderSourceCode =
        "#version 410 core" \
        "\n" \
        "in vec4 vPosition;" \
        "in vec4 vColor;"   \
        "uniform mat4 u_mvp_matrix;" \
        "out vec4 out_color;"   \
        "void main(void)" \
        "{" \
        "gl_Position = u_mvp_matrix * vPosition;" \
        "out_color = vColor;"   \
        "}";

    glShaderSource(gVertexShaderObject, 1, (const GLchar **)&vertexShaderSourceCode, NULL);
    glCompileShader(gVertexShaderObject);

    //  error checking for compilation errors:
    GLint iInfoLogLength = 0;
    GLint iShaderCompiledStatus = 0;
    char *szInfoLog = NULL;
    glGetShaderiv(gVertexShaderObject, GL_COMPILE_STATUS, &iShaderCompiledStatus);
    if (iShaderCompiledStatus == GL_FALSE)
    {
        glGetShaderiv(gVertexShaderObject, GL_INFO_LOG_LENGTH, &iInfoLogLength);
        if (iInfoLogLength > 0)
        {
            szInfoLog = (char *)malloc(iInfoLogLength);
            if (szInfoLog != NULL)
            {
                GLsizei written;
                glGetShaderInfoLog(gVertexShaderObject, iInfoLogLength, &written, szInfoLog);
                fprintf(gpFile, "Vertex Shader Compilation Log: %s \n", szInfoLog);
                free(szInfoLog);
                [self release];
                [NSApp terminate:self];
            }
        }
    }


    /* VERTEX SHADER ENDS */

    /*-----------------------------------------------------------------------------------------------------*/

    /* **** FRAGMENT SHADER **** */

    gFragmentShaderObject = glCreateShader(GL_FRAGMENT_SHADER);

    const GLchar *fragmentShaderSourceCode =
        "#version 410 core" \
        "\n" \
        "in vec4 out_color;"    \
        "out vec4 FragColor;" \
        "void main(void)" \
        "{" \
        "FragColor = out_color;" \
        "}";

    glShaderSource(gFragmentShaderObject, 1, (const GLchar **)&fragmentShaderSourceCode, NULL);
    glCompileShader(gFragmentShaderObject);

    iInfoLogLength = 0;
    iShaderCompiledStatus = 0;
    szInfoLog = NULL;
    glGetShaderiv(gFragmentShaderObject, GL_COMPILE_STATUS, &iShaderCompiledStatus);
    if (iShaderCompiledStatus == GL_FALSE)
    {
        glGetShaderiv(gFragmentShaderObject, GL_INFO_LOG_LENGTH, &iInfoLogLength);
        if (iInfoLogLength > 0)
        {
            szInfoLog = (char *)malloc(iInfoLogLength);
            if (szInfoLog != NULL)
            {
                GLsizei written;
                glGetShaderInfoLog(gFragmentShaderObject, iInfoLogLength, &written, szInfoLog);
                fprintf(gpFile, "Fragment Shader Compilation Log: %s \n", szInfoLog);
                free(szInfoLog);
                [self release];
                [NSApp terminate:self];
            }
        }
    }

    /* FRAGMENT SHADER ENDS */

    /*-----------------------------------------------------------------------------------------------------*/

    /* **** SHADER PROGRAM **** */

    gShaderProgramObject = glCreateProgram();

    glAttachShader(gShaderProgramObject, gVertexShaderObject);
    glAttachShader(gShaderProgramObject, gFragmentShaderObject);

    //  binding "in" attributes before linking
    glBindAttribLocation(gShaderProgramObject, NRC_ATTRIBUTE_VERTEX, "vPosition");
    glBindAttribLocation(gShaderProgramObject, NRC_ATTRIBUTE_COLOR, "vColor");

    glLinkProgram(gShaderProgramObject);

    //  error checking
    GLint iShaderProgramLinkStatus = 0;
    iInfoLogLength = 0;
    glGetProgramiv(gShaderProgramObject, GL_LINK_STATUS, &iShaderProgramLinkStatus);
    if (iShaderProgramLinkStatus == GL_FALSE)
    {
        glGetProgramiv(gShaderProgramObject, GL_INFO_LOG_LENGTH, &iInfoLogLength);
        if (iInfoLogLength > 0)
        {
            szInfoLog = (char *)malloc(iInfoLogLength);
            if (szInfoLog != NULL)
            {
                GLsizei written;
                glGetProgramInfoLog(gShaderProgramObject, iInfoLogLength, &written, szInfoLog);
                fprintf(gpFile, "Shader Program Link Log: %s\n", szInfoLog);
                free(szInfoLog);
                [self release];
                [NSApp terminate:self];
            }
        }
    }

    //  obtaining "uniform" after linking
    gMVPUniform = glGetUniformLocation(gShaderProgramObject, "u_mvp_matrix");

    /*-----------------------------------------------------------------------------------------------------*/

    /* VBO and VAO initialization */
    //  Vertex positions for triangle
    const GLfloat pyramidVertices[] =
    { 
        0.0f, 1.0f, 0.0f,   // front face
        -1.0f, -1.0f, 1.0f, 
        1.0f, -1.0f, 1.0f,  
        0.0f, 1.0f, 0.0f,   // right face
        1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        0.0f, 1.0f, 0.0f,   // back face
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        0.0f, 1.0f, 0.0f,   // left face
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f
    };

    //  Color Value Array
    const GLfloat pyramidColor[] =
    {
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f
    };

    //  Vertex positions for square
    const GLfloat cubeVertices[] =
    { 
        1.0f, 1.0f, 1.0f,   // front face
        -1.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,  // right face
        1.0f, 1.0f, 1.0f,
        1.0f, -1.0f, 1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,  //left face
        -1.0f, 1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f,
        -1.0f, 1.0f, -1.0f, // back face
        1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, 1.0f, 1.0f,  // top face
        1.0f, 1.0f, 1.0f,
        1.0f, 1.0f, -1.0f,
        -1.0f, 1.0f, -1.0f,
        1.0f, -1.0f, 1.0f,  // bottom face
        1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, 1.0f
    };

    const GLfloat cubeColor[] =
    {
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f,   //  red - front
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,   //  green - right
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,   //  blue - left
        1.0f, 0.5f, 0.0f,
        1.0f, 0.5f, 0.0f,
        1.0f, 0.5f, 0.0f,
        1.0f, 0.5f, 0.0f,   //  back
        0.0f, 1.0f, 0.5f,
        0.0f, 1.0f, 0.5f,
        0.0f, 1.0f, 0.5f,
        0.0f, 1.0f, 0.5f,   //  top
        0.5f, 0.0f, 1.0f,
        0.5f, 0.0f, 1.0f,
        0.5f, 0.0f, 1.0f,
        0.5f, 0.0f, 1.0f    //  bottom
    };

    /*------------------ PYRAMID VAO--------------------*/
    //  VAO for pyramid starts
    glGenVertexArrays(1, &gVaoPyramid);
    glBindVertexArray(gVaoPyramid);
    
    //  VBO - Position starts
    glGenBuffers(1, &gVboPosition);
    glBindBuffer(GL_ARRAY_BUFFER, gVboPosition);
    glBufferData(GL_ARRAY_BUFFER, sizeof(pyramidVertices), pyramidVertices, GL_STATIC_DRAW);
    glVertexAttribPointer(NRC_ATTRIBUTE_VERTEX, 3, GL_FLOAT, GL_FALSE, 0, NULL);
    glEnableVertexAttribArray(NRC_ATTRIBUTE_VERTEX);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    //  VBO - Position ends

    //  VBO - Color Starts
    glGenBuffers(1, &gVboColor);
    glBindBuffer(GL_ARRAY_BUFFER, gVboColor);
    glBufferData(GL_ARRAY_BUFFER, sizeof(pyramidColor), pyramidColor, GL_STATIC_DRAW);
    glVertexAttribPointer(NRC_ATTRIBUTE_COLOR, 3, GL_FLOAT, GL_FALSE, 0, NULL);
    glEnableVertexAttribArray(NRC_ATTRIBUTE_COLOR);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    //  VBO - Color Ends

    //  VAO for pyramid ends
    glBindVertexArray(0);
    
    /*------------------- CUBE VAO ------------------------------*/

    //  VAO for cube starts
    glGenVertexArrays(1, &gVaoCube);
    glBindVertexArray(gVaoCube);

    //  VBO - Position starts
    glGenBuffers(1, &gVboPosition);
    glBindBuffer(GL_ARRAY_BUFFER, gVboPosition);
    glBufferData(GL_ARRAY_BUFFER, sizeof(cubeVertices), cubeVertices, GL_STATIC_DRAW);
    glVertexAttribPointer(NRC_ATTRIBUTE_VERTEX, 3, GL_FLOAT, GL_FALSE, 0, NULL);
    glEnableVertexAttribArray(NRC_ATTRIBUTE_VERTEX);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    //  VBO - Position ends

    //  VBO - Color Starts
    glGenBuffers(1, &gVboColor);
    glBindBuffer(GL_ARRAY_BUFFER, gVboColor);
    glBufferData(GL_ARRAY_BUFFER, sizeof(cubeColor), cubeColor, GL_STATIC_DRAW);
    glVertexAttribPointer(NRC_ATTRIBUTE_COLOR, 3, GL_FLOAT, GL_FALSE, 0, NULL);
    glEnableVertexAttribArray(NRC_ATTRIBUTE_COLOR);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    //  VBO - Color Ends
    
    //  VAO for cube ends
    glBindVertexArray(0);
    
    /*----------------------------------------------------------------------*/

    //enabling depth and performing depth tests!
    //glShadeModel(GL_SMOOTH);
    glClearDepth(1.0f);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    //glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    //glEnable(GL_CULL_FACE);

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    gPerspectiveProjectionMatrix = vmath::mat4::identity();

    CVDisplayLinkCreateWithActiveCGDisplays(&displayLink);
    CVDisplayLinkSetOutputCallback(displayLink, &MyDisplayLinkCallback, self);
    CGLContextObj cglContext = (CGLContextObj)[[self openGLContext]CGLContextObj];
    CGLPixelFormatObj cglPixelFormat = (CGLPixelFormatObj)[[self pixelFormat]CGLPixelFormatObj];
    CVDisplayLinkSetCurrentCGDisplayFromOpenGLContext(displayLink, cglContext, cglPixelFormat);
    CVDisplayLinkStart(displayLink);  
}

-(void)reshape
{
    CGLLockContext((CGLContextObj)[[self openGLContext]CGLContextObj]);

    NSRect rect = [self bounds];

    GLfloat width = rect.size.width;
    GLfloat height = rect.size.height;

    if(height == 0)
        height = 1;

    glViewport(0, 0, (GLsizei)width, (GLsizei)height);

    gPerspectiveProjectionMatrix = vmath::perspective(45.0f, (GLfloat)width/(GLfloat)height, 0.1f, 100.0f);

    CGLUnlockContext((CGLContextObj)[[self openGLContext]CGLContextObj]);
}

-(void)drawRect:(NSRect)dirtyRect
{
    [self drawView];
}

-(void)drawView
{
    [[self openGLContext]makeCurrentContext];

    CGLLockContext((CGLContextObj)[[self openGLContext]CGLContextObj]);

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    //OPENGL-DRAW
    glUseProgram(gShaderProgramObject);

    //  drawing pyramid
    vmath::mat4 modelViewMatrix = vmath::mat4::identity();
    vmath::mat4 modelViewProjectionMatrix = vmath::mat4::identity();
    vmath::mat4 rotationMatrix;

    //  NOTE: Order of transformation matrix multiplication - Scale -> Translate -> Rotate
    modelViewMatrix = vmath::translate(-1.5f, 0.0f, -6.0f);
    rotationMatrix = vmath::rotate(angle_pyramid, 0.0f, 1.0f, 0.0f);
    modelViewMatrix = modelViewMatrix * rotationMatrix;

    modelViewProjectionMatrix = gPerspectiveProjectionMatrix * modelViewMatrix;
    glUniformMatrix4fv(gMVPUniform, 1, GL_FALSE, modelViewProjectionMatrix);

    glBindVertexArray(gVaoPyramid);
    glDrawArrays(GL_TRIANGLES, 0, 12);
    glBindVertexArray(0);

    // PMG correction - add scale matrix and apply the scaling correctly
    // the order should be
    // 1 Translate
    // 2 Rotate
    // 3 Scale
    // mvp = projection * mv


    //  drawing cube
    modelViewMatrix = vmath::mat4::identity();
    modelViewProjectionMatrix = vmath::mat4::identity();
    vmath::mat4 scaleMatrix = vmath::mat4::identity();
    vmath::mat4 translateMatrix = vmath::mat4::identity();
    rotationMatrix = vmath::mat4::identity();
    
    // PMG correction -> multiply matrices with modelview matrix separately with the order above

    scaleMatrix = vmath::scale(0.84f, 0.84f, 0.84f);
    translateMatrix = vmath::translate(1.39f, 0.0f, -6.0f);
    rotationMatrix = vmath::rotate(angle_cube, angle_cube, angle_cube);
    modelViewMatrix = modelViewMatrix * translateMatrix;
    modelViewMatrix = modelViewMatrix * rotationMatrix;
    modelViewMatrix = modelViewMatrix * scaleMatrix;

    modelViewProjectionMatrix = gPerspectiveProjectionMatrix * modelViewMatrix;
    glUniformMatrix4fv(gMVPUniform, 1, GL_FALSE, modelViewProjectionMatrix);

    glBindVertexArray(gVaoCube);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    glDrawArrays(GL_TRIANGLE_FAN, 4, 4);
    glDrawArrays(GL_TRIANGLE_FAN, 8, 4);
    glDrawArrays(GL_TRIANGLE_FAN, 12, 4);
    glDrawArrays(GL_TRIANGLE_FAN, 16, 4);
    glDrawArrays(GL_TRIANGLE_FAN, 20, 4);
    glBindVertexArray(0);

    glUseProgram(0);
    //END-OPENGL-DRAW

    update();

    CGLFlushDrawable((CGLContextObj)[[self openGLContext]CGLContextObj]);
    CGLUnlockContext((CGLContextObj)[[self openGLContext]CGLContextObj]);
}

-(BOOL)acceptsFirstResponder
{
    [[self window]makeFirstResponder:self];
    return(YES);
}

-(void)keyDown:(NSEvent *)theEvent
{
    int key = (int)[[theEvent characters]characterAtIndex:0];

    switch(key)
    {
        case 27:    //  for escape key
            [self release];
            [NSApp terminate:self];
            break;
        
        case 'F':
        case 'f':
            [[self window]toggleFullScreen:self];
            break;
        
        default:
            break;
    }
}

-(void)mouseDown:(NSEvent *)theEvent
{
    //
}

-(void)mouseDragged:(NSEvent *)theEvent
{
    //
}

-(void)rightMouseDown:(NSEvent *)theEvent
{
    //
}

-(void)dealloc
{
    if (gVboPosition)
    {
        glDeleteBuffers(1, &gVboPosition);
        gVboPosition = 0;
    }

    if (gVboColor)
    {
        glDeleteBuffers(1, &gVboColor);
        gVboColor = 0;
    }

    if (gVaoPyramid)
    {
        glDeleteVertexArrays(1, &gVaoPyramid);
        gVaoPyramid = 0;
    }

    if (gVaoCube)
    {
        glDeleteVertexArrays(1, &gVaoCube);
        gVaoCube = 0;
    }

    glDetachShader(gShaderProgramObject, gVertexShaderObject);
    glDetachShader(gShaderProgramObject, gFragmentShaderObject);

    glDeleteShader(gVertexShaderObject);
    gVertexShaderObject = 0;
    glDeleteShader(gFragmentShaderObject);
    gFragmentShaderObject = 0;

    glDeleteProgram(gShaderProgramObject);
    gShaderProgramObject = 0;

    glUseProgram(0);

    CVDisplayLinkStop(displayLink);
    CVDisplayLinkRelease(displayLink);

    [super dealloc];
}
@end

CVReturn MyDisplayLinkCallback(CVDisplayLinkRef displayLink, const CVTimeStamp *pNow, const CVTimeStamp *pOutputTime, CVOptionFlags flagsIn, CVOptionFlags *pFlagsOut, void *pDisplayLinkContext)
{
    CVReturn result = [(GLView *)pDisplayLinkContext getFrameForTime:pOutputTime];
    return(result);
}

void update()
{
    angle_pyramid = angle_pyramid + 0.5f;
    if (angle_pyramid >= 360.0f)
        angle_pyramid = 0.0f;

    angle_cube = angle_cube - 0.5f;
    if (angle_cube <= -360.0f)
        angle_cube = 0.0f;
}


