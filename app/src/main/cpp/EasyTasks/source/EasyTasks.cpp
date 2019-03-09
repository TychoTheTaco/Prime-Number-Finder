#include <iostream>
#include "find_primes_task.h"
#include "debug_listener.h"
#include <chrono>

class DListener : public DebugListener {
	void onTaskStarted(Task* task) {
		std::cout << "[Task " << task->getId() << "] " << "DListener onTaskStarted()" << std::endl;
	}

	void onTaskPausing(Task* task) {
		std::cout << "[Task " << task->getId() << "] " << "DListener onTaskPausing()" << std::endl;
	}

	void onTaskPaused(Task* task) {
		std::cout << "[Task " << task->getId() << "] " << "DListener onTaskPaused()" << std::endl;
	}

	void onTaskResuming(Task* task) {
		std::cout << "[Task " << task->getId() << "] " << "DListener onTaskResuming()" << std::endl;
	}

	void onTaskResumed(Task* task) {
		std::cout << "[Task " << task->getId() << "] " << "DListener onTaskResumed()" << std::endl;
	}

	void onTaskStopping(Task* task) {
		std::cout << "[Task " << task->getId() << "] " << "DListener onTaskStopping()" << std::endl;
	}

	void onTaskStopped(Task* task) {
		std::cout << "[Task " << task->getId() << "] " << "DListener onTaskStopped()" << std::endl;
	}
};

void printProgress(Task* task) {
	while (task->getState() != Task::STOPPED) {
		std::cout << "Progress: " << task->getProgress() << std::endl;
		std::this_thread::sleep_for(std::chrono::milliseconds(10));
	}
	std::cout << "Watchdog terminated." << std::endl;
}

int main() {

	DebugListener listener;

	// Test multithreaded task
	std::cout << "Testing multi-threaded" << std::endl;
	FindPrimesTask find_primes_task(0, 1000000, FindPrimesTask::SIEVE_OF_ERATOSTHENES, 1);
	find_primes_task.addTaskListener(&listener);
	find_primes_task.addTaskListener(new DListener());
	find_primes_task.setCacheDirectory("cache");
	std::thread* thread1 = find_primes_task.startOnNewThread();
	//find_primes_task.pause();
	find_primes_task.pauseAndWait();
	//find_primes_task.stopAndWait();
	//find_primes_task.resume();
	//std::thread timer(&printProgress, &find_primes_task);
	find_primes_task.finish();
	find_primes_task.saveToFile("saved.txt");
	//timer.join();

	std::cout << "Finished all tests." << std::endl;
	system("pause");
	return 0;
}
