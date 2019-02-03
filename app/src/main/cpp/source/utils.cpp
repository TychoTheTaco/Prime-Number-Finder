#include <jni.h>
#include <string>
#include <iostream>
#include <android/log.h>
#include <cmath>
#include <sstream>
#include "find_primes_task.h"
#include <sstream>

#define APP_NAME "PrimeNumberFinder"

extern "C" JNIEXPORT jstring JNICALL Java_com_tycho_app_primenumberfinder_activities_MainActivity_testNative(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesTask_startNativeTask(JNIEnv *env, jobject self) {
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Starting native task...");
    FindPrimesTask task(0, 10000000);
    task.start();
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Native task finished in %d ms.", task.getElapsedTime());
}

extern "C" JNIEXPORT jint JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesTask_nativeSieve(JNIEnv *env, jobject /* this */,
                                                                                                                     jlong start_value, jlong end_value) {
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Testing native sieve...");

    bool* array = new bool[(int) end_value + 1];
    for (int i = 0; i < (int) end_value + 1; ++i){
        array[i] = true;
    }

    int sqrtMax = (int) std::sqrt(end_value);
    int primeCount = 0;

    // mark non-primes <= n using Sieve of Eratosthenes
    for (int factor = 2; factor <= sqrtMax; factor++) {

        // if factor is prime, then mark multiples of factor as nonprime
        // suffices to consider mutiples factor, factor+1, ...,  n/factor
        if (array[factor]) {
            for (long j = factor; factor * j <= end_value; j++) {
                long number = factor * j;
                array[number] = false;
            }
        }
    }

    //Count primes
    for (int counter = (int) (start_value > 2 ? start_value : 2); counter <= end_value; counter++) {
        if (array[counter]) {
            primeCount++;
        }
    }

    delete[] array;
    return primeCount;
}

extern "C" JNIEXPORT jint JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesTask_nativeBrute(JNIEnv *env, jobject self,
                                                                                                                     jlong start_value, jlong end_value) {
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Testing native brute...");

    int primeCount = 0;
    long current = start_value;

    if (start_value <= 2){
        ++primeCount;
        current = 3;
    }

    jclass cls = env->FindClass("com/tycho/app/primenumberfinder/modules/findprimes/FindPrimesTask$BruteForceTask");
    jmethodID try_pause_id = env->GetMethodID(cls, "tryPause", "()V");
    jmethodID should_stop_id = env->GetMethodID(cls, "shouldStop", "()Z");

    while (current <= end_value) {
        int sqrtMax = (int) std::sqrt(current);

        // Assume the number is prime
        bool isPrime = true;

        // Check if the number is divisible by every odd number below it's square root.
        for (int i = 2; i <= sqrtMax; i += 1) {

            // Check if the number divides perfectly
            if (current % i == 0) {
                isPrime = false;
                break;
            }
        }

        env->CallVoidMethod(self, try_pause_id);
        if (env->CallBooleanMethod(self, should_stop_id)){
            break;
        }

        // Check if the number was prime
        if (isPrime) {
            primeCount++;
        }

        current += 1;
    }
    return primeCount;
}
