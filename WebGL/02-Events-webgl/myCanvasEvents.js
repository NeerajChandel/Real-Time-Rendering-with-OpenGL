//  global variables
var canvas = null;
var context = null;

//  onload function
function main()
{
    //  get canvas element
    canvas = document.getElementById("NRC");
    //  error checking
    if(!canvas)
        console.log("Could not obtain canvas.\n");
    else
        console.log("Canvas obtained successfully.\n");

    var canvWidth = canvas.width;
    var canvHeight = canvas.height;

    //  print canvas width and height on the console
    console.log("Canvas Width: " + canvWidth + " and Canvas Height: " + canvHeight);
    
    //  now, get 2D context
    context = canvas.getContext("2d");
    //  error checking
    if(!context)
        console.log("Could not obtain 2D context.\n");
    else
        console.log("2D context obtained successfully.\n");

    //  canvas color is black
    context.fillStyle = "#000000";
    context.fillRect(0, 0, canvWidth, canvHeight);

    //  alignment of text - at the center
    context.textAlign = "center";       //  center horizontally
    context.textBaseline = "middle";    //  center vertically

     //  text
     var str = "Hello World!";
     // text font-type
     context.font = "italic 48px arial";
     // text color
     context.fillStyle = "#00FF00";
     // display text in center
     context.fillText(str, (canvWidth / 2), (canvHeight / 2));

    //  register keyboard's keydown event handler
    window.addEventListener("keydown", keyDownEvent, false);
    window.addEventListener("click", mouseDownEvent, false);

}

//  write event functions
function keyDownEvent()
{
    //  some other code
    alert("A key is pressed.");
}

function mouseDownEvent()
{
    //  some other code
    alert("Mouse is clicked.");
}
