#include "find_primes_task.h"
#include "debug_listener.h"
#include <cmath>
#include <algorithm>
#include <queue>
#include <string>
#include <iostream>
#include <fstream>
#include <stdint.h>
#include <bitset>

#ifdef _WIN32
#define PLATFORM "Windows"
#else
#define PLATFORM "Unknown"
#endif


FindPrimesTask::FindPrimesTask(num_type start_value, num_type end_value, SearchMethod search_method, unsigned int thread_count) : start_value(start_value), end_value(end_value), search_method(search_method), thread_count(thread_count) {
	std::cout << "Platform: " << PLATFORM << std::endl;
}

FindPrimesTask::~FindPrimesTask() {
	
}

void FindPrimesTask::run() {

	//TODO: If no cache directory specified, then use this as a default location
	//Create cache directory at current location
	//this->setCacheDirectory("cache");

	switch (this->search_method) {
		case BRUTE_FORCE:
			//Determine best search mode to use
			if (this->thread_count == 1) {
				this->search_mode = PARTITION;
			} else if (this->thread_count % 2 == 0) {
				this->search_mode = ALTERNATE;
			} else {
				this->search_mode = PACKET;
			}

			switch (this->search_mode) {
				case PARTITION:
					searchPartitionMode();
					break;

				case ALTERNATE:
					searchAlternateMode();
					break;

				case PACKET:
					//The optimal packet size is roughly 10% of each thread's total workload
					searchPacketMode((num_type)((getRange() / this->thread_count) * 0.1));
					break;
			}
			break;

		case SIEVE_OF_ERATOSTHENES:
			SieveTask* task = new SieveTask(start_value, end_value);
			addSubTask(task);
			this->startSubTasks();
			this->finishSubTasks();
			break;
	}
	std::cout << "Found " << getPrimeCount() << " primes." << std::endl;
}

unsigned int count = 0;
std::recursive_mutex m_lock;
std::condition_variable_any m_cv;

class TL : public TaskListener {
	public:
	void onTaskStopped(Task* task) {
		std::lock_guard<std::recursive_mutex> lock(m_lock);
		count--;
		m_cv.notify_all();
	}
};

void FindPrimesTask::executeThreadPool() {
	std::vector<std::thread*> pool;
	std::queue<Task*> pending;
	for (Task* task : this->getTasks()) {
		pending.push(task);
	}
	while (!pending.empty()) {
		Task* task = pending.front();
		pending.pop();
		task->addTaskListener(new TL());

		std::unique_lock<std::recursive_mutex> lock(m_lock);
		m_cv.wait(lock, [this]() {return count < this->thread_count; });
		count++;
		pool.push_back(task->startOnNewThread());
		lock.unlock();
	}

	for (std::thread* thread : pool) {
		thread->join();
	}
}

void FindPrimesTask::searchPartitionMode() {
	const num_type partition_size = this->getRange() / this->thread_count;
	std::cout << "Partition Size: " << partition_size << std::endl;
	for (unsigned int i = 0; i < this->thread_count; ++i) {
		num_type start = this->start_value + (i * partition_size + 1);
		if (start % 2 == 0) ++start;
		BruteForceTask* task = new BruteForceTask(this, start, this->start_value + (i + 1) * partition_size, 2);
		task->addTaskListener(new DebugListener());
		this->addSubTask(task);
	}
	this->startSubTasks();
	this->finishSubTasks();
}

void FindPrimesTask::searchAlternateMode() {
	num_type *startValues = new num_type[this->thread_count];
	unsigned int increment = this->thread_count * 2;
	startValues[0] = (this->start_value % 2 == 0) ? (this->start_value + 1) : this->start_value;
	for (unsigned int i = 0; i < this->thread_count; i++) {
		num_type s = i == 0 ? startValues[0] : startValues[i - 1] + 2;
		if (s % 2 == 0) {
			s -= 1;
		}
		startValues[i] = s;
		BruteForceTask* task = new BruteForceTask(this, s, this->end_value, increment);
		task->addTaskListener(new DebugListener());
		this->addSubTask(task);
	}
	delete startValues;
	this->startSubTasks();
	this->finishSubTasks();
}

void FindPrimesTask::searchPacketMode(num_type packet_size) {
	std::cout << "packet_size: " << packet_size << std::endl;
	for (unsigned int i = 0; i < std::ceil((double)getRange() / packet_size); i++) {
		num_type start = this->start_value + (i * packet_size + 1);
		if (start % 2 == 0) start++;
		BruteForceTask* task = new BruteForceTask(this, start, std::min(this->start_value + (i + 1) * packet_size, this->end_value), 2);
		task->addTaskListener(new DebugListener());
		this->addSubTask(task);;
	}
	executeThreadPool();
}

