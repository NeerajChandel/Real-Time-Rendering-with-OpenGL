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
#include "Sphere.h"

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
bool gbLight;

enum
{
	NRC_ATTRIBUTE_VERTEX = 0,
	NRC_ATTRIBUTE_COLOR,
	NRC_ATTRIBUTE_NORMAL,
	NRC_ATTRIBUTE_TEXTURE0,
};

//	Sphere stuff
float sphere_vertices[1146];
float sphere_normals[1146];
float sphere_textures[764];
unsigned short sphere_elements[2280];

GLuint gNumVertices;
GLuint gNumElements;

/* *******Shader Objects********* */
GLuint gVertexShaderObject;
GLuint gFragmentShaderObject;
GLuint gShaderProgramObject;

/* *VAO and VBO* */
GLuint gVaoSphere;
GLuint gVboPosition;
GLuint gVboNormal;
GLuint gVboElement;

/*
GLuint gModelViewMatrixUniform, gProjectionMatrixUniform;
GLuint gLdUniform, gKdUniform, gLightPositionUniform;

GLuint gLKeyPressedUniform;
*/

GLuint gModelMatrixUniform, gViewMatrixUniform, gProjectionMatrixUniform;

GLuint LKeyPressedUniform;

//	Light component uniform variables
GLuint gLaUniform;	//	ambient component - Ia
GLuint gLdUniform;	//	diffuse component - Id
GLuint gLsUniform;	//	specular component - Ts
GLuint gLightPositionUniform;

//	Material component uniform variables
GLuint gKaUniform;	//	ambient
GLuint gKdUniform;	//	diffuse
GLuint gKsUniform;	//	specular
GLuint gMaterialShininessUniform;

mat4 gPerspectiveProjectionMatrix;

GLfloat lightAmbient[] = {0.0f, 0.0f, 0.0f, 1.0f};
GLfloat lightDiffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
GLfloat lightSpecular[] = {1.0f, 1.0f, 1.0f, 1.0f};
GLfloat lightPosition[] = {100.0f, 100.0f, 100.0f, 1.0f};

