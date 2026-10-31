package com.example.data.templates

import com.example.data.local.FileEntity
import com.example.data.local.ProjectEntity
import java.util.UUID

object ProjectTemplates {

    fun createSpaceShooterProject(): Pair<ProjectEntity, List<FileEntity>> {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = "Space Shooter 2D",
            description = "Arcade Space Shooter game with HTML5 Canvas, laser shooting, enemy waves & touch controls",
            templateType = "space_game"
        )

        val indexHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>Space Shooter 2D</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div id="game-container">
    <canvas id="gameCanvas"></canvas>
    <div id="ui-overlay">
      <div id="score-bar">
        <span>SCORE: <b id="scoreVal">0</b></span>
        <span>HEALTH: <b id="healthVal">100%</b></span>
        <span>LEVEL: <b id="levelVal">1</b></span>
      </div>
      <div id="game-over-screen" class="hidden">
        <h2>MISSION FAILED</h2>
        <p>Final Score: <span id="finalScore">0</span></p>
        <button id="restartBtn" onclick="restartGame()">PLAY AGAIN</button>
      </div>
    </div>
    
    <!-- Virtual Controls for Mobile -->
    <div id="virtual-controls">
      <div class="dpad">
        <button class="vbtn" id="btnLeft" onpointerdown="input.left=true" onpointerup="input.left=false">◀</button>
        <button class="vbtn" id="btnRight" onpointerdown="input.right=true" onpointerup="input.right=false">▶</button>
      </div>
      <div class="actions">
        <button class="vbtn fire-btn" id="btnFire" onpointerdown="input.fire=true" onpointerup="input.fire=false">FIRE ⚡</button>
      </div>
    </div>
  </div>
  <script src="game.js"></script>
</body>
</html>
""".trimIndent()

        val styleCss = """
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  touch-action: none;
  user-select: none;
}
body, html {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #080811;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #fff;
}
#game-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
#gameCanvas {
  background: radial-gradient(circle at center, #11142e 0%, #05060f 100%);
  display: block;
  max-width: 100%;
  max-height: 100%;
  box-shadow: 0 0 20px rgba(0, 122, 204, 0.5);
}
#ui-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
#score-bar {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
}
#score-bar b {
  color: #00d2ff;
}
#game-over-screen {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(20, 10, 30, 0.95);
  border: 2px solid #ff416c;
  padding: 24px 32px;
  border-radius: 12px;
  text-align: center;
  pointer-events: auto;
}
#game-over-screen h2 {
  color: #ff4b2b;
  margin-bottom: 12px;
  letter-spacing: 2px;
}
#game-over-screen button {
  margin-top: 16px;
  padding: 10px 24px;
  background: linear-gradient(135deg, #007acc, #00d2ff);
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  letter-spacing: 1px;
}
#virtual-controls {
  position: absolute;
  bottom: 12px;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
  pointer-events: none;
}
.dpad, .actions {
  display: flex;
  gap: 12px;
  pointer-events: auto;
}
.vbtn {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
}
.vbtn:active {
  background: rgba(0, 122, 204, 0.6);
  border-color: #00d2ff;
  transform: scale(0.95);
}
.fire-btn {
  width: 80px;
  border-radius: 29px;
  background: rgba(255, 65, 108, 0.35);
  border-color: #ff4b2b;
  font-size: 13px;
  font-weight: bold;
}
.fire-btn:active {
  background: #ff4b2b;
}
.hidden { display: none !important; }
""".trimIndent()

        val gameJs = """
// Space Shooter 2D Engine
console.log("Initializing Space Shooter 2D Engine...");

const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

function resize() {
  canvas.width = Math.min(window.innerWidth, 480);
  canvas.height = window.innerHeight;
}
window.addEventListener('resize', resize);
resize();

// Input state
const input = { left: false, right: false, fire: false };

