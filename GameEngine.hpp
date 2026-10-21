#pragma once
#include <vector>
struct Ball {
    float x, y;
    float vy;
    float radius;
    bool active;
};
struct Obstacle {
    float x, y;
    float width, height;
    int durability;
    bool active;
};
class GameEngine {
public:
    float playerX, playerY, playerSize;
    int waveIndex;
    int score;
    int ballDamage;
    bool gameOver;
    float screenW, screenH;
    float shootTimer;
    std::vector<Ball> balls;
    std::vector<Obstacle> obstacles;
    GameEngine(float width, float height, int damage);
    void update(float dt);
    void spawnWave();
    void shoot();
    void movePlayer(float x, float y);
};
