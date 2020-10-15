#pragma once

#include "multithreaded_task.h"

// The type of number used to store and process prime numbers. This determines the maximum prime number that can be calculated.
using num_type = uint_fast64_t;

class FindPrimesTask : public MultithreadedTask {

    public:

    static const unsigned int RANGE_INFINITY = 0;

    enum SearchMethod {
        BRUTE_FORCE,
        SIEVE_OF_ERATOSTHENES
    };

    enum SearchMode {
        PARTITION,
        ALTERNATE,
        PACKET
    };

    FindPrimesTask(num_type start_value, num_type end_value, SearchMethod search_method = BRUTE_FORCE, unsigned int thread_count = 1);

    virtual void run();

    void saveToFile(std::string file_path);

    bool isEndless();

    // Getters
    num_type getStartValue();

    num_type getEndValue();

    unsigned int getPrimeCount();

    SearchMethod getSearchMethod();

    unsigned int getThreadCount();

    std::string getCacheDirectory();

    std::string getStatus();

    num_type getCurrentFactor();

    //Setters
    void setCacheDirectory(std::string directory);

    private:

    num_type start_value;
    num_type end_value;

    SearchMethod search_method;
    SearchMode search_mode;

    const unsigned int thread_count;

    std::string cache_dir;

    void searchPartitionMode();

    void searchAlternateMode();

    void searchPacketMode(num_type packet_size);

    void executeThreadPool();

    num_type getRange();

    num_type bytesToNumber(char *bytes);

    void numberToBytes(num_type number, char destination[]);

    class BruteForceTask : public Task {
        friend class FindPrimesTask;

        public:
        BruteForceTask(const FindPrimesTask *parent, num_type start_value, num_type end_value, num_type increment = 1);

        virtual void run();

        virtual float getProgress();

        unsigned int getPrimeCount();

        private:
        const FindPrimesTask *parent;

        num_type start_value;
        num_type end_value;

        num_type increment;

        std::vector<num_type> primes;

        // Maximum buffer size before primes are saved to a cache file. Set to 0 to disable caching
        unsigned int buffer_size = 25000;
        unsigned int prime_count = 0;
        num_type current_number = 0;

        std::string cache_file;

        void dispatchPrimeFound(num_type number);

        void writeToCache();
    };

    class SieveTask : public Task {
        friend class FindPrimesTask;

        public:
        SieveTask(num_type start_value, num_type end_value);

        virtual void run();

        unsigned int getPrimeCount();

        std::vector<num_type> getPrimes();

        std::string getStatus();

        num_type getCurrentFactor();

        private:
        num_type start_value;
        num_type end_value;

        std::vector<num_type> primes;
        unsigned int prime_count = 0;
    };

};
