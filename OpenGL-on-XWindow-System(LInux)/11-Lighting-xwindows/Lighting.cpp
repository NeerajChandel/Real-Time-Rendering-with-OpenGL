#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <memory.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/XKBlib.h>
#include <X11/keysym.h>

#include <GL/glew.h>
#include <GL/gl.h>
#include <GL/glx.h>

#include "vmath.h"

using namespace vmath;

#define WIN_WIDTH 800
#define WIN_HEIGHT 600

FILE *gpFile = NULL;

Display *gpDisplay = NULL;
XVisualInfo *gpXVisualInfo = NULL;
Colormap gColormap;
Window gWindow;

typedef GLXContext(*glXCreateContextAttribsARBProc)(Display*, GLXFBConfig, GLXContext, Bool, const int*);
glXCreateContextAttribsARBProc glXCreateContextAttribsARB = NULL;
GLXFBConfig gGLXFBConfig;
GLXContext gGLXContext;

bool gbFullscreen = false;
bool gbAnimate;
bool gbLight;

enum
{
	NRC_ATTRIBUTE_VERTEX = 0,
	NRC_ATTRIBUTE_COLOR,
	NRC_ATTRIBUTE_NORMAL,
	NRC_ATTRIBUTE_TEXTURE0,
};

/* *******Shader Objects********* */
GLuint gVertexShaderObject;
GLuint gFragmentShaderObject;
GLuint gShaderProgramObject;

/* *VAO and VBO* */
GLuint gVaoCube;
GLuint gVboPosition;
GLuint gVboNormal;

GLuint gModelViewMatrixUniform, gProjectionMatrixUniform;
GLuint gLdUniform, gKdUniform, gLightPositionUniform;

GLuint gLKeyPressedUniform;

//	for rotation
GLfloat angle_cube = 0.0f;

mat4 gPerspectiveProjectionMatrix;

int main(int argc, char *argv[])
{
	void CreateWindow(void);
	void ToggleFullscreen(void);
	void initialize(void);
	void resize(int,int);
	void update(void);
	void display(void);
	void uninitialize(void);

	gpFile = fopen("Log.txt", "w");
	if (gpFile == NULL)
	{
		printf("\nLogcould not be created. Exiting...\n");
		exit(0);
	}
	else
	{
		fprintf(gpFile, "\nLog file opened.\n");
	}

	CreateWindow();

	initialize();

	XEvent event;
	KeySym keySym;
	int winWidth;
	int winHeight;
	bool bDone = false;
	static bool bIsAKeyPressed = false;
	static bool bIsLKeyPressed = false;

	while(bDone == false)
	{
		while(XPending(gpDisplay))
		{
			XNextEvent(gpDisplay, &event);
			switch(event.type)
			{
				case MapNotify:
						break;
				case KeyPress:	
						keySym=XkbKeycodeToKeysym(gpDisplay, event.xkey.keycode, 0, 0);
						switch(keySym)
						{
							case XK_Escape:
								bDone = true;
								break;
							
							case XK_A:	//	for A key
							case XK_a:
								if (bIsAKeyPressed == false)
								{
									gbAnimate = true;
									bIsAKeyPressed = true;
								}
								else
								{
									gbAnimate = false;
									bIsAKeyPressed = false;
								}
								break;

							case XK_L:	//	for L key
							case XK_l:			
								if (bIsLKeyPressed == false)
								{
									gbLight = true;
									bIsLKeyPressed = true;
								}
								else
								{
									gbLight = false;
									bIsLKeyPressed = false;
								}
								break;

							case XK_F:
							case XK_f:
								if(gbFullscreen == false)
								{
									ToggleFullscreen();
									gbFullscreen = true;
								}
								else
								{
									ToggleFullscreen();
									gbFullscreen = false;
								}
								break;
							default:
								break;
						}
						break;
				case ButtonPress:
						switch(event.xbutton.button)
						{
							case 1:
								break;
							case 2:
								break;
							case 3:
								break;
							default: 
								break;
						}
						break;
				case MotionNotify:
						break;
				case ConfigureNotify:
						winWidth = event.xconfigure.width;
						winHeight = event.xconfigure.height;
						resize(winWidth, winHeight);
						break;
				case Expose:
						break;
				case DestroyNotify:
						break;
				case 33:
						bDone = true;
						break;
				default:
						break;
			}
		}
		display();
		if (gbAnimate == true)
			update();	
	}

	uninitialize();
	return(0);
}

