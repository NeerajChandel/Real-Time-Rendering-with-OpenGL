#include<windows.h>
#include<gl\glew.h>
#include<gl\GL.h>
#include<stdio.h>

#include "vmath.h"
#include "Sphere.h"

using namespace vmath;

#define WIN_WIDTH 800
#define WIN_HEIGHT 600
#define PI 3.1415926535898f

//#define PI 3.1415926535898f
//GLfloat actual_rotate = 0.0f;

#pragma comment(lib, "glew32.lib")
#pragma comment(lib, "opengl32.lib")
#pragma comment(lib, "Sphere.lib")

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);

HWND ghwnd = NULL;
HDC ghdc = NULL;
HGLRC ghrc = NULL;

DWORD dwStyle;
WINDOWPLACEMENT wpPrev = { sizeof(WINDOWPLACEMENT) };

bool gbActiveWindow = false;
bool gbEscapeKeyIsPressed = false;
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

//	Light component uniform variables - red light
GLuint gLaUniformRed;	//	ambient component - Ia
GLuint gLdUniformRed;	//	diffuse component - Id
GLuint gLsUniformRed;	//	specular component - Ts
GLuint gLightPositionUniformRed;

//	Light component uniform variables - blue light
GLuint gLaUniformBlue;	//	ambient component - Ia
GLuint gLdUniformBlue;	//	diffuse component - Id
GLuint gLsUniformBlue;	//	specular component - Ts
GLuint gLightPositionUniformBlue;

//	Light component uniform variables
GLuint gLaUniformGreen;	//	ambient component - Ia
GLuint gLdUniformGreen;	//	diffuse component - Id
GLuint gLsUniformGreen;	//	specular component - Ts
GLuint gLightPositionUniformGreen;

//	Material component uniform variables
GLuint gKaUniform;	//	ambient
GLuint gKdUniform;	//	diffuse
GLuint gKsUniform;	//	specular
GLuint gMaterialShininessUniform;

mat4 gPerspectiveProjectionMatrix;

//	light1 is red light
GLfloat light1Ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat light1Diffuse[] = { 1.0f, 0.0f, 0.0f, 1.0f };
GLfloat light1Specular[] = { 1.0f, 0.0f, 0.0f, 1.0f };
GLfloat light1Position[] = { 0.0f, 0.0f, 0.0f, 1.0f };

//	light2 - blue light 
GLfloat light2Ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat light2Diffuse[] = { 0.0f, 0.0f, 1.0f, 1.0f };
GLfloat light2Specular[] = { 0.0f, 0.0f, 1.0f, 1.0f };
GLfloat light2Position[] = { 0.0f, 0.0f, 0.0f, 1.0f };

//	light3 - green light
GLfloat light3Ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat light3Diffuse[] = { 0.0f, 1.0f, 0.0f, 1.0f };
GLfloat light3Specular[] = { 0.0f, 1.0f, 0.0f, 1.0f };
GLfloat light3Position[] = { 0.0f, 0.0f, 0.0f, 0.0f };

GLfloat materialAmbient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat materialDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat materialSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat materialShininess = 50.0f;

GLfloat angleRed = 0.0f;
GLfloat angleBlue = 0.0f;
GLfloat angleGreen = 0.0f;

