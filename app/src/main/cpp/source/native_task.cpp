#include <jni.h>
#include <android/log.h>

#include "find_primes_task.h"

#define TAG "PrimeNumberFinder [Native]"

class NativeListener: public TaskListener{
public:
    NativeListener(JNIEnv* env, jobject task_listener): task_listener(env->NewGlobalRef(task_listener)){
        int result = env->GetJavaVM(&jvm);
        assert(result == JNI_OK);
        jclass cls = env->GetObjectClass(task_listener);
        method_ids[0] = env->GetMethodID(cls, "onTaskStarted", "()V");
        method_ids[1] = env->GetMethodID(cls, "onTaskPausing", "()V");
        method_ids[2] = env->GetMethodID(cls, "onTaskPaused", "()V");
        method_ids[3] = env->GetMethodID(cls, "onTaskResuming", "()V");
        method_ids[4] = env->GetMethodID(cls, "onTaskResumed", "()V");
        method_ids[5] = env->GetMethodID(cls, "onTaskStopping", "()V");
        method_ids[6] = env->GetMethodID(cls, "onTaskStopped", "()V");
    }

    void onTaskStarted(Task* task){
        getEnv()->CallVoidMethod(task_listener, method_ids[0]);
    }

    void onTaskPausing(Task* task){
        getEnv()->CallVoidMethod(task_listener, method_ids[1]);
    }

    void onTaskPaused(Task* task){
        getEnv()->CallVoidMethod(task_listener, method_ids[2]);
    }

    void onTaskResuming(Task* task){
        getEnv()->CallVoidMethod(task_listener, method_ids[3]);
    }

    void onTaskResumed(Task* task){
        getEnv()->CallVoidMethod(task_listener, method_ids[4]);
    }

    void onTaskStopping(Task* task){
        getEnv()->CallVoidMethod(task_listener, method_ids[5]);
    }

    void onTaskStopped(Task* task){
        getEnv()->CallVoidMethod(task_listener, method_ids[6]);
    }

private:
    JavaVM* jvm;
    jobject task_listener;
    jmethodID method_ids[7];

    JNIEnv* getEnv(){
        JNIEnv* env;
        int result = jvm->AttachCurrentThread(&env, 0);
        assert(result == JNI_OK);
        return env;
    }
};

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
    //__android_log_print(ANDROID_LOG_VERBOSE, TAG, "Native progress: %f", ((Task*) task_ptr)->getProgress());
    return (jfloat) ((Task*) task_ptr)->getProgress();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeAddTaskListener(JNIEnv *env, jobject self, jlong task_ptr, jobject task_listener) {
    ((Task*) task_ptr)->addTaskListener(new NativeListener(env, task_listener));
}