GLfloat materialAmbient[] = {0.0f, 0.0f, 0.0f, 1.0f};
GLfloat materialDiffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
GLfloat materialSpecular[] = {1.0f, 1.0f, 1.0f, 1.0f};
GLfloat materialShininess = 50.0f;

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
						keySym = XkbKeycodeToKeysym(gpDisplay, event.xkey.keycode, 0, 0);
						switch(keySym)
						{
							case XK_Escape:
								bDone = true;
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

	XStoreName(gpDisplay, gWindow, "Per Fragment Lighting");

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
		"#version 430 core"	\
		"\n"	\
		"in vec4 vPosition;"	\
		"in vec3 vNormal;"	\
		"uniform mat4 u_model_matrix;"	\
		"uniform mat4 u_view_matrix;"	\
		"uniform mat4 u_projection_matrix;"	\
		"uniform vec4 u_light_position;"	\
		"uniform int u_lighting_enabled;"	\
		"out vec3 transformed_normals;"	\
		"out vec3 light_direction;"	\
		"out vec3 viewer_vector;"	\
		"void main(void)"	\
		"{"	\
		"if(u_lighting_enabled == 1)"	\
		"{"	\
		"vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	\
		"transformed_normals = mat3(u_view_matrix * u_model_matrix) * vNormal;"	\
		"light_direction = vec3(u_light_position) - eye_coordinates.xyz;"	\
		"viewer_vector = -eye_coordinates.xyz;"	\
		"}"	\
		"gl_Position = u_projection_matrix * u_view_matrix * u_model_matrix * vPosition;"	\
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
		"in vec3 transformed_normals;"	\
		"in vec3 light_direction;"	\
		"in vec3 viewer_vector;"	\
		"out vec4 FragColor;"	\
		"uniform vec3 u_La;"	\
		"uniform vec3 u_Ld;"	\
		"uniform vec3 u_Ls;"	\
		"uniform vec3 u_Ka;"	\
		"uniform vec3 u_Kd;"	\
		"uniform vec3 u_Ks;"	\
		"uniform float u_material_shininess;"	\
		"uniform int u_lighting_enabled;"	\
		"void main(void)"	\
		"{" \
		"vec3 phong_ads_color;"	\
		"if(u_lighting_enabled == 1)"	\
		"{"	\
		"vec3 normalized_transformed_normals = normalize(transformed_normals);"	\
		"vec3 normalized_light_direction = normalize(light_direction);"	\
		"vec3 normalized_viewer_vector = normalize(viewer_vector);"	\
		"vec3 ambient = u_La * u_Ka;"	\
		"float tn_dot_ld = max(dot(normalized_transformed_normals, normalized_light_direction), 0.0);"	\
		"vec3 diffuse = u_Ld * u_Kd * tn_dot_ld;"	\
		"vec3 reflection_vector = reflect(-normalized_light_direction, normalized_transformed_normals);"	\
		"vec3 specular = u_Ls * u_Ks * pow(max(dot(reflection_vector, normalized_viewer_vector), 0.0), u_material_shininess);"	\
		"phong_ads_color = ambient + diffuse + specular;"	\
		"}"	\
		"else"	\
		"{"	\
		"phong_ads_color = vec3(1.0, 1.0, 1.0);"	\
		"}"	\
		"FragColor = vec4(phong_ads_color, 1.0);"	\
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
	gViewMatrixUniform = glGetUniformLocation(gShaderProgramObject, "u_view_matrix");
	gModelMatrixUniform = glGetUniformLocation(gShaderProgramObject, "u_model_matrix");
	gProjectionMatrixUniform = glGetUniformLocation(gShaderProgramObject, "u_projection_matrix");
	LKeyPressedUniform = glGetUniformLocation(gShaderProgramObject, "u_lighting_enabled");
	gLaUniform = glGetUniformLocation(gShaderProgramObject, "u_La");
	gKaUniform = glGetUniformLocation(gShaderProgramObject, "u_Ka");
	gLdUniform = glGetUniformLocation(gShaderProgramObject, "u_Ld");
	gKdUniform = glGetUniformLocation(gShaderProgramObject, "u_Kd");
	gLsUniform = glGetUniformLocation(gShaderProgramObject, "u_Ls");
	gKsUniform = glGetUniformLocation(gShaderProgramObject, "u_Ks");
	gLightPositionUniform = glGetUniformLocation(gShaderProgramObject, "u_light_position");
	gMaterialShininessUniform = glGetUniformLocation(gShaderProgramObject, "u_material_shininess");

	/*-----------------------------------------------------------------------------------------------------*/

	/* VBO and VAO initialization */
	getSphereVertexData(sphere_vertices, sphere_normals, sphere_textures, sphere_elements);
	gNumVertices = getNumberOfSphereVertices();
	gNumElements = getNumberOfSphereElements();

	/*------------------- SPHERE VAO ------------------------------*/

	//	VAO for sphere starts
	glGenVertexArrays(1, &gVaoSphere);
	glBindVertexArray(gVaoSphere);

	//	VBO - Position starts
	glGenBuffers(1, &gVboPosition);
	glBindBuffer(GL_ARRAY_BUFFER, gVboPosition);
	glBufferData(GL_ARRAY_BUFFER, sizeof(sphere_vertices), sphere_vertices, GL_STATIC_DRAW);
	glVertexAttribPointer(NRC_ATTRIBUTE_VERTEX, 3, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(NRC_ATTRIBUTE_VERTEX);
	glBindBuffer(GL_ARRAY_BUFFER, 0);
	//	VBO - Position ends

	//	VBO - Normal Starts
	glGenBuffers(1, &gVboNormal);
	glBindBuffer(GL_ARRAY_BUFFER, gVboNormal);
	glBufferData(GL_ARRAY_BUFFER, sizeof(sphere_normals), sphere_normals, GL_STATIC_DRAW);
	glVertexAttribPointer(NRC_ATTRIBUTE_NORMAL, 3, GL_FLOAT, GL_FALSE, 0, NULL);
	glEnableVertexAttribArray(NRC_ATTRIBUTE_NORMAL);
	glBindBuffer(GL_ARRAY_BUFFER, 0);
	//	VBO - Normal Ends

	// element vbo
	glGenBuffers(1, &gVboElement);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(sphere_elements), sphere_elements, GL_STATIC_DRAW);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

	//	VAO for sphere ends
	glBindVertexArray(0);

	/*----------------------------------------------------------------------*/

	//enabling depth and performing depth tests!
	glShadeModel(GL_SMOOTH);
	glClearDepth(1.0f);
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	glEnable(GL_CULL_FACE);

	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

	gPerspectiveProjectionMatrix = mat4::identity();

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

void display(void)
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glUseProgram(gShaderProgramObject);

	if (gbLight == true)
	{
		//	set "u_lighting_enabled" uniform as '1'
		glUniform1i(LKeyPressedUniform, 1);

		//	set light's properties
		glUniform3fv(gLaUniform, 1, lightAmbient);
		glUniform3fv(gLdUniform, 1, lightDiffuse);
		glUniform3fv(gLsUniform, 1, lightSpecular);
		glUniform4fv(gLightPositionUniform, 1, lightPosition);

		//	set material's properties
		glUniform3fv(gKaUniform, 1, materialAmbient);
		glUniform3fv(gKdUniform, 1, materialDiffuse);
		glUniform3fv(gKsUniform, 1, materialSpecular);
		glUniform1f(gMaterialShininessUniform, materialShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	matrix stuff
	mat4 modelMatrix = mat4::identity();
	mat4 viewMatrix = mat4::identity();

	modelMatrix = translate(0.0f, 0.0f, -2.0f);
	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

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
	
	if (gVaoSphere)
	{
		glDeleteVertexArrays(1, &gVaoSphere);
		gVaoSphere = 0;
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