void FindPrimesTask::saveToFile(const std::string file_path) {
	std::cout << "Saving..." << std::endl;

	// Clear the file if it already exists
	int result = std::remove(file_path.c_str());

	// Header Format (bits)
	// [0 - 7] Version
	// [8 - 15] Header length (in bytes)
	// [16 - 23] Number size (in bytes) 

	// Open file
	std::ofstream output(file_path, std::ios::binary | std::ios::app);

	// Write version number
	char header[3];
	header[0] = 1; // Version
	header[1] = sizeof(header) / sizeof(*header); // Header length
	header[2] = sizeof(num_type); // Number size
	output.write(header, sizeof(header) / sizeof(*header));

	std::string* cache_files = new std::string[this->getTasks().size()];
	for (int i = 0; i < this->getTasks().size(); ++i) {
		cache_files[i] = dynamic_cast<FindPrimesTask::BruteForceTask*>(this->getTasks().at(i))->cache_file;
	}

	if (this->search_method == BRUTE_FORCE) {
		switch (this->search_mode) {
			case PARTITION:
			case PACKET:
				// Each cache file is already sorted and we just need to combine them all into 1 file
				for (int i = 0; i < this->getTasks().size(); ++i) {
					std::ifstream input(cache_files[i], std::ios::binary);
					std::copy(std::istreambuf_iterator<char>(input), std::istreambuf_iterator<char>(), std::ostreambuf_iterator<char>(output));
				}
				break;

			case ALTERNATE:
				// Each cache file is sorted, but we need to interleave the numbers of each file
				const int BUFFER_SIZE = sizeof(num_type) * 10;
				std::vector<std::queue<num_type>*> buffers;
				for (int i = 0; i < this->getTasks().size(); ++i) {
					buffers.push_back(new std::queue<num_type>());
				}

				int* offsets = new int[buffers.size()];
				for (int i = 0; i < buffers.size(); ++i) {
					offsets[i] = 0;
				}

				while (buffers.size() > 0) {

					//Read from file
					std::vector<int> pending_remove;
					for (int i = 0; i < buffers.size(); ++i) {
						if (buffers.at(i)->empty()) {
							std::ifstream input(cache_files[i], std::ios::binary);
							char buffer[BUFFER_SIZE];
							input.seekg(offsets[i]);
							input.read(buffer, BUFFER_SIZE);
							offsets[i] += input.gcount();
							for (int a = 0; a < input.gcount(); a += sizeof(num_type)) {
								buffers.at(i)->push(bytesToNumber(buffer + a));
							}

							if (input.eof()) {
								pending_remove.push_back(i);
							}
						}
					}

					//Remove empty queues
					std::vector<int>::iterator iterator = pending_remove.begin();
					while (iterator != pending_remove.end()) {
						if (buffers[*iterator]->empty()) {
							buffers.erase(buffers.begin() + *iterator);
							iterator = pending_remove.erase(iterator);
						} else {
							iterator++;
						}
					}

					if (buffers.size() == 0) break;

					//Find smallest number
					int smallest_index = 0;
					for (int i = 1; i < buffers.size(); ++i) {
						if (buffers[i][0] < buffers[smallest_index][0]) {
							smallest_index = i;
						}
					}

					//Write to file
					char buffer[sizeof(num_type)];
					numberToBytes(buffers[smallest_index]->front(), buffer);
					buffers[smallest_index]->pop();
					output.write(buffer, sizeof(num_type));

				}

				delete[] offsets;

				break;
		}
	}

	delete[] cache_files;
	output.close();

	std::cout << "Saved!" << std::endl;
}

num_type FindPrimesTask::bytesToNumber(char* bytes) {
	num_type number = 0;
	for (unsigned int i = 0, offset = sizeof(num_type) * 8 - 8; i < sizeof(num_type); ++i, offset -= sizeof(num_type)) {
		num_type n = (unsigned char)bytes[i];
		number |= (n << offset);
	}
	return number;
}

