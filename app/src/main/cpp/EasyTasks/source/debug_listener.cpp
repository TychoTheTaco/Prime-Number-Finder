#include "debug_listener.h"
#include <iostream>

void DebugListener::onTaskStarted(Task* task) {
	std::cout << "[Task " << task->getId() << "] " << "onTaskStarted()" << std::endl;
}

void DebugListener::onTaskPausing(Task* task) {
	std::cout << "[Task " << task->getId() << "] " << "onTaskPausing()" << std::endl;
}

void DebugListener::onTaskPaused(Task* task) {
	std::cout << "[Task " << task->getId() << "] " << "onTaskPaused()" << std::endl;
}

void DebugListener::onTaskResuming(Task* task) {
	std::cout << "[Task " << task->getId() << "] " << "onTaskResuming()" << std::endl;
}

void DebugListener::onTaskResumed(Task* task) {
	std::cout << "[Task " << task->getId() << "] " << "onTaskResumed()" << std::endl;
}

void DebugListener::onTaskStopping(Task* task) {
	std::cout << "[Task " << task->getId() << "] " << "onTaskStopping()" << std::endl;
}

void DebugListener::onTaskStopped(Task* task) {
	std::cout << "[Task " << task->getId() << "] " << "onTaskStopped() Time Elapsed: " << task->getElapsedTime() << std::endl;
}