#include <jni.h>
#include <android/log.h>

#include "find_primes_task.h"
#include "debug_listener.h"
#include "task.h"

#define TAG "PrimeNumberFinder [Native]"

class DListener : public DebugListener {
    void onTaskStarted(Task *task) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStarted(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskPausing(Task *task) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPausing(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskPaused(Task *task) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPaused(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskResuming(Task *task) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResuming(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskResumed(Task *task) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResumed(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskStopping(Task *task) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopping(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }

    void onTaskStopped(Task *task) {
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopped(%d)", (int) reinterpret_cast<std::uintptr_t>(task));
    }
};

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeInit(JNIEnv *env, jclass self, jlong start_value, jlong end_value,
                                                                                        jobject search_method,
                                                                                        jint thread_count, jstring cache_directory) {
    //Get enum value
    jmethodID id = env->GetMethodID(env->GetObjectClass(search_method), "ordinal", "()I");
    int value = (int) env->CallIntMethod(search_method, id);

    FindPrimesTask *task = new FindPrimesTask(start_value, end_value, static_cast<FindPrimesTask::SearchMethod>(value), thread_count);

    //Set cache directory
    const char *string = env->GetStringUTFChars(cache_directory, nullptr);
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Cache dir: %s", string);
    task->setCacheDirectory(std::string(string));
    env->ReleaseStringUTFChars(cache_directory, string);

    return (long) task;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Getters
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jlong JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetStartValue(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask *) task_ptr)->getStartValue();
}

JNIEXPORT jlong JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetEndValue(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask *) task_ptr)->getEndValue();
}

JNIEXPORT jint JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetThreadCount(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask *) task_ptr)->getThreadCount();
}

JNIEXPORT jint JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetPrimeCount(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask *) task_ptr)->getPrimeCount();
}

JNIEXPORT jstring JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeGetCacheDirectory(JNIEnv *env, jobject self, jlong task_ptr) {
    return env->NewStringUTF(((FindPrimesTask *) task_ptr)->getCacheDirectory().c_str());
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Misc
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jboolean JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeIsEndless(JNIEnv *env, jobject self, jlong task_ptr) {
    return ((FindPrimesTask *) task_ptr)->isEndless();
}

JNIEXPORT void JNICALL
Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesNativeTask_nativeSaveToFile(JNIEnv *env, jobject self, jlong task_ptr, jstring file_path) {
    //Get file path
    const char *path = env->GetStringUTFChars(file_path, nullptr);
    ((FindPrimesTask *) task_ptr)->saveToFile(std::string(path));
    env->ReleaseStringUTFChars(file_path, path);
}

}