void FindPrimesTask::numberToBytes(num_type number, char destination[]) {
	for (unsigned int i = 0, offset = sizeof(num_type) * 8 - 8; i < sizeof(num_type); ++i, offset -= sizeof(num_type)) {
		destination[i] = number >> offset;
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// [FindPrimesTask] Getters
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

num_type FindPrimesTask::getStartValue() {
	return this->start_value;
}

num_type FindPrimesTask::getEndValue() {
	return this->end_value;
}

unsigned int FindPrimesTask::getThreadCount() {
	return this->thread_count;
}

unsigned int FindPrimesTask::getPrimeCount() {
	unsigned int total = 0;
	for (Task* task : this->getTasks()) {
		if (BruteForceTask* t = dynamic_cast<BruteForceTask*>(task)) {
			total += t->getPrimeCount();
		} else if (SieveTask* t = dynamic_cast<SieveTask*>(task)) {
			total += t->getPrimeCount();
		}
	}
	return total;
}

num_type FindPrimesTask::getRange() {
	return this->end_value - this->start_value;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// [FindPrimesTask] Setters
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void FindPrimesTask::setCacheDirectory(std::string directory) {
	this->cache_dir = directory + "/task_" + std::to_string(this->getId()) + "_cache";

	//Empty the cache directory
#ifdef _WIN32
	std::string cmd = "rmdir /s /q \"" + this->cache_dir + "\"";
#else
	std::string cmd = "rmdir \"" + this->cache_dir + "\"";
#endif
	std::cout << "Empty: " << cmd << std::endl;
	system(cmd.c_str());

	//Create cache directory
//#ifdef _WIN32
	cmd = "mkdir \"" + this->cache_dir + "\"";
//#else
//	cmd = "mkdir -p" + this->cache_dir;
//#endif // _WIN32
	system(cmd.c_str());


}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// [FindPrimesTask::BruteForceTask]
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// NOTE: start_value MUST be an odd number and increment MUST be an even number!
FindPrimesTask::BruteForceTask::BruteForceTask(const FindPrimesTask* parent, num_type start_value, num_type end_value, unsigned int increment) : parent(parent), start_value(start_value), end_value(end_value), increment(increment), current_number(start_value) {
	this->cache_file = parent->cache_dir + "/cache" + std::to_string(this->getId()) + ".txt";

	// Clear cache
	int result = std::remove(this->cache_file.c_str());
	std::cout << "Deleted File: " << result << std::endl;
}

FindPrimesTask::BruteForceTask::~BruteForceTask() {

}

void FindPrimesTask::BruteForceTask::run() {
	std::cout << "Starting BFT (" << this->getId() << "). start_value: " << start_value << " end_value: " << end_value << " increment: " << increment << std::endl;
	current_number = start_value;

	if (start_value <= 2) {
		if (end_value == FindPrimesTask::RANGE_INFINITY || end_value >= 2) {
			dispatchPrimeFound(2);
		}
		current_number += increment;
	}

	while (isRunning() && (current_number <= end_value || end_value == FindPrimesTask::RANGE_INFINITY)) {
		// We don't have to search past the sqrt of the number
		const num_type sqrt_max = std::sqrt(current_number);

		// Assume the number is prime
		bool isPrime = true;

		// Check if the number is divisible by every odd number below it's square root.
		for (num_type i = 3; i <= sqrt_max; i += 2) {

			// Check if the number divides perfectly
			if (current_number % i == 0) {
				isPrime = false;
				break;
			}
		}

		// Check if the number was prime
		if (isPrime) {
			dispatchPrimeFound(current_number);
		}

		current_number += increment;
	}
	std::cout << "Final cache write." << std::endl;
	writeToCache();
}

float FindPrimesTask::BruteForceTask::getProgress() {
	if (end_value == FindPrimesTask::RANGE_INFINITY) return 0;
	if (this->getState() == NOT_STARTED) return 0;
	if (this->getState() != STOPPED) {
		setProgress((float)(current_number - start_value) / (end_value - start_value));
	}
	return Task::getProgress();
}

void FindPrimesTask::BruteForceTask::dispatchPrimeFound(num_type number) {
	primes.push_back(number);
	++prime_count;

	if (buffer_size > 0 && primes.size() >= buffer_size) {
		std::cout << "Writing to cache!" << std::endl;
		writeToCache();
		primes.clear();
	}
}

void FindPrimesTask::BruteForceTask::writeToCache() {
	std::ofstream fos;
	fos.open(cache_file, std::ios::binary | std::ios::out | std::ios::app);
	for (num_type number : primes) {
		char data[8];
		for (int i = 0, offset = 56; i < 8; ++i, offset -= 8) {
			if (i < 8 - sizeof(num_type)) {
				data[i] = 0;
			} else {
				data[i] = number >> offset;
			}
		}
		fos.write(data, 8);
	}
	fos.close();
}

unsigned int FindPrimesTask::BruteForceTask::getPrimeCount() {
	return this->prime_count;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// [FindPrimesTask::SieveTask]
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

FindPrimesTask::SieveTask::SieveTask(num_type start_value, num_type end_value) : start_value(start_value), end_value(end_value) {

}

FindPrimesTask::SieveTask::~SieveTask() {

}

void FindPrimesTask::SieveTask::run() {
	std::cout << "Starting ST (" << this->getId() << "). start_value: " << start_value << " end_value: " << end_value << std::endl;

	bool* buffer = new bool[end_value + 1];
	for (num_type i = 0; i < end_value + 1; ++i) {
		buffer[i] = true;
	}

	const num_type sqrt_max = std::sqrt(end_value);

	//Mark numbers
	for (num_type factor = 2; factor <= sqrt_max && isRunning(); ++factor) {
		if (buffer[factor]) {
			for (num_type n = factor; factor * n <= end_value; ++n) {
				buffer[factor * n] = false;
			}
		}
	}

	//Count primes
	for (num_type i = (start_value > 2 ? start_value : 2); i < end_value && isRunning(); ++i) {
		if (buffer[i]) {
			primes.push_back(i);
			++prime_count;
		}
	}

	delete[] buffer;
}

unsigned int FindPrimesTask::SieveTask::getPrimeCount() {
	return this->prime_count;
}

float FindPrimesTask::SieveTask::getProgress() {
	return 0;
}