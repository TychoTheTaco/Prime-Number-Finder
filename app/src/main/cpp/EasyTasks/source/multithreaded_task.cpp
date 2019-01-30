#include "multithreaded_task.h"

void MultithreadedTask::pause() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->requested_state = PAUSED;
	if (this->state == RUNNING) {
		dispatchPausing();

		//Pause subtasks
		std::thread([this]() {
			for (Task* task : this->tasks) {
				std::cout << task << std::endl;
				task->pauseAndWait();
			}
			this->dispatchPaused();
		}).detach();
	}
}

void MultithreadedTask::pauseAndWait() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->requested_state = PAUSED;
	if (this->state == RUNNING) {
		this->dispatchPausing();
		for (Task* task : this->tasks) {
			std::cout << task << std::endl;
			task->pauseAndWait();
		}
		this->dispatchPaused();
	}
}

void MultithreadedTask::resume() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->requested_state = RUNNING;
	if (this->state == PAUSED) {
		dispatchResuming();

		//Resume subtasks
		std::thread([this]() {
			for (Task* task : this->tasks) {
				task->resumeAndWait();
			}
			this->dispatchResumed();
		}).detach();
	}
}

void MultithreadedTask::stop() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->requested_state = STOPPED;
	if (this->state == RUNNING) {
		dispatchStopping();

		//Stop subtasks
		std::thread([this]() {
			for (Task* task : this->tasks) {
				task->stopAndWait();
			}
		}).detach();
	}
}

void MultithreadedTask::addSubTask(Task* task) {
	this->tasks.push_back(task);
}

void MultithreadedTask::startSubTasks() {
	for (Task *task : this->tasks) {
		std::thread* thread = task->startOnNewThread();
	}
}

void MultithreadedTask::finishSubTasks() {
	for (Task *task : this->tasks) {
		task->finish();
	}
}