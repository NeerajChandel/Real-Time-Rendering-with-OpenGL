//  onload function - main()
function main()
{
    //  get <canvas> element
    var canvas = document.getElementById("NRC");    

    //  error checking
    if(!canvas)
        console.log("Obtaining Canvas Failed.\n");
    else
        console.log("Obtaining Canvas Succeeded.\n");
    
    //  print width and height of canvas on the console
    console.log("Canvas width: " + canvas.width + "and Canvas height: " + canvas.height);

    //get 2D context
    var context = canvas.getContext("2d");

    if(!context)
        console.log("Obtaining 2D context failed.\n");
    else
        console.log("Obtaining 2D context succeded.\n");
    
    //  fill canvas with black color
    context.fillStyle = "black";    //  #000000
    context.fillRect(0, 0, canvas.width, canvas.height);

    //  align the text to the center
    context.textAlign = "center";   //  center horizontally
    context.textBaseline = "middle";    //  center vertically

    //  write the text
    var str = "Hello Chrome!";
    //  text font
    context.font = "48px sans-serif";
    //  text color
    context.fillStyle = "#00FF00";
    //  display the text
    context.fillText(str, (canvas.width / 2), (canvas.height / 2));

}