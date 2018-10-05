//  global variables
var canvas = null;
var context = null;
var canvWidth;
var canvHeight;

//  the onload function
function main()
{
    //  get canvas element
    canvas = document.getElementById("NRC");
    //  check for errors
    if(!canvas)
        console.log("Could not obtain canvas.\n");
    else
        console.log("Canvas obtained successfully.\n");

    canvWidth = canvas.width;
    canvHeight = canvas.height;

    //  print canvas width n height on the console
    console.log("Canvas Width: " + canvWidth + " and Canvas Height: " + canvHeight);

    //  get 2D context
    context = canvas.getContext("2d");
    //  check for errors
    if(!context)
        console.log("Could not obtain 2D context.\n");
    else
        console.log("2D context obtained successfully.\n");

    //  fill canvas with black color
    context.fillStyle = "#000000";
    context.fillRect(0, 0, canvWidth, canvHeight);
    
    //  draw the text
    drawText("Hello World!!");

    //  register event handlers
    window.addEventListener("keydown", keyDownEvent, false);
    window.addEventListener("click", mouseDownEvent, false);

}

function drawText(text)
{
    //  align the text to the center
    context.textAlign = "center";       //  horizontally middle
    context.textBaseline = "middle";    //  vertically middle

    //  text font
    context.font = "italic 48px arial";
    //  text color
    context.fillStyle = "#00FF00";
    //  display the text in the center
    context.fillText(text, (canvWidth / 2), (canvHeight / 2));
}

function toggleFullscreen()
{
    var fullscreen_element = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement || null;

    //  if not fullscreen
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
    }
    //  if already fullscreen
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
    }
}

function keyDownEvent()
{
    switch(event.keyCode)
    {
        case 70:    //  for 'f' or 'F'
            toggleFullscreen();
            drawText("Hello World!!");  //  repaint()
            break;
    }
}

function mouseDownEvent()
{
    //  code
    alert("Mouse is clicked.");
}