FILE* gpFile = NULL;

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpszCmdLine, int iCmdShow)
{
	void initialize(void);
	void uninitialize(void);
	void display(void);
	void update(void);
	TCHAR str[255];

	fopen_s(&gpFile, "Log.txt", "w");
	if (gpFile == NULL)
	{
		MessageBox(NULL, str, TEXT("Could not open log file."), MB_OK);
		exit(0);
	}
	else
	{
		fprintf(gpFile, "Log File Is Successfully Opened.\n");
	}

	WNDCLASSEX wndclass;
	HWND hwnd;
	MSG msg;
	TCHAR szClassName[] = TEXT("RTROGL");
	bool bDone = false;

	wndclass.cbSize = sizeof(WNDCLASSEX);
	wndclass.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
	wndclass.cbClsExtra = 0;
	wndclass.cbWndExtra = 0;
	wndclass.hInstance = hInstance;
	wndclass.hbrBackground = CreateSolidBrush(RGB(0, 0, 0));
	wndclass.hIcon = LoadIcon(NULL, IDI_APPLICATION);
	wndclass.hCursor = LoadCursor(NULL, IDC_ARROW);
	wndclass.hIconSm = LoadIcon(NULL, IDI_APPLICATION);
	wndclass.lpfnWndProc = WndProc;
	wndclass.lpszClassName = szClassName;
	wndclass.lpszMenuName = NULL;

	RegisterClassEx(&wndclass);

	int x = (GetSystemMetrics(SM_CXSCREEN)) / 2;
	int y = (GetSystemMetrics(SM_CYSCREEN)) / 2;

	hwnd = CreateWindowEx(WS_EX_APPWINDOW,
		szClassName,
		TEXT("Programmable Pipeline Per Fragment Lighting"),
		WS_OVERLAPPEDWINDOW | WS_CLIPCHILDREN | WS_CLIPSIBLINGS | WS_VISIBLE,
		x - WIN_WIDTH / 2,
		y - WIN_HEIGHT / 2,
		WIN_WIDTH,
		WIN_HEIGHT,
		NULL,
		NULL,
		hInstance,
		NULL);

	ghwnd = hwnd;

	initialize();

	ShowWindow(hwnd, SW_SHOW);
	SetForegroundWindow(hwnd);
	SetFocus(hwnd);

	while (bDone == false)
	{
		if (PeekMessage(&msg, NULL, 0, 0, PM_REMOVE))
		{
			if (msg.message == WM_QUIT)
				bDone = true;
			else
			{
				TranslateMessage(&msg);
				DispatchMessage(&msg);
			}
		}
		else
		{
			if (gbActiveWindow == true)
			{
				if (gbEscapeKeyIsPressed == true)
					bDone = true;

				display();
			}
		}
	}

	uninitialize();
	return((int)msg.wParam);

}

LRESULT CALLBACK WndProc(HWND hwnd, UINT iMsg, WPARAM wParam, LPARAM lParam)
{
	void resize(int, int);
	void ToggleFullscreen(void);
	void uninitialize(void);

	static bool bIsAKeyPressed = false;
	static bool bIsLKeyPressed = false;

	switch (iMsg)
	{
	case WM_ACTIVATE:
		if (HIWORD(wParam) == 0)
			gbActiveWindow = true;
		else
			gbActiveWindow = false;
		break;

	case WM_ERASEBKGND:
		return(0);

	case WM_SIZE:
		resize(LOWORD(lParam), HIWORD(lParam));
		break;

	case WM_KEYDOWN:
		switch (wParam)
		{
		case VK_ESCAPE:
			gbEscapeKeyIsPressed = true;
			break;

		case 0x4C:	//	for L key
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

		case 0x46:	//	for F key
			if (gbFullscreen == false)
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
			//MessageBox(hwnd, TEXT("Wrong Key Pressed"), TEXT("Message Box"), MB_OK);
			break;
		}
		break;

	case WM_LBUTTONDOWN:
		break;

	case WM_DESTROY:
		PostQuitMessage(0);
		break;

	default:
		break;
	}

	return(DefWindowProc(hwnd, iMsg, wParam, lParam));
}

