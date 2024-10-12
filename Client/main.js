var width = window.innerWidth;
var height = window.innerHeight;

var stage = new Konva.Stage({
    container: 'container',
    width: width,
    height: height,
});

var socket = new WebSocket("ws://localhost:80")
var layer = new Konva.Layer();
var size = 0.2 * Math.min(stage.width(), stage.height())
socket.onmessage = (event) => {
    var blobs = JSON.parse(event.data);
    var first = true;
    // add the layer to the stage
    var newLayer = new Konva.Layer();
    blobs.forEach(blob => {
        newLayer.add(new Konva.Circle({
            x: stage.width() / 2 + size * blob.x,
            y: stage.height() / 2 + size * blob.y,
            radius: size * blob.r,
            fill: 'red',
            stroke: 'black',
            strokeWidth: 4,
            opacity: 0.4,
        }));
    });
    layer.destroy();
    stage.add(newLayer);
    layer = newLayer;
};

var moving = false;

stage.on('pointerdown', function () {
    moving = true;
    var pointerPos = stage.getPointerPosition();
    var x = pointerPos.x - stage.width() / 2;
    var y = pointerPos.y - stage.height() / 2;
    var angle = Math.atan(y / x);
    if (x < 0) {
        angle = angle + Math.PI;
    }
    var strength = Math.sqrt((x * x + y * y)) / size / 2;
    socket.send(JSON.stringify({angle, strength}));
    console.log({x,y,angle, strength});
});

stage.on('pointermove', function () {
    if (!moving) {
        return;
    }
    var pointerPos = stage.getPointerPosition();
    var x = pointerPos.x - stage.width() / 2;
    var y = pointerPos.y - stage.height() / 2;
    var angle = Math.atan(y / x);
    if (x < 0) {
        angle = angle + Math.PI;
    }
    var strength = Math.sqrt((x * x + y * y)) / size / 2;
    socket.send(JSON.stringify({angle, strength}));
});

stage.on('pointerup', function () {
    moving = false;
    var angle = 0;
    var strength = 0;
    socket.send(JSON.stringify({angle, strength}));
    console.log({angle, strength});
});