void CreateWindow(void)
{
	void uninitialize(void);

	XSetWindowAttributes winAttribs;
	GLXFBConfig *pGLXFBConfigs = NULL;
	GLXFBConfig bestGLXFBConfig;
	XVisualInfo *pTempXVisualInfo = NULL;
	int iNumFBConfigs = 0;
	int styleMask;
	int i;

	static int frameBufferAttributes[] = {GLX_X_RENDERABLE, True, GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT, GLX_RENDER_TYPE, GLX_RGBA_BIT, GLX_X_VISUAL_TYPE, GLX_TRUE_COLOR, GLX_RED_SIZE, 8, GLX_GREEN_SIZE, 8, GLX_BLUE_SIZE, 8, GLX_ALPHA_SIZE, 8, GLX_DEPTH_SIZE, 24, GLX_STENCIL_SIZE, 8, GLX_DOUBLEBUFFER, True, None};

	gpDisplay = XOpenDisplay(NULL);
	if(gpDisplay == NULL)
	{
		printf("ERROR: Unable to obtain X Display.\n");
		uninitialize();
		exit(1);
	}

	pGLXFBConfigs = glXChooseFBConfig(gpDisplay, DefaultScreen(gpDisplay), frameBufferAttributes, &iNumFBConfigs);
	if(pGLXFBConfigs == NULL)
	{
		printf( "Failed to get valid frame buffer configuration. Exiting...\n");
		uninitialize();
		exit(1);
	}
	printf("%d Matching frame buffer configs found.\n",iNumFBConfigs);

	int bestFramebufferconfig = -1, worstFramebufferConfig = -1, bestNumberOfSamples = -1, worstNumberOfSamples = 999;
	for(i = 0; i < iNumFBConfigs; i++)
	{
		pTempXVisualInfo = glXGetVisualFromFBConfig(gpDisplay, pGLXFBConfigs[i]);
		if(pTempXVisualInfo)
		{
			int sampleBuffer,samples;

			glXGetFBConfigAttrib(gpDisplay, pGLXFBConfigs[i], GLX_SAMPLE_BUFFERS, &sampleBuffer);
			glXGetFBConfigAttrib(gpDisplay, pGLXFBConfigs[i], GLX_SAMPLES, &samples);
			printf("Matching frame buffer configuration = %d : Visual ID = 0x%lu : SAMPLE_BUFFERS = %d : SAMPLES = %d\n", i, pTempXVisualInfo->visualid, sampleBuffer, samples);

			if(bestFramebufferconfig < 0 || sampleBuffer && samples > bestNumberOfSamples)
			{
				bestFramebufferconfig = i;
				bestNumberOfSamples = samples;
			}

			if( worstFramebufferConfig < 0 || !sampleBuffer || samples < worstNumberOfSamples)
			{
				worstFramebufferConfig = i;
			    worstNumberOfSamples = samples;
			}
		}

		XFree(pTempXVisualInfo);

	}
	bestGLXFBConfig = pGLXFBConfigs[bestFramebufferconfig];
	gGLXFBConfig=bestGLXFBConfig;

	XFree(pGLXFBConfigs);

	gpXVisualInfo = glXGetVisualFromFBConfig(gpDisplay, bestGLXFBConfig);
	printf("Chosen visual ID = 0x%lu\n", gpXVisualInfo->visualid);

	winAttribs.border_pixel = 0;
	winAttribs.background_pixmap = 0;
	winAttribs.colormap = XCreateColormap(gpDisplay, RootWindow(gpDisplay, gpXVisualInfo->screen), gpXVisualInfo->visual, AllocNone);
	winAttribs.event_mask = StructureNotifyMask | KeyPressMask | ButtonPressMask | ExposureMask | VisibilityChangeMask | PointerMotionMask;
	styleMask = CWBorderPixel | CWEventMask | CWColormap;
	gColormap = winAttribs.colormap;
	gWindow = XCreateWindow(gpDisplay, RootWindow(gpDisplay, gpXVisualInfo->screen), 0, 0, WIN_WIDTH, WIN_HEIGHT, 0,
gpXVisualInfo->depth, InputOutput, gpXVisualInfo->visual, styleMask, &winAttribs);

	if(!gWindow)
	{
		printf("Could not create window.\n");
		uninitialize();
		exit(1);
	}

	XStoreName(gpDisplay, gWindow, "GLEW Window");

	Atom windowManagerDelete = XInternAtom(gpDisplay, "WM_WINDOW_DELETE", True);
	XSetWMProtocols(gpDisplay, gWindow, &windowManagerDelete, 1);

	XMapWindow(gpDisplay, gWindow);
}

