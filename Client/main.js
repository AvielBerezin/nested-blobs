var width = window.innerWidth;
var height = window.innerHeight;

var stage = new Konva.Stage({
    container: 'container',
    width: width,
    height: height,
});

var socket = new WebSocket("ws://localhost:80")
var layer = new Konva.Layer();
var size = 0.8 * Math.min(stage.width(), stage.height())

function linearMap(x0, x1, x, y0, y1) {
    if (x0 === x1) {
        return y0;
    }
    return y0 + x * (y1 - y0) / (x1 - x0);
}

socket.onmessage = (event) => {
    const view = JSON.parse(event.data);
    const { radius, blobs } = view;
    var first = true;
    // add the layer to the stage
    var newLayer = new Konva.Layer();
    blobs.forEach(blob => {
        newLayer.add(new Konva.Circle({
            x: stage.width() / 2 + blob.x * size / radius / 2,
            y: stage.height() / 2 + blob.y * size / radius / 2,
            radius: blob.r * size / radius / 2,
            fill: 'red',
            stroke: 'black',
            strokeWidth: 4 * Math.min(1, 5 * blob.r),
            opacity: Math.min(1, Math.max(0.05, linearMap(1, radius, blob.r, 0.6, 0.3))),
        }));
    });
    layer.destroy();
    stage.add(newLayer);
    layer = newLayer;
};

var moving = false;

function sendMovement() {
    const pointerPos = stage.getPointerPosition();
    const x = pointerPos.x - stage.width() / 2;
    const y = pointerPos.y - stage.height() / 2;
    const angle = Math.atan(y / x) + +(x < 0) * Math.PI;
    const relDistance = Math.sqrt((x * x + y * y)) / size * 2;
    const strength = (relDistance - 0.1) / (0.5 - 0.1);
    socket.send(JSON.stringify({angle, strength}));
    console.log(strength)
}

stage.on('pointerdown', function () {
    moving = true;
    sendMovement();
});

stage.on('pointermove', function () {
    if (!moving) {
        return;
    }
    sendMovement();
});

stage.on('pointerup', function () {
    moving = false;
    var angle = 0;
    var strength = 0;
    socket.send(JSON.stringify({angle, strength}));
    console.log({angle, strength});
});

