#include <iostream>
#include "test_task.h"
#include "find_primes_task.h"
#include "debug_listener.h"
#include <chrono>

void printProgress(Task* task) {
	while (task->getState() != Task::STOPPED) {
		std::cout << "Progress: " << task->getProgress() << std::endl;
		std::this_thread::sleep_for(std::chrono::milliseconds(10));
	}
	std::cout << "Watchdog terminated." << std::endl;
}

int main() {

	DebugListener listener;

	// Test single threaded task
	std::cout << "Testing single-threaded" << std::endl;
	TestTask task;
	task.addTaskListener(&listener);
	std::thread* thread0 = task.startOnNewThread();
	task.pauseAndWait();
	task.resume();
	task.finish();
	std::cout << std::endl;

	// Test multithreaded task
	std::cout << "Testing multi-threaded" << std::endl;
	FindPrimesTask find_primes_task(0, 1000000, FindPrimesTask::BRUTE_FORCE, 1);
	find_primes_task.addTaskListener(&listener);
	std::thread* thread1 = find_primes_task.startOnNewThread();
	find_primes_task.pause();
	//find_primes_task.resume();
	//std::thread timer(&printProgress, &find_primes_task);
	find_primes_task.finish();
	//timer.join();

	std::cout << "Finished all tests." << std::endl;
	system("pause");
	return 0;
}
