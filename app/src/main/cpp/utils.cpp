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

extern "C" JNIEXPORT jint JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesTask_nativeBrute(JNIEnv *env, jobject /* this */,
                                                                                                                     jlong start_value, jlong end_value) {
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Testing native brute...");

    int primeCount = 0;
    long current = start_value;

    if (start_value <= 2){
        ++primeCount;
        current = 3;
    }

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

        // Check if the number was prime
        if (isPrime) {
            primeCount++;
        }

        current += 1;
    }
    return primeCount;
}

extern "C" JNIEXPORT void JNICALL Java_com_tycho_app_primenumberfinder_modules_findprimes_FindPrimesTask_00024BruteForceTask_nativeRun(JNIEnv *env, jobject self,
                                                                                                                     jlong start_value, jlong end_value, jint increment) {
    __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "Native run()...");

    long current = start_value;
    if (start_value < 3) {
        if (end_value == -1 || end_value >= 2) {
            jclass cls = env->FindClass("com/tycho/app/primenumberfinder/modules/findprimes/FindPrimesTask$BruteForceTask");
            jmethodID id = env->GetMethodID(cls, "dispatchPrimeFound", "(J)V");
            env->CallVoidMethod(self, id, 2);
        }
        current += increment;
    }

    bool running = true;

    while (running && (current <= end_value || end_value == -1)) {

        /*
        Get the square root of the number. We only need to calculate up to the square root
        to determine if the number is prime. The square root of a long will always fit
        inside the value range of an int.
         */
        int sqrtMax = (int) std::sqrt(current);

        // Assume the number is prime
        bool isPrime = true;

        // Check if the number is divisible by every odd number below it's square root.
        for (int i = 3; i <= sqrtMax; i += 2) {

            /*
            TODO: Optimization
            Ideally, this check should go after the check for primality so it does not get
            called every iteration. For now, this will remain here in case a thread never
            finds a prime number.
             */
            /*jclass cls = env->FindClass("com/tycho/app/primenumberfinder/modules/findprimes/FindPrimesTask$BruteForceTask");
            jmethodID id = env->GetMethodID(cls, "tryPause", "()V");
            env->CallVoidMethod(self, id);

            cls = env->FindClass("com/tycho/app/primenumberfinder/modules/findprimes/FindPrimesTask$BruteForceTask");
            id = env->GetMethodID(cls, "shouldStop", "()Z");
            if (env->CallBooleanMethod(self, id)){
                running = false;
                break;
            }*/

            /*tryPause();
            if (shouldStop()) {
                running = false;
                break;
            }*/

            // Check if the number divides perfectly
            if (current % i == 0) {
                isPrime = false;
                break;
            }
        }

        // Check if the number was prime
        if (isPrime) {
            jclass cls = env->FindClass("com/tycho/app/primenumberfinder/modules/findprimes/FindPrimesTask$BruteForceTask");
            jmethodID id = env->GetMethodID(cls, "dispatchPrimeFound", "(J)V");
            env->CallVoidMethod(self, id, current);
        }

        current += increment;
    }
}
