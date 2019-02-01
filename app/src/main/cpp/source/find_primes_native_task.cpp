#include <jni.h>
#include <android/log.h>

#include "find_primes_task.h"
#include "debug_listener.h"

#define TAG "PrimeNumberFinder [Native]"

class DListener : public DebugListener{
    void onTaskStarted(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStarted(%d)", (int) task);
    }

    void onTaskPausing(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPausing(%d)", (int) task);
    }

    void onTaskPaused(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPaused(%d)", (int) task);
    }

    void onTaskResuming(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResuming(%d)", (int) task);
    }

    void onTaskResumed(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResumed(%d)", (int) task);
    }

    void onTaskStopping(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopping(%d)", (int) task);
    }

    void onTaskStopped(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopped(%d)", (int) task);
    }
};


extern "C" JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeInit(JNIEnv *env, jobject /*this*/) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "init()");
    FindPrimesTask* task = new FindPrimesTask(0, 2000000);
    task->addTaskListener(new DListener());
    return (long) task;
}