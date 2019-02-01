#include <jni.h>
#include <android/log.h>

#include "find_primes_task.h"

#define TAG "PrimeNumberFinder [Native]"

/*class CallbackListener : public TaskListener{
public:
    CallbackListener(JNIEnv *env, jobject &self){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Constructing: env: %d, self: %d", (int) &*env, (int) &self);
        this->self = env->NewGlobalRef(self);

        int gotVM = env->GetJavaVM(&jvm);
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Got JVM: %s", (gotVM ? "false" : "true"));

        this->method_ids = new jmethodID[7];
        jclass cls = env->FindClass("com/tycho/app/primenumberfinder/NativeTask");
        this->method_ids[0] = env->GetMethodID(cls, "dispatchStarted", "()V");
        this->method_ids[1] = env->GetMethodID(cls, "dispatchPausing", "()V");
        this->method_ids[2] = env->GetMethodID(cls, "dispatchPaused", "()V");
        this->method_ids[3] = env->GetMethodID(cls, "dispatchResuming", "()V");
        this->method_ids[4] = env->GetMethodID(cls, "dispatchResumed", "()V");
        this->method_ids[5] = env->GetMethodID(cls, "dispatchStopping", "()V");
        this->method_ids[6] = env->GetMethodID(cls, "dispatchStopped", "()V");
    }

    void onTaskStarted(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStarted(%d)", (int) task);
        getEnv()->CallVoidMethod(self, this->method_ids[0]);
    }

    void onTaskPausing(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPausing(%d)", (int) task);
        getEnv()->CallVoidMethod(self, this->method_ids[1]);
    }

    void onTaskPaused(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskPaused(%d)", (int) task);
        getEnv()->CallVoidMethod(self, this->method_ids[2]);
    }

    void onTaskResuming(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResuming(%d)", (int) task);
        getEnv()->CallVoidMethod(self, this->method_ids[3]);
    }

    void onTaskResumed(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskResumed(%d)", (int) task);
        getEnv()->CallVoidMethod(self, this->method_ids[4]);
    }

    void onTaskStopping(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopping(%d)", (int) task);
        getEnv()->CallVoidMethod(self, this->method_ids[5]);
    }

    void onTaskStopped(Task* task){
        __android_log_print(ANDROID_LOG_VERBOSE, TAG, "onTaskStopped(%d)", (int) task);
        getEnv()->CallVoidMethod(self, this->method_ids[6]);
    }

private:
    JavaVM *jvm;
    jobject self;

    jmethodID* method_ids;

    JNIEnv* getEnv(){
        JNIEnv *env;
        jint status = this->jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
        if (status == JNI_EDETACHED) {
            status = this->jvm->AttachCurrentThread(&env, 0);
        }
        if (status != JNI_OK) {
            __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Failed to get VM!");
            return nullptr;
        }
        return env;
    }
};

CallbackListener* callbackListener;*/

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStart(JNIEnv *env, jobject self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "start(%d)", task_ptr);
    Task* task = (Task*) task_ptr;
    task->start();
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "Finished in %d ms.", task->getElapsedTime());
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStartOnNewThread(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->startOnNewThread();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativePause(JNIEnv *env, jobject self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "pause(%d)", task_ptr);
    Task* task = (Task*) task_ptr;
    task->pause();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativePauseAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->pauseAndWait();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeResume(JNIEnv *env, jobject self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "resume(%d)", task_ptr);
    Task* task = (Task*) task_ptr;
    task->resume();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeResumeAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->resumeAndWait();
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStop(JNIEnv *env, jobject self, jlong task_ptr) {
    __android_log_print(ANDROID_LOG_VERBOSE, TAG, "stop(%d)", task_ptr);
    Task* task = (Task*) task_ptr;
    task->stop();
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