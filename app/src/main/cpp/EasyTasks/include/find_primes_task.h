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
	~FindPrimesTask();

	virtual void run();

	void saveToFile(const std::string file_path);

	// Getters
	num_type getStartValue();
	num_type getEndValue();
	unsigned int getPrimeCount();
	SearchMethod getSearchMethod();
	unsigned int getThreadCount();

	//Setters
	void setCacheDirectory(std::string directory);

	private:

	num_type start_value;
	num_type end_value;

	SearchMethod search_method;
	SearchMode search_mode;

	unsigned int thread_count;

	std::string cache_dir;

	void searchPartitionMode();
	void searchAlternateMode();
	void searchPacketMode(num_type packet_size);
	void executeThreadPool();

	num_type getRange();
	num_type bytesToNumber(char* bytes);
	void numberToBytes(num_type number, char destination[]);

	class BruteForceTask : public Task {
		friend class FindPrimesTask;

		public:
		BruteForceTask(const FindPrimesTask* parent, num_type start_value, num_type end_value, num_type increment = 1);
		~BruteForceTask();

		virtual void run();

		virtual float getProgress();

		unsigned int getPrimeCount();

		private:
		const FindPrimesTask* parent;

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
		~SieveTask();

		virtual void run();

		virtual float getProgress();

		unsigned int getPrimeCount();

		private:
		num_type start_value;
		num_type end_value;

		std::vector<num_type> primes;
		unsigned int prime_count = 0;
	};

};


/*

 private class SieveTask extends MultithreadedTask.SubTask {

		private String status = "searching";

private final Queue<Long> primes = new ArrayDeque<>();

private final int sqrtMax = (int)Math.sqrt(endValue);
private int factor;
private long counter;

@Override
protected void run() {
	//Assume all numbers are prime
	final BitSet bitSet = new BitSet((int)(endValue + 1));
	bitSet.set(0, bitSet.size() - 1, true);

	// mark non-primes <= n using Sieve of Eratosthenes
	for (factor = 2; factor <= sqrtMax; factor++) {

		// if factor is prime, then mark multiples of factor as nonprime
		// suffices to consider mutiples factor, factor+1, ...,  n/factor
		if (bitSet.get(factor)) {
			for (int j = factor; factor * j <= endValue; j++) {
				bitSet.set(factor * j, false);
			}
		}

		tryPause();
	}

	if (shouldStop()) {
		return;
	}

	status = "counting";

	//Count primes
	for (counter = (startValue > 2 ? startValue : 2); counter <= endValue; counter++) {
		if (bitSet.get((int)counter)) {
			primes.add(counter);
			primeCount++;
		}
		tryPause();
	}

	status = String.valueOf(getState());
}

@Override
public float getProgress() {
	switch (status) {
		case "searching":
			setProgress(((float)factor / sqrtMax) / 2);
			break;

		case "counting":
			setProgress(0.5f + (((float)counter / endValue) / 2));
			break;
	}
	return super.getProgress();
}

public int getFactor() {
	return factor;
}
	}

*/