void ToggleFullscreen(void)
{
	MONITORINFO mi;

	if (gbFullscreen == false)
	{
		dwStyle = GetWindowLong(ghwnd, GWL_STYLE);
		if (dwStyle & WS_OVERLAPPEDWINDOW)
		{
			mi = { sizeof(MONITORINFO) };
			if (GetWindowPlacement(ghwnd, &wpPrev) && GetMonitorInfo(MonitorFromWindow(ghwnd, MONITORINFOF_PRIMARY), &mi))
			{
				SetWindowLong(ghwnd, GWL_STYLE, dwStyle & ~WS_OVERLAPPEDWINDOW);
				SetWindowPos(ghwnd, HWND_TOP, mi.rcMonitor.left, mi.rcMonitor.top, mi.rcMonitor.right - mi.rcMonitor.left, mi.rcMonitor.bottom - mi.rcMonitor.top, SWP_NOZORDER | SWP_FRAMECHANGED);
			}
		}

		ShowCursor(FALSE);
	}

	else
	{
		SetWindowLong(ghwnd, GWL_STYLE, dwStyle | WS_OVERLAPPEDWINDOW);
		SetWindowPlacement(ghwnd, &wpPrev);
		SetWindowPos(ghwnd, HWND_TOP, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOOWNERZORDER | SWP_NOZORDER | SWP_FRAMECHANGED);
		ShowCursor(TRUE);
	}
}

