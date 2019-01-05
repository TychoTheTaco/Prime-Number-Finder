#include <jni.h>
#include <string>
#include <iostream>
#include <android/log.h>
#include <cmath>

#define APP_NAME "PrimeNumberFinder"

extern "C" JNIEXPORT jstring JNICALL Java_com_tycho_app_primenumberfinder_activities_MainActivity_testNative(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
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