window.addEventListener('keydown', (e) => {
  if (e.key === 'ArrowLeft' || e.key === 'a') input.left = true;
  if (e.key === 'ArrowRight' || e.key === 'd') input.right = true;
  if (e.key === ' ' || e.key === 'Enter') input.fire = true;
});

window.addEventListener('keyup', (e) => {
  if (e.key === 'ArrowLeft' || e.key === 'a') input.left = false;
  if (e.key === 'ArrowRight' || e.key === 'd') input.right = false;
  if (e.key === ' ' || e.key === 'Enter') input.fire = false;
});

// Touch drag support on canvas
let touchStartX = 0;
canvas.addEventListener('touchstart', (e) => {
  touchStartX = e.touches[0].clientX;
  input.fire = true;
});
canvas.addEventListener('touchmove', (e) => {
  const diff = e.touches[0].clientX - touchStartX;
  player.x += diff * 0.4;
  touchStartX = e.touches[0].clientX;
});
canvas.addEventListener('touchend', () => {
  input.fire = false;
});

// Game state
let score = 0;
let health = 100;
let level = 1;
let isGameOver = false;

// Stars background
const stars = [];
for (let i = 0; i < 60; i++) {
  stars.push({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    speed: Math.random() * 2 + 0.5,
    size: Math.random() * 2 + 1
  });
}

// Player
const player = {
  x: canvas.width / 2,
  y: canvas.height - 90,
  width: 36,
  height: 40,
  speed: 5.5,
  lastShot: 0
};

// Lasers & Enemies
let lasers = [];
let enemies = [];
let particles = [];

function spawnEnemy() {
  if (isGameOver) return;
  const size = Math.random() * 15 + 25;
  enemies.push({
    x: Math.random() * (canvas.width - size),
    y: -size,
    size: size,
    speed: Math.random() * 2 + 1.5 + (level * 0.3),
    color: ['#ff416c', '#ff4b2b', '#f7b733', '#9b51e0'][Math.floor(Math.random() * 4)]
  });
}
setInterval(spawnEnemy, 1200);

function createExplosion(x, y, color) {
  for (let i = 0; i < 16; i++) {
    particles.push({
      x, y,
      vx: (Math.random() - 0.5) * 6,
      vy: (Math.random() - 0.5) * 6,
      alpha: 1,
      color: color || '#ff9900'
    });
  }
}

function update() {
  if (isGameOver) return;

  // Move stars
  stars.forEach(s => {
    s.y += s.speed;
    if (s.y > canvas.height) {
      s.y = 0;
      s.x = Math.random() * canvas.width;
    }
  });

  // Move player
  if (input.left) player.x -= player.speed;
  if (input.right) player.x += player.speed;
  player.x = Math.max(player.width / 2, Math.min(canvas.width - player.width / 2, player.x));

  // Shooting
  const now = Date.now();
  if (input.fire && now - player.lastShot > 180) {
    lasers.push({ x: player.x - 8, y: player.y - 10, speed: 9 });
    lasers.push({ x: player.x + 8, y: player.y - 10, speed: 9 });
    player.lastShot = now;
  }

  // Update lasers
  lasers.forEach(l => l.y -= l.speed);
  lasers = lasers.filter(l => l.y > -20);

  // Update enemies
  enemies.forEach(e => {
    e.y += e.speed;

    // Check hit player
    const distToPlayer = Math.hypot(e.x + e.size / 2 - player.x, e.y + e.size / 2 - player.y);
    if (distToPlayer < e.size / 2 + 16) {
      health -= 20;
      createExplosion(player.x, player.y, '#ff416c');
      e.y = canvas.height + 100;
      updateUI();
      if (health <= 0) gameOver();
    }
  });

  // Laser hit enemy collisions
  lasers.forEach((l, lIdx) => {
    enemies.forEach((e, eIdx) => {
      if (l.x > e.x && l.x < e.x + e.size && l.y > e.y && l.y < e.y + e.size) {
        createExplosion(e.x + e.size / 2, e.y + e.size / 2, e.color);
        enemies.splice(eIdx, 1);
        lasers.splice(lIdx, 1);
        score += 100;
        if (score % 800 === 0) level++;
        updateUI();
      }
    });
  });

  enemies = enemies.filter(e => e.y < canvas.height + 50);

  // Particles
  particles.forEach(p => {
    p.x += p.vx;
    p.y += p.vy;
    p.alpha -= 0.03;
  });
  particles = particles.filter(p => p.alpha > 0);
}