void ToggleFullscreen(void)
{
	Atom wm_state = XInternAtom(gpDisplay, "_NET_WM_STATE", False);
	XEvent event;
	memset(&event, 0, sizeof(XEvent));

	event.type = ClientMessage;
	event.xclient.window = gWindow;
	event.xclient.message_type = wm_state;
	event.xclient.format = 32;
	event.xclient.data.l[0] = gbFullscreen ? 0 : 1;

	Atom fullscreen = XInternAtom(gpDisplay, "_NET_WM_STATE_FULLSCREEN", False);
	event.xclient.data.l[1] = fullscreen;

	XSendEvent(gpDisplay, RootWindow(gpDisplay, gpXVisualInfo->screen), False, StructureNotifyMask, &event);
}

void initialize(void)
{
	void uninitialize(void);
	void resize(int,int);

	glXCreateContextAttribsARB = (glXCreateContextAttribsARBProc)glXGetProcAddressARB((GLubyte *)"glXCreateContextAttribsARB");

	GLint attribs[] = {GLX_CONTEXT_MAJOR_VERSION_ARB, 4, GLX_CONTEXT_MINOR_VERSION_ARB, 5, GLX_CONTEXT_PROFILE_MASK_ARB, GLX_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB, 0};

	gGLXContext = glXCreateContextAttribsARB(gpDisplay, gGLXFBConfig, 0, True, attribs);

	if(!gGLXContext)
	{
		GLint attribs[] = {GLX_CONTEXT_MAJOR_VERSION_ARB, 1, GLX_CONTEXT_MINOR_VERSION_ARB, 0, 0};
		printf("Failed to create GLX 4.5 context. Hence, using old-style GLX context\n");
		gGLXContext = glXCreateContextAttribsARB(gpDisplay, gGLXFBConfig, 0, True, attribs);
	}
	else
	{
		printf("OpenGL context 4.5 Is Created.\n");
	}

	if(!glXIsDirect(gpDisplay, gGLXContext))
	{
		printf("Indirect GLX rendering context obtained\n");
	}
	else
	{
		printf("Direct GLX rendering context obtained\n" );
	}

	glXMakeCurrent(gpDisplay, gWindow, gGLXContext);

	GLenum glew_error = glewInit();
	if(glew_error != GLEW_OK)
	{
		glXDestroyContext(gpDisplay, gGLXContext);
	}

	/*-----------------------------------------------------------------------------------------------------*/

	/* **** VERTEX SHADER **** */

	gVertexShaderObject = glCreateShader(GL_VERTEX_SHADER);

	const GLchar *vertexShaderSourceCode =
		"#version 430 core" \
		"\n" \
		"in vec4 vPosition;" \
		"in vec3 vNormal;"	\
		"uniform mat4 u_model_view_matrix;"	\
		"uniform mat4 u_projection_matrix;"	\
		"uniform int u_LKeyPressed;"	\
		"uniform vec3 u_Ld;"	\
		"uniform vec3 u_Kd;"	\
		"uniform vec4 u_light_position;"	\
		"out vec3 diffuse_light;"	\
		"void main(void)" \
		"{" \
		"if (u_LKeyPressed == 1)"	\
		"{"	\
		"vec4 eyeCoordinates = u_model_view_matrix * vPosition;"	\
		"vec3 tnorm = normalize(mat3(u_model_view_matrix) * vNormal);"	\
		"vec3 s = normalize(vec3(u_light_position - eyeCoordinates));"	\
		"diffuse_light = u_Ld * u_Kd * max(dot(s, tnorm), 0.0);"	\
		"}"	\
		"gl_Position = u_projection_matrix * u_model_view_matrix * vPosition;" \
		"}";

	glShaderSource(gVertexShaderObject, 1, (const GLchar **)&vertexShaderSourceCode, NULL);
	glCompileShader(gVertexShaderObject);

	//	error checking for compilation errors:
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
				uninitialize();
				exit(0);
			}
		}
	}


	/* VERTEX SHADER ENDS */

	/*-----------------------------------------------------------------------------------------------------*/

	/* **** FRAGMENT SHADER **** */

	gFragmentShaderObject = glCreateShader(GL_FRAGMENT_SHADER);

	const GLchar *fragmentShaderSourceCode =
		"#version 430 core" \
		"\n" \
		"in vec3 diffuse_light;"	\
		"out vec4 FragColor;" \
		"uniform int u_LKeyPressed;"	\
		"void main(void)" \
		"{" \
		"vec4 color;" \
		"if (u_LKeyPressed == 1)"	\
		"{"	\
		"color = vec4(diffuse_light, 1.0);"	\
		"}"	\
		"else"	\
		"{"	\
		"color = vec4(1.0, 1.0, 1.0, 1.0);"	\
		"}"	\
		"FragColor = color;"	\
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
				uninitialize();
				exit(0);
			}
		}
	}

	/* FRAGMENT SHADER ENDS */

	/*-----------------------------------------------------------------------------------------------------*/

	/* **** SHADER PROGRAM **** */

	gShaderProgramObject = glCreateProgram();

	glAttachShader(gShaderProgramObject, gVertexShaderObject);
	glAttachShader(gShaderProgramObject, gFragmentShaderObject);

	//	binding "in" attributes before linking
	glBindAttribLocation(gShaderProgramObject, NRC_ATTRIBUTE_VERTEX, "vPosition");
	glBindAttribLocation(gShaderProgramObject, NRC_ATTRIBUTE_NORMAL, "vNormal");

	glLinkProgram(gShaderProgramObject);

	//	error checking
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
				uninitialize();
				exit(0);
			}
		}
	}

	//	obtaining "uniform" after linking
	gModelViewMatrixUniform = glGetUniformLocation(gShaderProgramObject, "u_model_view_matrix");
	gProjectionMatrixUniform = glGetUniformLocation(gShaderProgramObject, "u_projection_matrix");
	gLKeyPressedUniform = glGetUniformLocation(gShaderProgramObject, "u_LKeyPressed");
	gLdUniform = glGetUniformLocation(gShaderProgramObject, "u_Ld");
	gKdUniform = glGetUniformLocation(gShaderProgramObject, "u_Kd");
	gLightPositionUniform = glGetUniformLocation(gShaderProgramObject, "u_light_position");


	/*-----------------------------------------------------------------------------------------------------*/

	/* VBO and VAO initialization */
	//	Vertex positions for cube
	const GLfloat cubeVertices[] =
	{
		1.0f, 1.0f, -1.0f,
		-1.0f, 1.0f, -1.0f,
		-1.0f, 1.0f, 1.0f,
		1.0f, 1.0f, 1.0f,

		1.0f, -1.0f, 1.0f,
		-1.0f, -1.0f, 1.0f,
		-1.0f, -1.0f, -1.0f,
		1.0f, -1.0f, -1.0f,

		1.0f, 1.0f, 1.0f,
		-1.0f, 1.0f, 1.0f,
		-1.0f, -1.0f, 1.0f,
		1.0f, -1.0f, 1.0f,

		1.0f, -1.0f, -1.0f,
		-1.0f, -1.0f, -1.0f,
		-1.0f, 1.0f, -1.0f,
		1.0f, 1.0f, -1.0f,

		-1.0f, 1.0f, 1.0f,
		-1.0f, 1.0f, -1.0f,
		-1.0f, -1.0f, -1.0f,
		-1.0f, -1.0f, 1.0f,

		1.0f, 1.0f, -1.0f,
		1.0f, 1.0f, 1.0f,
		1.0f, -1.0f, 1.0f,
		1.0f, -1.0f, -1.0f,
	};

	const GLfloat cubeNormals[] =
	{
		0.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f,
		0.0f, 1.0f, 0.0f,

		0.0f, -1.0f, 0.0f,
		0.0f, -1.0f, 0.0f,
		0.0f, -1.0f, 0.0f,
		0.0f, -1.0f, 0.0f,

		0.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f,
		0.0f, 0.0f, 1.0f,

		0.0f, 0.0f, -1.0f,
		0.0f, 0.0f, -1.0f,
		0.0f, 0.0f, -1.0f,
		0.0f, 0.0f, -1.0f,

		-1.0f, 0.0f, 0.0f,
		-1.0f, 0.0f, 0.0f,
		-1.0f, 0.0f, 0.0f,
		-1.0f, 0.0f, 0.0f,

		1.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f,
		1.0f, 0.0f, 0.0f
	};

	/*------------------- CUBE VAO ------------------------------*/

	//	VAO for cube starts
	glGenVertexArrays(1, &gVaoCube);
	glBindVertexArray(gVaoCube);

	//	VBO - Position starts
	glGenBuffers(1, &gVboPosition);
	glBindBuffer(GL_ARRAY_BUFFER, gVboPosition);
	glBufferData(GL_ARRAY_BUFFER, sizeof(cubeVertices), cubeVertices, GL_STATIC_DRAW);
	glVertexAttribPointer(NRC_ATTRIBUTE_VERTEX, 3, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(NRC_ATTRIBUTE_VERTEX);
	glBindBuffer(GL_ARRAY_BUFFER, 0);
	//	VBO - Position ends

	//	VBO - Normal Starts
	glGenBuffers(1, &gVboNormal);
	glBindBuffer(GL_ARRAY_BUFFER, gVboNormal);
	glBufferData(GL_ARRAY_BUFFER, sizeof(cubeNormals), cubeNormals, GL_STATIC_DRAW);
	glVertexAttribPointer(NRC_ATTRIBUTE_NORMAL, 3, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(NRC_ATTRIBUTE_NORMAL);
	glBindBuffer(GL_ARRAY_BUFFER, 0);
	//	VBO - Normal Ends

	//	VAO for cube ends
	glBindVertexArray(0);

	/*----------------------------------------------------------------------*/

	//enabling depth and performing depth tests!
	glShadeModel(GL_SMOOTH);
	glClearDepth(1.0f);
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);
	//glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	//glEnable(GL_CULL_FACE);

	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

	gPerspectiveProjectionMatrix = mat4::identity();

	gbAnimate = false;
	gbLight = false;

	resize(WIN_WIDTH, WIN_HEIGHT);
}

