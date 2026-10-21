#include "GameEngine.hpp"
#include <algorithm>
#include <cstdlib>

GameEngine::GameEngine(float width, float height, int damage) {
    screenW = width;
    screenH = height;
    playerSize = 100.0f;
    playerX = screenW / 2.0f;
    playerY = screenH - playerSize - 200.0f;
    waveIndex = 0;
    score = 0;
    ballDamage = damage;
    gameOver = false;
    shootTimer = 0.0f;
    spawnWave();
}

void GameEngine::spawnWave() {
    waveIndex++;
    int cols = 5;
    float obsWidth = screenW / (float)cols;
    float obsHeight = 100.0f;
    obstacles.reserve(obstacles.size() + (size_t)cols);
  
    for (int i = 0; i < cols; i++) {
        Obstacle obs;
        obs.x = (float)i * obsWidth;
        obs.y = -obsHeight;
        obs.width = obsWidth;
        obs.height = obsHeight;
        obs.active = true;
        int maxDurability = waveIndex * 10;
        obs.durability = waveIndex * 5 + (rand() % maxDurability);
        obstacles.push_back(obs);
    }
}

void GameEngine::shoot() {
    Ball b;
    b.x = playerX;
    b.y = playerY - 20.0f;
    b.vy = -1200.0f;
    b.radius = 15.0f;
    b.active = true;
    balls.push_back(b);
}

void GameEngine::movePlayer(float x, float y) {
    playerX = x;
    if (playerX < playerSize / 2) playerX = playerSize / 2;
    if (playerX > screenW - playerSize / 2) playerX = screenW - playerSize / 2;
    playerY = y;
    float minY = screenH * 0.7f;
    if (playerY < minY) playerY = minY;

    if (playerY > screenH - playerSize / 2) playerY = screenH - playerSize / 2;
}

void GameEngine::update(float dt) {
    if (gameOver) return;

    if (dt > 0.05f) dt = 0.05f;

    shootTimer += dt;
    if (shootTimer >= 0.2f) {
        shoot();
        shootTimer = 0.0f;
    }

    for (auto& ball : balls) {
        if (!ball.active) continue;
        ball.y += ball.vy * dt;
        if (ball.y < -50.0f) ball.active = false;
        for (auto& obs : obstacles) {
            if (!obs.active) continue;
            float closestX = std::max(obs.x, std::min(ball.x, obs.x + obs.width));
            float closestY = std::max(obs.y, std::min(ball.y, obs.y + obs.height));
            float distanceX = ball.x - closestX;
            float distanceY = ball.y - closestY;
            if ((distanceX * distanceX + distanceY * distanceY) < (ball.radius * ball.radius)) {
                ball.active = false;

                int actualDamage = std::min(ballDamage, obs.durability);
                obs.durability -= actualDamage;
                score += actualDamage;

                if (obs.durability <= 0) {
                    obs.active = false;
                }
                break;
            }
        }
    }

    balls.erase(std::remove_if(balls.begin(), balls.end(),
        [](const Ball& b) { return !b.active; }), balls.end());

    bool anyBlockAboveTrigger = false;
    float spawnTriggerY = 1200.0f;
    for (auto& obs : obstacles) {
        if (obs.active) {
            obs.y += (250.0f + (float)waveIndex * 15.0f) * dt;
            if (obs.y < spawnTriggerY) {
                anyBlockAboveTrigger = true;
            }
            float halfP = playerSize / 2.0f;
            if (obs.y + obs.height >= playerY - halfP && obs.y <= playerY + halfP) {
                 if (playerX + halfP > obs.x && playerX - halfP < obs.x + obs.width) {
                     gameOver = true;
                 }
            }
            if (obs.y > screenH) {
                obs.active = false;
            }
        }
    }
    if (!anyBlockAboveTrigger) {
        spawnWave();
    }
    obstacles.erase(std::remove_if(obstacles.begin(), obstacles.end(),
        [](const Obstacle& o) { return !o.active; }), obstacles.end());
}