function draw() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Draw stars
  ctx.fillStyle = '#ffffff';
  stars.forEach(s => {
    ctx.fillRect(s.x, s.y, s.size, s.size);
  });

  // Draw player spaceship
  ctx.save();
  ctx.translate(player.x, player.y);
  
  // Ship body
  ctx.fillStyle = '#00d2ff';
  ctx.beginPath();
  ctx.moveTo(0, -player.height / 2);
  ctx.lineTo(player.width / 2, player.height / 2);
  ctx.lineTo(0, player.height / 3);
  ctx.lineTo(-player.width / 2, player.height / 2);
  ctx.closePath();
  ctx.fill();

  // Engine flame
  ctx.fillStyle = Math.random() > 0.5 ? '#ff9900' : '#ff4400';
  ctx.beginPath();
  ctx.moveTo(-6, player.height / 3);
  ctx.lineTo(6, player.height / 3);
  ctx.lineTo(0, player.height / 3 + 14 + Math.random() * 8);
  ctx.closePath();
  ctx.fill();
  ctx.restore();

  // Draw lasers
  ctx.fillStyle = '#00ffff';
  ctx.shadowColor = '#00ffff';
  ctx.shadowBlur = 10;
  lasers.forEach(l => {
    ctx.fillRect(l.x - 2, l.y, 4, 14);
  });
  ctx.shadowBlur = 0;

  // Draw enemies
  enemies.forEach(e => {
    ctx.fillStyle = e.color;
    ctx.beginPath();
    ctx.arc(e.x + e.size / 2, e.y + e.size / 2, e.size / 2, 0, Math.PI * 2);
    ctx.fill();
    // Inner core
    ctx.fillStyle = '#ffffff';
    ctx.beginPath();
    ctx.arc(e.x + e.size / 2, e.y + e.size / 2, e.size / 4, 0, Math.PI * 2);
    ctx.fill();
  });

  // Draw particles
  particles.forEach(p => {
    ctx.save();
    ctx.globalAlpha = p.alpha;
    ctx.fillStyle = p.color;
    ctx.beginPath();
    ctx.arc(p.x, p.y, 3, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  });
}

function loop() {
  update();
  draw();
  requestAnimationFrame(loop);
}
requestAnimationFrame(loop);

function updateUI() {
  document.getElementById('scoreVal').innerText = score;
  document.getElementById('healthVal').innerText = Math.max(0, health) + '%';
  document.getElementById('levelVal').innerText = level;
}

function gameOver() {
  isGameOver = true;
  document.getElementById('finalScore').innerText = score;
  document.getElementById('game-over-screen').classList.remove('hidden');
  console.log("Game Over! Final Score:", score);
}

function restartGame() {
  score = 0;
  health = 100;
  level = 1;
  isGameOver = false;
  enemies = [];
  lasers = [];
  particles = [];
  player.x = canvas.width / 2;
  document.getElementById('game-over-screen').classList.add('hidden');
  updateUI();
  console.log("Game restarted!");
}
""".trimIndent()

        val readmeMd = """
# Space Shooter 2D

A high-performance HTML5 canvas retro shooter designed and optimized for mobile devices and tablets.

## Features
- Dynamic multi-touch / on-screen dpad controls
- Accelerating enemy waves & particle explosion physics
- Level scaling & health monitoring
- Live interactive console logging

