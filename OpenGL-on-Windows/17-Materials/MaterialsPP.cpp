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

GLfloat angleLight1 = 0.0f;
GLfloat angleLight2 = 0.0f;
GLfloat angleLight3 = 0.0f;

GLfloat x_rotation = 0.0f;
GLfloat y_rotation = 0.0f;
GLfloat z_rotation = 0.0f;

int index = 0;

GLfloat angleLight;

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

GLfloat lightAmbient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat lightDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat lightSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
GLfloat lightPosition[] = { 100.0f, 100.0f, 100.0f, 1.0f };

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

//material components
//emerald
GLfloat emerald_ambient[] = { 0.0215f, 0.1745f, 0.0215f, 1.0f };
GLfloat emerald_diffuse[] = { 0.07568f, 0.61424f, 0.07568f, 1.0f };
GLfloat emerald_specular[] = { 0.633f, 0.727811f, 0.633f, 1.0f };
GLfloat emeraldShininess = 0.6 * 128.0f;

//jade
GLfloat jade_ambient[] = { 0.135f, 0.2225f, 0.155f, 1.0f };
GLfloat jade_diffuse[] = { 0.54f, 0.89f, 0.63f, 1.0f };
GLfloat jade_specular[] = { 0.316228f, 0.316228f, 0.316228f, 1.0f };
GLfloat jadeShininess = 0.21794872 * 128.0f;

//obsidian
GLfloat obsidian_ambient[] = { 0.05375f, 0.05f, 0.06625f, 1.0f };
GLfloat obsidian_diffuse[] = { 0.18275f, 0.17f, 0.22525f, 1.0f };
GLfloat obsidian_specular[] = { 0.332741f, 0.328634f, 0.346435f, 1.0f };
GLfloat obsidianShininess = 0.3 * 128;

//pearl
GLfloat pearl_ambient[] = { 0.25f, 0.20725f, 0.20725f, 1.0f };
GLfloat pearl_diffuse[] = { 1.0f, 0.829f, 0.829f, 1.0f };
GLfloat pearl_specular[] = { 0.296648f, 0.296648f, 0.296648f, 1.0f };
GLfloat pearlShininess = 0.088 * 128;

//ruby
GLfloat ruby_ambient[] = { 0.1745f, 0.01175f, 0.01175f, 1.0f };
GLfloat ruby_diffuse[] = { 0.61424f, 0.04136f, 0.04136f, 1.0f };
GLfloat ruby_specular[] = { 0.727811f, 0.626959f, 0.626959f, 1.0f };
GLfloat rubyShininess = 0.6 * 128;

//turquoise
GLfloat turquoise_ambient[] = { 0.1f, 0.18725f, 0.1745f, 1.0f };
GLfloat turquoise_diffuse[] = { 0.396f, 0.74151f, 0.69102f, 1.0f };
GLfloat turquoise_specular[] = { 0.297254f, 0.30829f, 0.306678f, 1.0f };
GLfloat turquoiseShininess = 0.1 * 128;

//brass
GLfloat brass_ambient[] = { 0.329412f, 0.223529f, 0.027451f, 1.0f };
GLfloat brass_diffuse[] = { 0.780392f, 0.568627f, 0.113725f, 1.0f };
GLfloat brass_specular[] = { 0.992157f, 0.941176f, 0.807843f, 1.0f };
GLfloat brassShininess = 0.21794872 * 128;

//bronze
GLfloat bronze_ambient[] = { 0.2125f, 0.1275f, 0.054f, 1.0f };
GLfloat bronze_diffuse[] = { 0.714f, 0.4284f, 0.18144f, 1.0f };
GLfloat bronze_specular[] = { 0.393548f, 0.271906f, 0.166721f, 1.0f };
GLfloat bronzeShininess = 0.2 * 128;

//chrome
GLfloat chrome_ambient[] = { 0.25f, 0.25f, 0.25f, 1.0f };
GLfloat chrome_diffuse[] = { 0.4f, 0.4f, 0.4f, 1.0f };
GLfloat chrome_specular[] = { 0.774597f, 0.774597f, 0.774597f, 1.0f };
GLfloat chromeShininess = 0.6 * 128;

