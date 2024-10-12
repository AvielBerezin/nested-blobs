var width = window.innerWidth;
var height = window.innerHeight;

var stage = new Konva.Stage({
    container: 'container',
    width: width,
    height: height,
});

var socket = new WebSocket("ws://localhost:80")
socket.onmessage = (event) => {
    var blobs = JSON.parse(event.data);
    var layer = new Konva.Layer();
    var size = 0.2 * Math.min(stage.width(), stage.height())
    blobs.forEach(blob => {
        layer.add(new Konva.Circle({
            x: stage.width() / 2 + size * blob.x,
            y: stage.height() / 2 + size * blob.y,
            radius: size * blob.r,
            fill: 'red',
            stroke: 'black',
            strokeWidth: 4,
            opacity: 0.4,
        }));
    })
    // add the layer to the stage
    stage.add(layer);
};

