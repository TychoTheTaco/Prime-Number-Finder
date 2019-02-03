#include "find_primes_task.h"
#include "debug_listener.h"
#include <cmath>
#include <algorithm>
#include <queue>
#include <string>
#include <fstream>

FindPrimesTask::FindPrimesTask(unsigned long start_value, unsigned long end_value, SearchMethod search_method, unsigned int thread_count) {
	this->start_value = start_value;
	this->end_value = end_value;
	this->search_method = search_method;
	this->thread_count = thread_count;
	this->cache_dir = "task_" + std::to_string(this->getId()) + "_cache";
}

FindPrimesTask::~FindPrimesTask() {

}

void FindPrimesTask::run() {
	//Determine best search mode to use
	if (this->thread_count % 2 == 0) {
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
			searchPacketMode((unsigned long)((getRange() / this->thread_count) * 0.1));
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
	const unsigned long partition_size = this->getRange() / this->thread_count;
	std::cout << "Partition Size: " << partition_size << std::endl;
	for (unsigned int i = 0; i < this->thread_count; ++i) {
		unsigned long start = this->start_value + (i * partition_size + 1);
		if (start % 2 == 0) ++start;
		BruteForceTask* task = new BruteForceTask(start, this->start_value + (i + 1) * partition_size, 2);
		task->addTaskListener(new DebugListener());
		this->addSubTask(task);
	}
	this->startSubTasks();
	this->finishSubTasks();
}

void FindPrimesTask::searchAlternateMode() {
	unsigned long *startValues = new unsigned long[this->thread_count];
	unsigned int increment = this->thread_count * 2;
	startValues[0] = (this->start_value % 2 == 0) ? (this->start_value + 1) :this->start_value;
	for (unsigned int i = 0; i < this->thread_count; i++) {
		unsigned long s = i == 0 ? startValues[0] : startValues[i - 1] + 2;
		if (s % 2 == 0) {
			s -= 1;
		}
		startValues[i] = s;
		BruteForceTask* task = new BruteForceTask(s, this->end_value, increment);
		task->addTaskListener(new DebugListener());
		this->addSubTask(task);
	}
	delete startValues;
	this->startSubTasks();
	this->finishSubTasks();
}

void FindPrimesTask::searchPacketMode(unsigned long packet_size) {
	std::cout << "packet_size: " << packet_size << std::endl;
	for (unsigned int i = 0; i < std::ceil((double) getRange() / packet_size); i++) {
		unsigned long start = this->start_value + (i * packet_size + 1);
		if (start % 2 == 0) start++;
		BruteForceTask* task = new BruteForceTask(start, std::min(this->start_value + (i + 1) * packet_size, this->end_value), 2);
		task->addTaskListener(new DebugListener());
		this->addSubTask(task);;
	}
	executeThreadPool();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// [FindPrimesTask] Getters
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

unsigned long FindPrimesTask::getStartValue() {
	return this->start_value;
}

unsigned long FindPrimesTask::getEndValue() {
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
		}
	}
	return total;
}

unsigned long FindPrimesTask::getRange() {
	return this->end_value - this->start_value;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// [FindPrimesTask::BruteForceTask]
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// NOTE: start_value MUST be an odd number and increment MUST be an even number!
FindPrimesTask::BruteForceTask::BruteForceTask(unsigned long start_value, unsigned long end_value, unsigned int increment): start_value(start_value), end_value(end_value), increment(increment) {
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
		const int sqrt_max = (int) std::sqrt(current_number);

		// Assume the number is prime
		bool isPrime = true;

		// Check if the number is divisible by every odd number below it's square root.
		for (int i = 3; i <= sqrt_max; i += 2) {

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
}

float FindPrimesTask::BruteForceTask::getProgress() {
	return end_value == FindPrimesTask::RANGE_INFINITY ? 0 : (float)(current_number - start_value) / (end_value - start_value);
}

void FindPrimesTask::BruteForceTask::dispatchPrimeFound(unsigned long number) {
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
	fos.open("cache", std::ios::binary | std::ios::out | std::ios::app);
	fos.write(reinterpret_cast<const char*>(&primes[0]), primes.size() * sizeof(unsigned long));
	fos.close();
}

unsigned int FindPrimesTask::BruteForceTask::getPrimeCount() {
	return this->prime_count;
}