//copper
GLfloat copper_ambient[] = { 0.19125f, 0.0735f, 0.0225f, 1.0f };
GLfloat copper_diffuse[] = { 0.7038f, 0.27048f, 0.0828f, 1.0f };
GLfloat copper_specular[] = { 0.256777f, 0.137622f, 0.086014f, 1.0f };
GLfloat copperShininess = 0.1 * 128;

//gold
GLfloat gold_ambient[] = { 0.24725f, 0.1995f, 0.0745f, 1.0f };
GLfloat gold_diffuse[] = { 0.75164f, 0.60648f, 0.22648f, 1.0f };
GLfloat gold_specular[] = { 0.628281f, 0.555802f, 0.366065f, 1.0f };
GLfloat goldShininess = 0.4 * 128;

//silver
GLfloat silver_ambient[] = { 0.19225f, 0.19225f, 0.19225f, 1.0f };
GLfloat silver_diffuse[] = { 0.50754f, 0.50754f, 0.50754f, 1.0f };
GLfloat silver_specular[] = { 0.508273f, 0.508273f, 0.508273f, 1.0f };
GLfloat silverShininess = 0.4 * 128;

//black
GLfloat black_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat black_diffuse[] = { 0.01f, 0.01f, 0.01f, 1.0f };
GLfloat black_specular[] = { 0.50f, 0.50f, 0.50f, 1.0f };
GLfloat blackShininess = 0.25 * 128;

//cyan
GLfloat cyan_ambient[] = { 0.0f, 0.1f, 0.06f, 1.0f };
GLfloat cyan_diffuse[] = { 0.0f, 0.50980392f, 0.50980392f, 1.0f };
GLfloat cyan_specular[] = { 0.50196078f, 0.50196078f, 0.50196078f, 1.0f };
GLfloat cyanShininess = 0.25 * 128;

//green
GLfloat green_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat green_diffuse[] = { 0.1f, 0.35f, 0.1f, 1.0f };
GLfloat green_specular[] = { 0.45f, 0.55f, 0.45f, 1.0f };
GLfloat greenShininess = 0.25 * 128;

//red
GLfloat red_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat red_diffuse[] = { 0.5f, 0.0f, 0.0f, 1.0f };
GLfloat red_specular[] = { 0.7f, 0.6f, 0.6f, 1.0f };
GLfloat redShininess = 0.25 * 128;

//white
GLfloat white_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat white_diffuse[] = { 0.55f, 0.55f, 0.55f, 1.0f };
GLfloat white_specular[] = { 0.70f, 0.70f, 0.70f, 1.0f };
GLfloat whiteShininess = 0.25 * 128;

//yellow-plastic
GLfloat yellow_ambient[] = { 0.0f, 0.0f, 0.0f, 1.0f };
GLfloat yellow_diffuse[] = { 0.5f, 0.5f, 0.0f, 1.0f };
GLfloat yellow_specular[] = { 0.60f, 0.60f, 0.50f, 1.0f };
GLfloat yellowShininess = 0.25 * 128;

//black
GLfloat black2_ambient[] = { 0.02f, 0.02f, 0.02f, 1.0f };
GLfloat black2_diffuse[] = { 0.01f, 0.01f, 0.01f, 1.0f };
GLfloat black2_specular[] = { 0.4f, 0.4f, 0.4f, 1.0f };
GLfloat black2Shininess = 0.078125 * 128;

//cyan
GLfloat cyan2_ambient[] = { 0.0f, 0.05f, 0.05f, 1.0f };
GLfloat cyan2_diffuse[] = { 0.4f, 0.5f, 0.5f, 1.0f };
GLfloat cyan2_specular[] = { 0.04f, 0.7f, 0.7f, 1.0f };
GLfloat cyan2Shininess = 0.078125 * 128;

//green
GLfloat green2_ambient[] = { 0.0f, 0.05f, 0.0f, 1.0f };
GLfloat green2_diffuse[] = { 0.4f, 0.5f, 0.4f, 1.0f };
GLfloat green2_specular[] = { 0.04f, 0.7f, 0.04f, 1.0f };
GLfloat green2Shininess = 0.078125 * 128;

