#include "find_primes_task.h"
#include "debug_listener.h"

FindPrimesTask::FindPrimesTask(unsigned long start_value, unsigned long end_value, SearchMethod search_method, unsigned int thread_count) {
	this->start_value = start_value;
	this->end_value = end_value;
	this->search_method = search_method;
	this->thread_count = thread_count;
}

FindPrimesTask::~FindPrimesTask() {

}

void FindPrimesTask::run() {
	this->preparePartitionMode();

	this->startSubTasks();
	this->finishSubTasks();
}

unsigned long FindPrimesTask::getRange() {
	return this->end_value - this->start_value;
}

void FindPrimesTask::preparePartitionMode() {
	const unsigned long partition_size = this->getRange() / this->thread_count;
	std::cout << "Partition Size: " << partition_size << std::endl;
	for (unsigned int i = 0; i < this->thread_count; ++i) {
		unsigned long start = this->start_value + (i * partition_size + 1);
		if (start % 2 == 0) ++start;
		BruteForceTask* task = new BruteForceTask(start, this->start_value + (i + 1) * partition_size, 2);
		task->addTaskListener(new DebugListener());
		this->addSubTask(task);
	}
}

// BruteForceTask

FindPrimesTask::BruteForceTask::BruteForceTask(unsigned long start_value, unsigned long end_value, unsigned int increment) {
	this->start_value = start_value;
	this->end_value = end_value;
	this->increment = increment;
}

FindPrimesTask::BruteForceTask::~BruteForceTask() {

}

void FindPrimesTask::BruteForceTask::run() {
	this->current_number = this->start_value;

	if (this->start_value <= 2) {
		if (this->end_value == FindPrimesTask::RANGE_INFINITY || this->end_value >= 2) {
			this->dispatchPrimeFound(2);
		}
		this->current_number += this->increment;
	}

	while (this->isRunning() && (this->current_number <= this->end_value || this->end_value == FindPrimesTask::RANGE_INFINITY)) {
		// We don't have to search past the sqrt of the number
		const int sqrt_max = (int) std::sqrt(this->current_number);

		// Assume the number is prime
		bool isPrime = true;

		// Check if the number is divisible by every odd number below it's square root.
		for (int i = 2; i <= sqrt_max; i += 1) {

			// Check if the number divides perfectly
			if (this->current_number % i == 0) {
				isPrime = false;
				break;
			}
		}

		// Check if the number was prime
		if (isPrime) {
			this->dispatchPrimeFound(this->current_number);
		}

		this->current_number += this->increment;
	}
}

void FindPrimesTask::BruteForceTask::dispatchPrimeFound(unsigned long number) {
	++this->prime_count;
}