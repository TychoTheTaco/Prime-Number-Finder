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

        /**
         * This method searches for prime numbers using sequential "packets" of numbers. For example, if the packet size is 100, the search space will be divided into packets of
         * 100 and they will be worked on by the thread pool. Each packet is a block of sequential numbers.
         */
        PACKET
    };

    FindPrimesTask(num_type start_value, num_type end_value, SearchMethod search_method = BRUTE_FORCE, unsigned int thread_count = 1);

    void run() override;

    void saveToFile(const std::string &file_path);

    bool isEndless() const;

    // Getters
    num_type getStartValue() const;

    num_type getEndValue() const;

    unsigned int getPrimeCount();

    SearchMethod getSearchMethod();

    unsigned int getThreadCount() const;

    std::string getCacheDirectory();

    //Setters
    void setCacheDirectory(const std::string &directory);

    private:

    std::mutex saveLock;

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

    num_type getRange() const;

    num_type bytesToNumber(const char* bytes);

    void numberToBytes(num_type number, char destination[]);

    class PrimesTask : public Task {

        public:
        PrimesTask(num_type startValue, num_type endValue);

        virtual size_t getPrimeCount() const = 0;

        protected:
        const num_type startValue;
        const num_type endValue;
    };

    class BruteForceTask : public PrimesTask {
        friend class FindPrimesTask;

        public:
        BruteForceTask(const FindPrimesTask* parent, num_type start_value, num_type end_value, num_type increment = 1);

        void run() override;

        float getProgress() override;

        size_t getPrimeCount() const override;

        private:
        const FindPrimesTask* parent;

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

    class SieveTask : public PrimesTask {
        friend class FindPrimesTask;

        public:
        SieveTask(num_type startValue, num_type endValue);

        void run() override;

        size_t getPrimeCount() const override;

        std::vector<num_type> getPrimes();

        private:

        std::vector<num_type> primes;
        unsigned int prime_count = 0;
    };

};