//red
GLfloat red2_ambient[] = { 0.05f, 0.0f, 0.0f, 1.0f };
GLfloat red2_diffuse[] = { 0.5f, 0.4f, 0.4f, 1.0f };
GLfloat red2_specular[] = { 0.7f, 0.04f, 0.04f, 1.0f };
GLfloat red2Shininess = 0.078125 * 128;

//white
GLfloat white2_ambient[] = { 0.05f, 0.05f, 0.05f, 1.0f };
GLfloat white2_diffuse[] = { 0.5f, 0.5f, 0.5f, 1.0f };
GLfloat white2_specular[] = { 0.7f, 0.7f, 0.7f, 1.0f };
GLfloat white2Shininess = 0.078125 * 128;

//yellow-rubber
GLfloat yellow2_ambient[] = { 0.05f, 0.05f, 0.0f, 1.0f };
GLfloat yellow2_diffuse[] = { 0.5f, 0.5f, 0.4f, 1.0f };
GLfloat yellow2_specular[] = { 0.7f, 0.7f, 0.04f, 1.0f };
GLfloat yellow2Shininess = 0.078125 * 128;

FILE* gpFile = NULL;

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpszCmdLine, int iCmdShow)
{
	void initialize(void);
	void uninitialize(void);
	void display(void);
	void update(void);
	//void resize(int, int);
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
		TEXT("Programmable Pipeline Materials (Twenty Four Spheres)"),
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
				update();
			}
		}
	}

	//resize(WIN_WIDTH, WIN_HEIGHT);

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

			//x-rotation for light
		case 0x58:
			lightPosition[0] = 0;
			lightPosition[1] = 0;
			lightPosition[2] = 0;
			lightPosition[3] = 0;
			x_rotation = 1.0f;
			y_rotation = 0.0f;
			z_rotation = 0.0f;
			index = 1;
			break;

			//y-rotation for light
		case 0x59:
			lightPosition[0] = 0;
			lightPosition[1] = 0;
			lightPosition[2] = 0;
			lightPosition[3] = 0;
			y_rotation = 1.0f;
			x_rotation = 0.0f;
			z_rotation = 0.0f;
			index = 0;
			break;

			//	z-rotation
		case 0x5A:
			lightPosition[0] = 0;
			lightPosition[1] = 0;
			lightPosition[2] = 0;
			lightPosition[3] = 0;
			z_rotation = 1.0f;
			x_rotation = 0.0f;
			y_rotation = 0.0f;
			index = 0;
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

	glClearColor(0.25f, 0.25f, 0.25f, 0.0f);

	gPerspectiveProjectionMatrix = mat4::identity();

	gbLight = false;

	resize(WIN_WIDTH, WIN_HEIGHT);

}

