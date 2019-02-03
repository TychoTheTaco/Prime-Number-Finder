#include <iostream>
#include "test_task.h"
#include "find_primes_task.h"
#include "debug_listener.h"

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
	FindPrimesTask find_primes_task(0, 1000, FindPrimesTask::BRUTE_FORCE, 1);
	find_primes_task.addTaskListener(&listener);
	std::thread* thread1 = find_primes_task.startOnNewThread();
	//find_primes_task.pauseAndWait();
	//find_primes_task.resume();
	find_primes_task.finish();

	std::cout << "Finished all tests." << std::endl;
	system("pause");
	return 0;
}
