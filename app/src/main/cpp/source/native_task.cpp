#include <jni.h>
#include <android/log.h>
#include <map>

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
        call(task_listener, method_ids[0]);
    }

    void onTaskPausing(Task* task){
        call(task_listener, method_ids[1]);
    }

    void onTaskPaused(Task* task){
        call(task_listener, method_ids[2]);
    }

    void onTaskResuming(Task* task){
        call(task_listener, method_ids[3]);
    }

    void onTaskResumed(Task* task){
        call(task_listener, method_ids[4]);
    }

    void onTaskStopping(Task* task){
        call(task_listener, method_ids[5]);
    }

    void onTaskStopped(Task* task){
        call(task_listener, method_ids[6]);
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

    void call(jobject object, jmethodID method_id){
        getEnv()->CallVoidMethod(object, method_id);
        jvm->DetachCurrentThread();
    }
};

std::map<std::string, NativeListener*> listener_map;

extern "C"{
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Life-cycle methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStart(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->start();
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStartOnNewThread(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->startOnNewThread();
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativePause(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->pause();
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativePauseAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->pauseAndWait();
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeResume(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->resume();
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeResumeAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->resumeAndWait();
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStop(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->stop();
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeStopAndWait(JNIEnv *env, jobject self, jlong task_ptr) {
    ((Task*) task_ptr)->stopAndWait();
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Listeners
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeAddTaskListener(JNIEnv *env, jobject self, jlong task_ptr, jobject task_listener, jstring id) {
    const char* string = env->GetStringUTFChars(id, 0);
    NativeListener* native_listener = new NativeListener(env, task_listener);
    listener_map[string] = native_listener;
    ((Task*) task_ptr)->addTaskListener(native_listener);
    env->ReleaseStringUTFChars(id, string);
}

JNIEXPORT jboolean JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeRemoveTaskListener(JNIEnv *env, jobject self, jlong task_ptr, jstring id) {
    const char* string = env->GetStringUTFChars(id, 0);
    NativeListener* native_listener = listener_map[string];
    listener_map.erase(string);
    env->ReleaseStringUTFChars(id, string);
    bool result = ((Task*) task_ptr)->removeTaskListener(native_listener);
    delete native_listener;
    return (jboolean ) result;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Time methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetStartTime(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jlong) ((Task*) task_ptr)->getStartTime();
}

JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetEndTime(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jlong) ((Task*) task_ptr)->getEndTime();
}

JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetElapsedTime(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jlong) ((Task*) task_ptr)->getElapsedTime();
}

JNIEXPORT jlong JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetEstimatedTimeRemaining(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jlong) ((Task*) task_ptr)->getEstimatedTimeRemaining();
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// State methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

JNIEXPORT jint JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetState(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jint) ((Task*) task_ptr)->getState();
}

JNIEXPORT jfloat JNICALL Java_com_tycho_app_primenumberfinder_NativeTask_nativeGetProgress(JNIEnv *env, jobject self, jlong task_ptr) {
    return (jfloat) ((Task*) task_ptr)->getProgress();
}

}