## How to Test
1. Tap the **Run** button (or open the **Preview** panel) in VS Code.
2. Use on-screen Left/Right and Fire buttons, or drag your finger on the screen to steer your ship.
""".trimIndent()

        val configJson = """
{
  "name": "Space Shooter 2D",
  "version": "1.0.0",
  "author": "Mobile Developer",
  "maxLevels": 10,
  "difficulty": "medium",
  "audioEnabled": true
}
        """.trimIndent()

        val files = listOf(
            FileEntity(UUID.randomUUID().toString(), projectId, "index.html", "/index.html", indexHtml, isFolder = false, parentPath = "/"),
            FileEntity(UUID.randomUUID().toString(), projectId, "README.md", "/README.md", readmeMd, isFolder = false, parentPath = "/"),
            FileEntity(UUID.randomUUID().toString(), projectId, "src", "/src", "", isFolder = true, parentPath = "/"),
            FileEntity(UUID.randomUUID().toString(), projectId, "game.js", "/src/game.js", gameJs, isFolder = false, parentPath = "/src"),
            FileEntity(UUID.randomUUID().toString(), projectId, "styles", "/styles", "", isFolder = true, parentPath = "/"),
            FileEntity(UUID.randomUUID().toString(), projectId, "style.css", "/styles/style.css", styleCss, isFolder = false, parentPath = "/styles"),
            FileEntity(UUID.randomUUID().toString(), projectId, "assets", "/assets", "", isFolder = true, parentPath = "/"),
            FileEntity(UUID.randomUUID().toString(), projectId, "config.json", "/assets/config.json", configJson, isFolder = false, parentPath = "/assets")
        )

        return Pair(project, files)
    }

    fun createFlappyBirdProject(): Pair<ProjectEntity, List<FileEntity>> {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = "Flappy Pixel Bird",
            description = "Classic Flappy Bird canvas game with gravity, pipe obstacles, and high scores",
            templateType = "flappy_game"
        )

        val indexHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>Flappy Pixel Bird</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; touch-action: none; }
    body { background: #111; display: flex; justify-content: center; align-items: center; height: 100vh; overflow: hidden; font-family: monospace; }
    canvas { background: #70c5ce; border: 2px solid #333; max-width: 100%; height: 100%; }
  </style>
</head>
<body>
  <canvas id="canvas" width="360" height="600"></canvas>
  <script>
    const canvas = document.getElementById('canvas');
    const ctx = canvas.getContext('2d');
    let bird = { x: 50, y: 250, r: 14, vy: 0, gravity: 0.35, jump: -6.5 };
    let pipes = [];
    let score = 0;
    let high = 0;
    let gameOver = false;

    function jump() {
      if (gameOver) {
        pipes = [];
        bird.y = 250;
        bird.vy = 0;
        score = 0;
        gameOver = false;
        return;
      }
      bird.vy = bird.jump;
    }
    window.addEventListener('pointerdown', jump);
    window.addEventListener('keydown', (e) => { if (e.code === 'Space') jump(); });

    let frame = 0;
    function loop() {
      frame++;
      // Physics
      if (!gameOver) {
        bird.vy += bird.gravity;
        bird.y += bird.vy;

        if (frame % 90 === 0) {
          let topH = Math.random() * 200 + 50;
          pipes.push({ x: 360, top: topH, bot: topH + 130, passed: false });
        }

        pipes.forEach(p => {
          p.x -= 2.2;
          if (!p.passed && p.x < bird.x) {
            p.passed = true;
            score++;
            if (score > high) high = score;
          }
          // Collision
          if (bird.x + bird.r > p.x && bird.x - bird.r < p.x + 50) {
            if (bird.y - bird.r < p.top || bird.y + bird.r > p.bot) gameOver = true;
          }
        });
        pipes = pipes.filter(p => p.x > -60);
        if (bird.y > 600 - bird.r || bird.y < bird.r) gameOver = true;
      }

      // Draw
      ctx.fillStyle = '#70c5ce';
      ctx.fillRect(0, 0, 360, 600);

      // Pipes
      ctx.fillStyle = '#73bf2e';
      pipes.forEach(p => {
        ctx.fillRect(p.x, 0, 50, p.top);
        ctx.fillRect(p.x, p.bot, 50, 600 - p.bot);
      });

      // Bird
      ctx.fillStyle = '#f7d02c';
      ctx.beginPath();
      ctx.arc(bird.x, bird.y, bird.r, 0, Math.PI * 2);
      ctx.fill();

      // UI
      ctx.fillStyle = '#ffffff';
      ctx.font = 'bold 24px monospace';
      ctx.fillText('SCORE: ' + score, 16, 40);
      ctx.fillText('BEST: ' + high, 16, 70);

      if (gameOver) {
        ctx.fillStyle = 'rgba(0,0,0,0.6)';
        ctx.fillRect(0, 0, 360, 600);
        ctx.fillStyle = '#ff4b4b';
        ctx.font = 'bold 28px monospace';
        ctx.fillText('GAME OVER', 95, 290);
        ctx.fillStyle = '#fff';
        ctx.font = '16px monospace';
        ctx.fillText('Tap to Restart', 115, 330);
      }
      requestAnimationFrame(loop);
    }
    loop();
  </script>
</body>
</html>
""".trimIndent()

        val files = listOf(
            FileEntity(UUID.randomUUID().toString(), projectId, "index.html", "/index.html", indexHtml)
        )
        return Pair(project, files)
    }

    fun createTodoAppProject(): Pair<ProjectEntity, List<FileEntity>> {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = "Todo & Notes WebApp",
            description = "Responsive tasks and notes manager with filtering and local storage",
            templateType = "todo_app"
        )

        val indexHtml = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Developer Task Tracker</title>
  <style>
    body { background: #1e1e1e; color: #d4d4d4; font-family: -apple-system, sans-serif; padding: 20px; }
    h1 { color: #007acc; font-size: 22px; margin-bottom: 16px; }
    .input-row { display: flex; gap: 8px; margin-bottom: 20px; }
    input { flex: 1; padding: 10px; background: #252526; border: 1px solid #3c3c3c; color: white; border-radius: 4px; }
    button { padding: 10px 16px; background: #007acc; border: none; color: white; font-weight: bold; border-radius: 4px; cursor: pointer; }
    ul { list-style: none; padding: 0; }
    li { background: #2d2d2d; padding: 12px; margin-bottom: 8px; border-radius: 4px; display: flex; justify-content: space-between; align-items: center; }
    li.done { text-decoration: line-through; opacity: 0.6; }
  </style>
</head>
<body>
  <h1>Tasks & Code Milestones</h1>
  <div class="input-row">
    <input id="taskInput" placeholder="Add a new task or feature..." />
    <button onclick="addTask()">Add</button>
  </div>
  <ul id="taskList"></ul>
  <script>
    let tasks = [
      { id: 1, text: "Design VS Code responsive layout", done: true },
      { id: 2, text: "Build HTML5 game preview runner", done: true },
      { id: 3, text: "Enable Git commits and diff tracking", done: false }
    ];
    function render() {
      const list = document.getElementById('taskList');
      list.innerHTML = tasks.map(t => `
        <li class="${'$'}{t.done ? 'done' : ''}" onclick="toggle(${'$'}{t.id})">
          <span>${'$'}{t.text}</span>
          <span>${'$'}{t.done ? '✅' : '⏳'}</span>
        </li>
      `).join('');
    }
    function addTask() {
      const inp = document.getElementById('taskInput');
      if (inp.value.trim()) {
        tasks.push({ id: Date.now(), text: inp.value.trim(), done: false });
        inp.value = '';
        render();
      }
    }
    function toggle(id) {
      tasks = tasks.map(t => t.id === id ? { ...t, done: !t.done } : t);
      render();
    }
    render();
  </script>
</body>
</html>
""".trimIndent()

        val files = listOf(
            FileEntity(UUID.randomUUID().toString(), projectId, "index.html", "/index.html", indexHtml)
        )
        return Pair(project, files)
    }
}
