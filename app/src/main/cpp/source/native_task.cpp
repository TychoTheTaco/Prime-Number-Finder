#include <jni.h>
#include <android/log.h>

#include "find_primes_task.h"

#define TAG "PrimeNumberFinder [Native]"

extern "C" JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeInit(JNIEnv *env, jobject /*this*/) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "init()");
    return (long) new FindPrimesTask(0, 1000000);
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeRun(JNIEnv *env, jobject *self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "run(%d)", task_ptr);
    Task* task = (Task*) task_ptr;
    task->start();
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Finished in %d ms.", task->getElapsedTime());
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativePause(JNIEnv *env, jobject *self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "pauses(%d)", task_ptr);
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeResume(JNIEnv *env, jobject *self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "resume(%d)", task_ptr);
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStop(JNIEnv *env, jobject *self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "stop(%d)", task_ptr);
}