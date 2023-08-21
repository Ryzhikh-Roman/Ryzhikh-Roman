let cvs = document.getElementById("SnakeCanvas");
let ctx = cvs.getContext("2d");

let count = 0;

let grid = cvs.height/10;
let mid = {
    x: cvs.height/2,
    y: cvs.width/2
}

class SnakeCell{
    direction = {dx: 0, dy: 0};
    x = 0;
    y = 0;

    constructor(x, y, direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    draw(){
        ctx.fillStyle = 'green'; // рисуем змейку зелёным
        ctx.fillRect(this.x + 1, this.y + 1, snake.grid - 2, snake.grid - 2);
    }

    move(){
        this.x += this.direction.dx;
        this.y += this.direction.dy;
        if (this.x >= cvs.width){
            this.x = 0;
        }
        if (this.y >= cvs.height){
            this.y = 0;
        }
        if (this.x < 0){
            this.x = cvs.width - grid;
        }
        if (this.y < 0){
            this.y = cvs.height - grid;
        }
    }
}


class Snake {
    grid = 0;
    score = 0;
    snakeCells = [];

    constructor(x, y, grid) {
        this.grid = grid;
        this.snakeCells.push(new SnakeCell(x, y, {dx: 0, dy: 0}));
        //this.snakeCells.push(new SnakeCell(x - grid, y, {dx: 60, dy: 0}));
    }

    move_snake(cur_direction){

        if(cur_direction.dx === -this.snakeCells[0].direction.dx && cur_direction.dy === -this.snakeCells[0].direction.dy){
            cur_direction.dx = this.snakeCells[0].direction.dx;
            cur_direction.dy = this.snakeCells[0].direction.dy;
        }

        for (let i = this.snakeCells.length - 1; i > 0; i--){
            this.snakeCells[i].direction.dx = this.snakeCells[i-1].direction.dx;
            this.snakeCells[i].direction.dy = this.snakeCells[i-1].direction.dy;
            this.snakeCells[i].move();
        }

        this.snakeCells[0].direction.dx = cur_direction.dx;
        this.snakeCells[0].direction.dy = cur_direction.dy;
        this.snakeCells[0].move();
    }

    draw(){
        this.snakeCells.forEach(function(cell){
            cell.draw();
        });
    }


    eat() {
        const clone = this.snakeCells[this.score];
        console.log(clone);
        this.snakeCells.push(new SnakeCell(clone.x - clone.direction.dx, clone.y - clone.direction.dy, {
            dx: clone.direction.dx,
            dy: clone.direction.dy
        }));
        this.score += 1;
        //let snakeTail = new SnakeCell()
        //this.snakeCells.push(new SnakeCell(x, y, {dx: 60, dy: 0}));
    }
}

class Apple{
    x = 0;
    y = 0;
}

function drawGrid(){
    ctx.strokeStyle = 'white';
    ctx.lineWidth = 1;
    for (let i = grid; i < cvs.width; i+=grid){
        ctx.beginPath();
        ctx.moveTo(i, 0);
        ctx.lineTo(i, cvs.height);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(0, i);
        ctx.lineTo(cvs.width, i);
        ctx.stroke();
    }
}

document.addEventListener('keydown', function (e) {
    // Стрелка влево
    if (e.which === 37) {
        chosen_direction.dx = -grid;
        chosen_direction.dy = 0;
    }
    // Стрелка вверх
    else if (e.which === 38) {
        chosen_direction.dx = 0;
        chosen_direction.dy = -grid;
    }
    // Стрелка вправо
    else if (e.which === 39) {
        chosen_direction.dx = grid;
        chosen_direction.dy = 0;
    }
    // Стрелка вниз
    else if (e.which === 40) {
        chosen_direction.dx = 0;
        chosen_direction.dy = grid;
    }
});



let chosen_direction = {
    dx: 0,
    dy: 0
}

let snake = new Snake(mid.x, mid.y, grid);
let a = 0;

function loop() {
    requestAnimationFrame(loop) // рекурсивный запуск loop
    if (++count < 4) {  // движение раз в 60 тиков
        return;
    }
    a++;
    count = 0; //обнуляем тики

    ctx.clearRect(0, 0, cvs.width, cvs.height); // чистим canvas
    drawGrid(); // ресуем сетку
    //apple.plant();
    snake.move_snake(chosen_direction);
    if (a%10 === 0){
        snake.eat();
    }
    snake.draw();
}


requestAnimationFrame(loop) // запуск loop