void resize(int width, int height)
{
	if(height == 0)
		height = 1;

	glViewport(0, 0, (GLsizei)width, (GLsizei)height);
	gPerspectiveProjectionMatrix = perspective(45.0f, (GLfloat)width / (GLfloat)height, 0.1f, 100.0f);
}

void update(void)
{

	angle_cube = angle_cube - 0.03f;
	if (angle_cube <= -360.0f)
		angle_cube = 0.0f;
}

void display(void)
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glUseProgram(gShaderProgramObject);

	if (gbLight == true)
	{
		glUniform1i(gLKeyPressedUniform, 1);
		glUniform3f(gLdUniform, 1.0f, 1.0f, 1.0f);
		glUniform3f(gKdUniform, 0.5f, 0.5f, 0.5f);

		float lightPosition[] = {0.0f, 0.0f, 2.0f, 1.0f};
		glUniform4fv(gLightPositionUniform, 1, (GLfloat *)lightPosition);
	}
	else
	{
		glUniform1i(gLKeyPressedUniform, 0);
	}


	//	drawing cube
	mat4 modelMatrix = mat4::identity();
	mat4 modelViewMatrix = mat4::identity();
	mat4 rotationMatrix = mat4::identity();

	modelMatrix = translate(0.0f, 0.0f, -5.0f);
	rotationMatrix = vmath::rotate(angle_cube, angle_cube, angle_cube);


	modelViewMatrix = modelMatrix * rotationMatrix;
	glUniformMatrix4fv(gModelViewMatrixUniform, 1, GL_FALSE, modelViewMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

	glBindVertexArray(gVaoCube);

	glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
	glDrawArrays(GL_TRIANGLE_FAN, 4, 4);
	glDrawArrays(GL_TRIANGLE_FAN, 8, 4);
	glDrawArrays(GL_TRIANGLE_FAN, 12, 4);
	glDrawArrays(GL_TRIANGLE_FAN, 16, 4);
	glDrawArrays(GL_TRIANGLE_FAN, 20, 4);

	glBindVertexArray(0);

	glUseProgram(0);

	glXSwapBuffers(gpDisplay, gWindow);
}

void uninitialize(void)
{

	if (gVboPosition)
	{
		glDeleteBuffers(1, &gVboPosition);
		gVboPosition = 0;
	}

	if (gVboNormal)
	{
		glDeleteBuffers(1, &gVboNormal);
		gVboNormal = 0;
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

	GLXContext currentContext = glXGetCurrentContext();
	if(currentContext != NULL && currentContext == gGLXContext)
	{
		glXMakeCurrent(gpDisplay, 0, 0);
	}

	if(gGLXContext)
	{
		glXDestroyContext(gpDisplay, gGLXContext);
	}

	if(gWindow)
	{
		XDestroyWindow(gpDisplay, gWindow);
	}

	if(gColormap)
	{
		XFreeColormap(gpDisplay, gColormap);
	}

	if(gpXVisualInfo)
	{
		free(gpXVisualInfo);
		gpXVisualInfo = NULL;
	}

	if(gpDisplay)
	{
		XCloseDisplay(gpDisplay);
		gpDisplay = NULL;
	}

	if (gpFile)
	{
		fprintf(gpFile, "Log file closed.\n");
		fclose(gpFile);
		gpFile = NULL;
	}
}
