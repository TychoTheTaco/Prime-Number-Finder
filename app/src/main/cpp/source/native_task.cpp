#include <jni.h>
#include <android/log.h>

#include "find_primes_task.h"

#define TAG "PrimeNumberFinder [Native]"

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStart(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->start();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStartOnNewThread(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->startOnNewThread();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativePause(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->pause();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativePauseAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->pauseAndWait();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeResume(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->resume();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeResumeAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->resumeAndWait();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStop(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->stop();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStopAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->stopAndWait();
}

extern "C" JNIEXPORT jint JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetState(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jint) ((Task*) task_ptr)->getState();
}

extern "C" JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetElapsedTime(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jlong) ((Task*) task_ptr)->getElapsedTime();
}

extern "C" JNIEXPORT jfloat JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetProgress(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jfloat) ((Task*) task_ptr)->getProgress();
}