void initialize(void)
{
	void resize(int, int);
	void uninitialize(void);

	PIXELFORMATDESCRIPTOR pfd;
	int iPixelFormatIndex;

	ZeroMemory(&pfd, sizeof(PIXELFORMATDESCRIPTOR));

	pfd.nSize = sizeof(PIXELFORMATDESCRIPTOR);
	pfd.nVersion = 1;
	pfd.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;
	pfd.iPixelType = PFD_TYPE_RGBA;
	pfd.cColorBits = 32;
	pfd.cRedBits = 8;
	pfd.cGreenBits = 8;
	pfd.cBlueBits = 8;
	pfd.cAlphaBits = 8;
	pfd.cDepthBits = 32;

	ghdc = GetDC(ghwnd);

	iPixelFormatIndex = ChoosePixelFormat(ghdc, &pfd);
	if (iPixelFormatIndex == 0)
	{
		ReleaseDC(ghwnd, ghdc);
		ghdc = NULL;
	}

	if (SetPixelFormat(ghdc, iPixelFormatIndex, &pfd) == FALSE)
	{
		ReleaseDC(ghwnd, ghdc);
		ghdc = NULL;
	}

	ghrc = wglCreateContext(ghdc);
	if (ghrc == NULL)
	{
		ReleaseDC(ghwnd, ghdc);
		ghdc = NULL;
	}

	if (wglMakeCurrent(ghdc, ghrc) == FALSE)
	{
		wglDeleteContext(ghrc);
		ghrc = NULL;
		ReleaseDC(ghwnd, ghdc);
		ghdc = NULL;
	}

	//glewInit() - touching programmable pipeline
	GLenum glew_error = glewInit();
	if (glew_error != GLEW_OK)
	{
		wglDeleteContext(ghrc);
		ghrc = NULL;
		ReleaseDC(ghwnd, ghdc);
		ghdc = NULL;
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
		"uniform vec4 u_light_position_red;"	\
		"uniform vec4 u_light_position_blue;"	\
		"uniform vec4 u_light_position_green;"	\
		"uniform int u_lighting_enabled;"	\
		"out vec3 transformed_normals;"	\
		"out vec3 light_direction_red;"	\
		"out vec3 light_direction_blue;"	\
		"out vec3 light_direction_green;"	\
		"out vec3 viewer_vector;"	\
		"void main(void)"	\
		"{"	\
		"if(u_lighting_enabled == 1)"	\
		"{"	\
		"vec4 eye_coordinates = u_view_matrix * u_model_matrix * vPosition;"	\
		"transformed_normals = mat3(u_view_matrix * u_model_matrix) * vNormal;"	\
		"light_direction_red = vec3(u_light_position_red) - eye_coordinates.xyz;"	\
		"light_direction_blue = vec3(u_light_position_blue) - eye_coordinates.xyz;"	\
		"light_direction_green = vec3(u_light_position_green) - eye_coordinates.xyz;"	\
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
		"in vec3 light_direction_red;"	\
		"in vec3 light_direction_blue;"	\
		"in vec3 light_direction_green;"	\
		"in vec3 viewer_vector;"	\
		"out vec4 FragColor;"	\
		"uniform vec3 u_La_Red;"	\
		"uniform vec3 u_Ld_Red;"	\
		"uniform vec3 u_Ls_Red;"	\
		"uniform vec3 u_La_Blue;"	\
		"uniform vec3 u_Ld_Blue;"	\
		"uniform vec3 u_Ls_Blue;"	\
		"uniform vec3 u_La_Green;"	\
		"uniform vec3 u_Ld_Green;"	\
		"uniform vec3 u_Ls_Green;"	\
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
		"vec3 normalized_light_direction_red = normalize(light_direction_red);"	\
		"vec3 normalized_light_direction_blue = normalize(light_direction_blue);"	\
		"vec3 normalized_light_direction_green = normalize(light_direction_green);"	\
		"vec3 normalized_viewer_vector = normalize(viewer_vector);"	\
		"vec3 ambient_red = u_La_Red * u_Ka;"	\
		"vec3 ambient_blue = u_La_Blue * u_Ka;"	\
		"vec3 ambient_green = u_La_Green * u_Ka;"	\
		"float tn_dot_ld_red = max(dot(normalized_transformed_normals, normalized_light_direction_red), 0.0);"	\
		"float tn_dot_ld_blue = max(dot(normalized_transformed_normals, normalized_light_direction_blue), 0.0);"	\
		"float tn_dot_ld_green = max(dot(normalized_transformed_normals, normalized_light_direction_green), 0.0);"	\
		"vec3 diffuse_red = u_Ld_Red * u_Kd * tn_dot_ld_red;"	\
		"vec3 diffuse_blue = u_Ld_Blue * u_Kd * tn_dot_ld_blue;"	\
		"vec3 diffuse_green = u_Ld_Green * u_Kd * tn_dot_ld_green;"	\
		"vec3 reflection_vector_red = reflect(-normalized_light_direction_red, normalized_transformed_normals);"	\
		"vec3 reflection_vector_blue = reflect(-normalized_light_direction_blue, normalized_transformed_normals);"	\
		"vec3 reflection_vector_green = reflect(-normalized_light_direction_green, normalized_transformed_normals);"	\
		"vec3 specular_red = u_Ls_Red * u_Ks * pow(max(dot(reflection_vector_red, normalized_viewer_vector), 0.0), u_material_shininess);"	\
		"vec3 specular_blue = u_Ls_Blue * u_Ks * pow(max(dot(reflection_vector_blue, normalized_viewer_vector), 0.0), u_material_shininess);"	\
		"vec3 specular_green = u_Ls_Green * u_Ks * pow(max(dot(reflection_vector_green, normalized_viewer_vector), 0.0), u_material_shininess);"	\
		"vec3 phong_ads_color_red = ambient_red + diffuse_red + specular_red;"	\
		"vec3 phong_ads_color_blue = ambient_blue + diffuse_blue + specular_blue;"	\
		"vec3 phong_ads_color_green = ambient_green + diffuse_green + specular_green;"	\
		"phong_ads_color = phong_ads_color_red + phong_ads_color_blue + phong_ads_color_green;"	\
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
	gLaUniformRed = glGetUniformLocation(gShaderProgramObject, "u_La_Red");
	gLaUniformBlue = glGetUniformLocation(gShaderProgramObject, "u_La_Blue");
	gLaUniformGreen = glGetUniformLocation(gShaderProgramObject, "u_La_Green");
	gKaUniform = glGetUniformLocation(gShaderProgramObject, "u_Ka");
	gLdUniformRed = glGetUniformLocation(gShaderProgramObject, "u_Ld_Red");
	gLdUniformBlue = glGetUniformLocation(gShaderProgramObject, "u_Ld_Blue");
	gLdUniformGreen = glGetUniformLocation(gShaderProgramObject, "u_Ld_Green");
	gKdUniform = glGetUniformLocation(gShaderProgramObject, "u_Kd");
	gLsUniformRed = glGetUniformLocation(gShaderProgramObject, "u_Ls_Red");
	gLsUniformBlue = glGetUniformLocation(gShaderProgramObject, "u_Ls_Blue");
	gLsUniformGreen = glGetUniformLocation(gShaderProgramObject, "u_Ls_Green");
	gKsUniform = glGetUniformLocation(gShaderProgramObject, "u_Ks");
	gLightPositionUniformRed = glGetUniformLocation(gShaderProgramObject, "u_light_position_red");
	gLightPositionUniformBlue = glGetUniformLocation(gShaderProgramObject, "u_light_position_blue");
	gLightPositionUniformGreen = glGetUniformLocation(gShaderProgramObject, "u_light_position_green");
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

void display(void)
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glUseProgram(gShaderProgramObject);

	if (gbLight == true)
	{
		//	set "u_lighting_enabled" uniform as '1'
		glUniform1i(LKeyPressedUniform, 1);

		//	set light1's (red light's) properties
		glUniform3fv(gLaUniformRed, 1, light1Ambient);
		glUniform3fv(gLdUniformRed, 1, light1Diffuse);
		glUniform3fv(gLsUniformRed, 1, light1Specular);
		glUniform4fv(gLightPositionUniformRed, 1, light1Position);

		glUniform3fv(gLaUniformBlue, 1, light2Ambient);
		glUniform3fv(gLdUniformBlue, 1, light2Diffuse);
		glUniform3fv(gLsUniformBlue, 1, light2Specular);
		glUniform4fv(gLightPositionUniformBlue, 1, light2Position);

		glUniform3fv(gLaUniformGreen, 1, light3Ambient);
		glUniform3fv(gLdUniformGreen, 1, light3Diffuse);
		glUniform3fv(gLsUniformGreen, 1, light3Specular);
		glUniform4fv(gLightPositionUniformGreen, 1, light3Position);

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

	angleRed = angleRed + 0.002f;
	{
		light1Position[0] = 0.0f;
		light1Position[2] = 50 * cos(angleRed);
		light1Position[1] = 50 * sin(angleRed);
		if (angleRed >= 2 * PI)
			angleRed = 0.0f;
	}

	angleBlue = angleBlue + 0.002f;
	{
		light2Position[1] = 0.0f;
		light2Position[2] = 50 * cos(angleBlue);
		light2Position[0] = 50 * sin(angleBlue);
		if (angleBlue >= 2 * PI)
			angleBlue = 0.0f;
	}

	angleGreen = angleGreen + 0.002f;
	{
		light3Position[2] = 0.0f;
		light3Position[0] = 50 * cos(angleGreen);
		light3Position[1] = 50 * sin(angleGreen);
		if (angleGreen >= 2 * PI)
			angleGreen = 0.0f;
	}

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

	SwapBuffers(ghdc);
}


void resize(int width, int height)
{
	if (height == 0)
		height = 1;
	glViewport(0, 0, (GLsizei)width, (GLsizei)height);

	//gPerspectiveProjectionMatrix = mat4::identity();
	gPerspectiveProjectionMatrix = perspective(60.0f, (GLfloat)width / (GLfloat)height, 0.1f, 100.0f);

}

void uninitialize(void)
{
	if (gbFullscreen == true)
	{
		dwStyle = GetWindowLong(ghwnd, GWL_STYLE);
		SetWindowLong(ghwnd, GWL_STYLE, dwStyle | WS_OVERLAPPEDWINDOW);
		SetWindowPlacement(ghwnd, &wpPrev);
		SetWindowPos(ghwnd, HWND_TOP, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE | SWP_NOOWNERZORDER | SWP_NOZORDER | SWP_FRAMECHANGED);
		ShowCursor(TRUE);
	}

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

	if (gVboElement)
	{
		glDeleteBuffers(1, &gVboElement);
		gVboElement = 0;
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

	wglMakeCurrent(NULL, NULL);

	wglDeleteContext(ghrc);
	ghrc = NULL;

	ReleaseDC(ghwnd, ghdc);
	ghdc = NULL;

	DestroyWindow(ghwnd);
	ghwnd = NULL;

	if (gpFile)
	{
		fprintf(gpFile, "Log File Is Successfully Closed. \n");
		fclose(gpFile);
		gpFile = NULL;
	}

}