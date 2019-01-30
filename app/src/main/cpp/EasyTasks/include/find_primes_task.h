#pragma once
#include "multithreaded_task.h"
#include <iostream>
#include <cmath>

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

	FindPrimesTask(unsigned long start_value, unsigned long end_value, SearchMethod search_method = BRUTE_FORCE, unsigned int thread_count = 1);
	~FindPrimesTask();

	virtual void run();

	private:

	unsigned long start_value;
	unsigned long end_value;

	SearchMethod search_method;
	SearchMode search_mode;

	unsigned int thread_count;

	std::vector<unsigned int> primes;

	class BruteForceTask : public Task {
		public:
		BruteForceTask(unsigned long start_value, unsigned long end_value, unsigned int increment = 1);
		~BruteForceTask();

		virtual void run();

		private:
		unsigned long start_value;
		unsigned long end_value;

		int increment;

		unsigned int prime_count = 0;
		unsigned long current_number = 0;

		void dispatchPrimeFound(unsigned long number);
	};

	void preparePartitionMode();
	void prepareAlternateMode();
	void perparePacketMode();

	unsigned long getRange();

};