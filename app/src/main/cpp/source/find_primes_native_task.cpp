#include <jni.h>
#include <android/log.h>

#include "find_primes_task.h"
#include "debug_listener.h"

#define TAG "PrimeNumberFinder [Native]"

class DListener : public DebugListener{
    void onTaskStarted(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStarted(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskPausing(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPausing(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskPaused(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPaused(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskResuming(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResuming(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskResumed(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResumed(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskStopping(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopping(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskStopped(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopped(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }
};

extern "C" JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeInit(JNIEnv *env, jobject self, jlong start_value, jlong end_value, jobject search_method, jint thread_count, jstring cache_directory) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "init()");

    jmethodID id = env->GetMethodID(env->GetObjectClass(search_method), "ordinal", "()I");
    int value = (int) env->CallIntMethod(search_method, id);
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Enum value: %i", value);

    FindPrimesTask* task = new FindPrimesTask((unsigned long) start_value, (unsigned long) end_value,
                                              static_cast<FindPrimesTask::SearchMethod>(value), (unsigned int) thread_count);
    const char* string = env->GetStringUTFChars(cache_directory, 0);
    task->setCacheDirectory(std::string(string));
    env->ReleaseStringUTFChars(cache_directory, string);
    task->addTaskListener(new DListener());
    return (long) task;
}

extern "C" JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetStartValue(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask*) task_ptr)->getStartValue();
}

extern "C" JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetEndValue(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask*) task_ptr)->getEndValue();
}

extern "C" JNIEXPORT jint JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetPrimeCount(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask*) task_ptr)->getPrimeCount();
}

extern "C" JNIEXPORT jstring JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetStatus(JNIEnv *env, jobject self, jlong task_ptr) {
    return env->NewStringUTF("Native Status String");
}