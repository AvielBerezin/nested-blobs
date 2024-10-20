window.onload = () => {
const canvas = document.createElement("canvas");
document.body.appendChild(canvas);
let size = 0;
function adjustSize() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    size = Math.min(canvas.width, canvas.height);
}
window.onresize = adjustSize;
adjustSize();
const context = canvas.getContext("2d");

function drawCircle(x, y, r, color, alpha) {
    context.fillStyle = color;
    context.globalAlpha = alpha;
    context.beginPath();
    context.arc(canvas.width / 2 + x * size / 2,
                canvas.height / 2 + y * size / 2,
                r * size / 2,
                0, Math.PI * 2);
    context.fill();
    context.fillStyle = 'black';
    context.beginPath();
    context.arc(canvas.width / 2 + x * size / 2,
                canvas.height / 2 + y * size / 2,
                r * size / 2,
                0, Math.PI * 2);
    context.stroke();
}

function linearMap(x0, x1, x, y0, y1) {
    if (x0 === x1) {
        return y0;
    }
    return y0 + x * (y1 - y0) / (x1 - x0);
}

const socket = new WebSocket("ws://localhost:81");

socket.onmessage = (event) => {
    const view = JSON.parse(event.data);
    const { radius, player, blobs } = view;
    context.clearRect(0, 0, canvas.width, canvas.height);
    function alpha(blob) {
        return Math.min(1, Math.max(0.05, linearMap(radius / 12, radius, blob.r, 0.6, 0.2)));
    }
    blobs.forEach(blob => {
        drawCircle(blob.x / radius, blob.y / radius, blob.r / radius,
                   blob.human ? 'red' : 'yellow',
                   alpha(blob));
    });
    drawCircle(player.x / radius, player.y / radius, player.r / radius,
               'green',
               alpha(player));
};

let moving = false;

function sendMovement(x, y) {
    const angle = Math.atan(y / x) + +(x < 0) * Math.PI;
    const d = Math.sqrt(x * x + y * y);
    const strength = (d - 0.1) / (0.5 - 0.1);
    socket.send(JSON.stringify({angle, strength}));
}
window.onmousedown = function ({x, y}) {
    moving = true;
    sendMovement((x - canvas.width / 2) * 2 / size,
                 (y - canvas.height / 2) * 2 / size);
};
window.onmousemove = function ({x, y}) {
    if (!moving) {
        return;
    }
    sendMovement((x - canvas.width / 2) * 2 / size,
                 (y - canvas.height / 2) * 2 / size);
};
window.onmouseup = window.onmouseleave = () => {
    moving = false;
    const angle = 0;
    const strength = 0;
    socket.send(JSON.stringify({angle, strength}));
};
window.onmousewheel = ({deltaY}) => {
    socket.send(JSON.stringify({zoom: deltaY > 0}));
};
}