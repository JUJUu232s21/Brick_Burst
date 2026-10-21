#include <jni.h>
#include <vector>
#include <android/log.h>
#include "GameEngine.hpp"
#define LOG_TAG "BrickBurstNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
GameEngine* gameInstance = nullptr;
extern "C" JNIEXPORT void JNICALL
Java_semyon_android_1studio_brick_1burst_GameView_initGameNative(JNIEnv * /* env */, jobject /* thiz */, jfloat width, jfloat height, jint damage) {
    if (gameInstance) delete gameInstance;
    gameInstance = new GameEngine(width, height, damage);
}
extern "C" JNIEXPORT void JNICALL
Java_semyon_android_1studio_brick_1burst_GameView_updateGameNative(JNIEnv *env, jobject /* thiz */, jfloat dt, jfloatArray stateArray) {
    if (!gameInstance) return;
    gameInstance->update(dt);
    float temp[500] = {0};
    temp[0] = gameInstance->playerX;
    temp[1] = gameInstance->playerY;
    temp[2] = gameInstance->playerSize;
    temp[3] = (float)gameInstance->score;
    temp[4] = gameInstance->gameOver ? 1.0f : 0.0f;
    temp[5] = (float)gameInstance->waveIndex;
    int obsCount = 0;
    int baseIdx = 7;
    for (const auto& obs : gameInstance->obstacles) {
        if (obs.active && baseIdx + 4 < 500) {
            temp[baseIdx]     = obs.x;
            temp[baseIdx + 1] = obs.y;
            temp[baseIdx + 2] = obs.width;
            temp[baseIdx + 3] = obs.height;
            temp[baseIdx + 4] = (float)obs.durability;
            baseIdx += 5;
            obsCount++;
        }
    }
    temp[6] = (float)obsCount;
    if (baseIdx < 500) {
        int ballCountIdx = baseIdx;
        baseIdx++;
        int ballCount = 0;
        for (const auto& ball : gameInstance->balls) {
            if (ball.active && baseIdx + 2 < 500) {
                temp[baseIdx]     = ball.x;
                temp[baseIdx + 1] = ball.y;
                temp[baseIdx + 2] = ball.radius;
                baseIdx += 3;
                ballCount++;
            }
        }
        temp[ballCountIdx] = (float)ballCount;
    }
    env->SetFloatArrayRegion(stateArray, 0, baseIdx, temp);
}
extern "C" JNIEXPORT void JNICALL
Java_semyon_android_1studio_brick_1burst_GameView_movePlayerNative(JNIEnv * /* env */, jobject /* thiz */, jfloat x, jfloat y) {
    if (gameInstance) gameInstance->movePlayer(x, y);
}
