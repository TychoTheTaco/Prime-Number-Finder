//
// Created by tycho on 3/11/2018.
//
#include <iostream>
#include <jni.h>
#include <android/log.h>

extern "C"{
JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_utils_Utils_compact(JNIEnv* env,jobject obj);
}

JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_utils_Utils_compact(JNIEnv* env,jobject obj){
    std::cout << "Compact from Utils.cpp" << std::endl;
    printf("This is a test.");
    //__android_log_print(ANDROID_LOG_DEBUG, "TAG", "Please work this time");
}