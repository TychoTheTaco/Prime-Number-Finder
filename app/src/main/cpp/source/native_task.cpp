#include <jni.h>
#include <android/log.h>
#include <map>

#include "find_primes_task.h"

#define TAG "PrimeNumberFinder [Native]"

class NativeListener: public TaskListener{
public:
    NativeListener(JNIEnv* env, std::string listener_id, jobject native_task_object): listener_id(listener_id), native_task_object(env->NewGlobalRef(native_task_object)){
        int result = env->GetJavaVM(&jvm);
        assert(result == JNI_OK);
        jclass cls = env->FindClass("com/tycho/app/primenumberfinder/NativeTask");
        method_ids[0] = env->GetMethodID(cls, "sendOnTaskStarted", "(Ljava/lang/String;)V");
        method_ids[1] = env->GetMethodID(cls, "sendOnTaskPausing", "(Ljava/lang/String;)V");
        method_ids[2] = env->GetMethodID(cls, "sendOnTaskPaused", "(Ljava/lang/String;)V");
        method_ids[3] = env->GetMethodID(cls, "sendOnTaskResuming", "(Ljava/lang/String;)V");
        method_ids[4] = env->GetMethodID(cls, "sendOnTaskResumed", "(Ljava/lang/String;)V");
        method_ids[5] = env->GetMethodID(cls, "sendOnTaskStopping", "(Ljava/lang/String;)V");
        method_ids[6] = env->GetMethodID(cls, "sendOnTaskStopped", "(Ljava/lang/String;)V");
    }

    void onTaskStarted(Task* task){
        assert(method_ids);
        call(native_task_object, method_ids[0], task);
    }

    void onTaskPausing(Task* task){
        call(native_task_object, method_ids[1], task);
    }

    void onTaskPaused(Task* task){
        call(native_task_object, method_ids[2], task);
    }

    void onTaskResuming(Task* task){
        call(native_task_object, method_ids[3], task);
    }

    void onTaskResumed(Task* task){
        call(native_task_object, method_ids[4], task);
    }

    void onTaskStopping(Task* task){
        call(native_task_object, method_ids[5], task);
    }

    void onTaskStopped(Task* task){
        call(native_task_object, method_ids[6], task);
    }

private:
    JavaVM* jvm;
    std::string listener_id;
    jobject native_task_object;
    jmethodID method_ids[7];

    void call(jobject object, jmethodID method_id, Task* task){
        JNIEnv* env;
        bool attached = false;
        switch (jvm->GetEnv((void**) &env, JNI_VERSION_1_6)){
            case JNI_EDETACHED:
                assert(jvm->AttachCurrentThread(&env, nullptr) == JNI_OK);
                attached = true;
                break;

            case JNI_OK:
                break;

            default:
                break;
        }
        assert(method_id);
        env->CallVoidMethod(object, method_id, env->NewStringUTF(listener_id.c_str()));

        // Detach from thread if we just attached
        if (attached){
            jvm->DetachCurrentThread();
        }
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
    const char* string = env->GetStringUTFChars(id, nullptr);
    NativeListener* native_listener = new NativeListener(env, string, self);
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