void display(void)
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glUseProgram(gShaderProgramObject);


	//--------------------------------------------- 1st row ------------------------------------------------------------//
	//	matrix stuff
	mat4 modelMatrix = mat4::identity();
	mat4 viewMatrix = mat4::identity();
	mat4 scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);
	
	modelMatrix = translate(-1.8f, 2.0f, -6.0f);

	modelMatrix = modelMatrix * scaleMatrix;

	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, emerald_ambient);
		glUniform3fv(gKdUniform, 1, emerald_diffuse);
		glUniform3fv(gKsUniform, 1, emerald_specular);
		glUniform1f(gMaterialShininessUniform, emeraldShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);
	
	//-----------------------------------------------1st row---------------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-0.6f, 2.0f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, brass_ambient);
		glUniform3fv(gKdUniform, 1, brass_diffuse);
		glUniform3fv(gKsUniform, 1, brass_specular);
		glUniform1f(gMaterialShininessUniform, brassShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//-------------------------------------------------1st row-------------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(0.6f, 2.0f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, black_ambient);
		glUniform3fv(gKdUniform, 1, black_diffuse);
		glUniform3fv(gKsUniform, 1, black_specular);
		glUniform1f(gMaterialShininessUniform, blackShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);


	//-------------------------------------------------1st row-------------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(1.8f, 2.0f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, black2_ambient);
		glUniform3fv(gKdUniform, 1, black2_diffuse);
		glUniform3fv(gKsUniform, 1, black2_specular);
		glUniform1f(gMaterialShininessUniform, black2Shininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);


	//------------------------------------------------------ 2nd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-1.8f, 1.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, jade_ambient);
		glUniform3fv(gKdUniform, 1, jade_diffuse);
		glUniform3fv(gKsUniform, 1, jade_specular);
		glUniform1f(gMaterialShininessUniform, jadeShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 2nd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-0.6f, 1.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, bronze_ambient);
		glUniform3fv(gKdUniform, 1, bronze_diffuse);
		glUniform3fv(gKsUniform, 1, bronze_specular);
		glUniform1f(gMaterialShininessUniform, bronzeShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 2nd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(0.6f, 1.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, cyan_ambient);
		glUniform3fv(gKdUniform, 1, cyan_diffuse);
		glUniform3fv(gKsUniform, 1, cyan_specular);
		glUniform1f(gMaterialShininessUniform, cyanShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 2nd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(1.8f, 1.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, cyan2_ambient);
		glUniform3fv(gKdUniform, 1, cyan2_diffuse);
		glUniform3fv(gKsUniform, 1, cyan2_specular);
		glUniform1f(gMaterialShininessUniform, cyan2Shininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 3rd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-1.8f, 0.5f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, obsidian_ambient);
		glUniform3fv(gKdUniform, 1, obsidian_diffuse);
		glUniform3fv(gKsUniform, 1, obsidian_specular);
		glUniform1f(gMaterialShininessUniform, obsidianShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 3rd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-0.6f, 0.5f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, chrome_ambient);
		glUniform3fv(gKdUniform, 1, chrome_diffuse);
		glUniform3fv(gKsUniform, 1, chrome_specular);
		glUniform1f(gMaterialShininessUniform, chromeShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 3rd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(0.6f, 0.5f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, green_ambient);
		glUniform3fv(gKdUniform, 1, green_diffuse);
		glUniform3fv(gKsUniform, 1, green_specular);
		glUniform1f(gMaterialShininessUniform, greenShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 3rd row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(1.8f, 0.5f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, green2_ambient);
		glUniform3fv(gKdUniform, 1, green2_diffuse);
		glUniform3fv(gKsUniform, 1, green2_specular);
		glUniform1f(gMaterialShininessUniform, green2Shininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);


	//------------------------------------------------------ 4th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-1.8f, -0.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, pearl_ambient);
		glUniform3fv(gKdUniform, 1, pearl_diffuse);
		glUniform3fv(gKsUniform, 1, pearl_specular);
		glUniform1f(gMaterialShininessUniform, pearlShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 4th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-0.6f, -0.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, copper_ambient);
		glUniform3fv(gKdUniform, 1, copper_diffuse);
		glUniform3fv(gKsUniform, 1, copper_specular);
		glUniform1f(gMaterialShininessUniform, copperShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 4th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(0.6f, -0.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, red_ambient);
		glUniform3fv(gKdUniform, 1, red_diffuse);
		glUniform3fv(gKsUniform, 1, red_specular);
		glUniform1f(gMaterialShininessUniform, redShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 4th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(1.8f, -0.25f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, red2_ambient);
		glUniform3fv(gKdUniform, 1, red2_diffuse);
		glUniform3fv(gKsUniform, 1, red2_specular);
		glUniform1f(gMaterialShininessUniform, red2Shininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);


	//------------------------------------------------------ 5th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-1.8f, -1.0f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, ruby_ambient);
		glUniform3fv(gKdUniform, 1, ruby_diffuse);
		glUniform3fv(gKsUniform, 1, ruby_specular);
		glUniform1f(gMaterialShininessUniform, rubyShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 5th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-0.6f, -1.0f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, gold_ambient);
		glUniform3fv(gKdUniform, 1, gold_diffuse);
		glUniform3fv(gKsUniform, 1, gold_specular);
		glUniform1f(gMaterialShininessUniform, goldShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 5th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(0.6f, -1.0f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, white_ambient);
		glUniform3fv(gKdUniform, 1, white_diffuse);
		glUniform3fv(gKsUniform, 1, white_specular);
		glUniform1f(gMaterialShininessUniform, whiteShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 5th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(1.8f, -1.0f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, white2_ambient);
		glUniform3fv(gKdUniform, 1, white2_diffuse);
		glUniform3fv(gKsUniform, 1, white2_specular);
		glUniform1f(gMaterialShininessUniform, white2Shininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 6th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-1.8f, -1.75f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, turquoise_ambient);
		glUniform3fv(gKdUniform, 1, turquoise_diffuse);
		glUniform3fv(gKsUniform, 1, turquoise_specular);
		glUniform1f(gMaterialShininessUniform, turquoiseShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 6th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(-0.6f, -1.75f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, silver_ambient);
		glUniform3fv(gKdUniform, 1, silver_diffuse);
		glUniform3fv(gKsUniform, 1, silver_specular);
		glUniform1f(gMaterialShininessUniform, silverShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 6th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(0.6f, -1.75f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);

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
		glUniform3fv(gKaUniform, 1, yellow_ambient);
		glUniform3fv(gKdUniform, 1, yellow_diffuse);
		glUniform3fv(gKsUniform, 1, yellow_specular);
		glUniform1f(gMaterialShininessUniform, yellowShininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}

	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//------------------------------------------------------ 6th row --------------------------------------------------------------//
	modelMatrix = mat4::identity();
	viewMatrix = mat4::identity();
	scaleMatrix = mat4::identity();

	scaleMatrix = vmath::scale(0.4f, 0.4f, 0.4f);

	modelMatrix = translate(1.8f, -1.75f, -6.0f);
	modelMatrix = modelMatrix * scaleMatrix;

	//	NOTE: Matrix multiplication is being done inside the Vertex Shader itself
	glUniformMatrix4fv(gViewMatrixUniform, 1, GL_FALSE, viewMatrix);
	glUniformMatrix4fv(gModelMatrixUniform, 1, GL_FALSE, modelMatrix);
	glUniformMatrix4fv(gProjectionMatrixUniform, 1, GL_FALSE, gPerspectiveProjectionMatrix);
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
		glUniform3fv(gKaUniform, 1, yellow2_ambient);
		glUniform3fv(gKdUniform, 1, yellow2_diffuse);
		glUniform3fv(gKsUniform, 1, yellow2_specular);
		glUniform1f(gMaterialShininessUniform, yellow2Shininess);
	}
	else
	{
		glUniform1i(LKeyPressedUniform, 0);
	}


	//	drawing sphere
	glBindVertexArray(gVaoSphere);

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, gVboElement);
	glDrawElements(GL_TRIANGLES, gNumElements, GL_UNSIGNED_SHORT, 0);

	glBindVertexArray(0);

	//-----------------------------------------------------------------------------------------------------------------------//
	glUseProgram(0);

	SwapBuffers(ghdc);
}


void update()
{
	if (x_rotation == 1)
	{

		angleLight = angleLight + 0.002f;
		{
			lightPosition[0] = 0.0f;
			lightPosition[2] = 50 * cos(angleLight);
			lightPosition[1] = 50 * sin(angleLight);
			if (angleLight >= 2 * PI)
				angleLight = 0.0f;
		}
	}

	if (y_rotation == 1)
	{

		angleLight = angleLight + 0.002f;
		{
			lightPosition[1] = 0.0f;
			lightPosition[2] = 50 * cos(angleLight);
			lightPosition[0] = 50 * sin(angleLight);
			if (angleLight >= 2 * PI)
				angleLight = 0.0f;
		}
	}

	if (z_rotation == 1)
	{

		angleLight = angleLight + 0.002f;
		{
			lightPosition[2] = 0.0f;
			lightPosition[0] = 50 * cos(angleLight);
			lightPosition[1] = 50 * sin(angleLight);
			if (angleLight >= 2 * PI)
				angleLight = 0.0f;
		}
	}
}

void resize(int width, int height)
{
	if (height == 0)
		height = 1;
	glViewport(0, 0, (GLsizei)width, (GLsizei)height);

	//gPerspectiveProjectionMatrix = mat4::identity();
	gPerspectiveProjectionMatrix = perspective(45.0f, (GLfloat)width / (GLfloat)height, 0.1f, 100.0f);

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