#include "task.h"
#include <chrono>
#include <iostream>
#include <algorithm>

//TOODO:
// 1. Replace time measurements with chrono::now() instead of casting to millis every time
// 2. Create random UUIDs for new tasks

long Task::next_id = 0;

Task::Task() {
	this->id = next_id;
	this->next_id++;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Life-cycle methods
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void Task::start() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	if (this->state == NOT_STARTED || this->state == STARTING) {
		dispatchStarted();
	} else {
		throw std::runtime_error("Cannot start a task that has already started!");
	}
	lock.unlock();
	run();
	dispatchStopped();
}

std::thread* Task::startOnNewThread() {
	dispatchStarting();
	return new std::thread(&Task::start, this);
}

void Task::pause() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->requested_state = PAUSED;
	if (this->state == RUNNING) {
		dispatchPausing();
	}
}

void Task::pauseAndWait() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	pause();
	this->condition_variable.wait(lock, [this]() {return this->state == NOT_STARTED || this->state == PAUSED || this->state == STOPPED; });
}

void Task::resume() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->requested_state = RUNNING;
	if (this->state == PAUSED) {
		dispatchResuming();
	}
}

void Task::resumeAndWait() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	resume();
	this->condition_variable.wait(lock, [this]() {return this->state == NOT_STARTED || this->state == RUNNING || this->state == STOPPED; });
}

void Task::stop() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->requested_state = STOPPED;
	if (this->state != NOT_STARTED && this->state != STOPPING && this->state != STOPPED) {
		condition_variable.notify_all();
		dispatchStopping();
	}
}

void Task::stopAndWait() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	stop();
	this->condition_variable.wait(lock, [this]() {return this->state == NOT_STARTED || this->state == STOPPED; });
}

void Task::finish() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->condition_variable.wait(lock, [this]() {return this->state == STOPPED; });
}

void Task::pauseThread() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	dispatchPaused();
	this->condition_variable.wait(lock, [this]() {return this->requested_state != PAUSED; });
	dispatchResumed();
}

void Task::tryPause() {
	if (this->state == PAUSING) {
		this->pauseThread();
	}
}

bool Task::shouldStop() {
	return this->requested_state == STOPPED;
}

bool Task::isRunning() {
	tryPause();
	return !shouldStop();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Other
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void Task::processRequestedState() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	if (this->state != this->requested_state) {
		switch (this->requested_state) {
			case RUNNING:
				resume();
				break;

			case PAUSED:
				pause();
				break;

			case STOPPED:
				stop();
				break;
		}
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Getters
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

long Task::getId() const {
	return this->id;
}

int64_t Task::getStartTime() const {
	return this->start_time;
}

int64_t Task::getEndTime() const {
	return this->end_time;
}

int64_t Task::getElapsedTime() const {
	switch (this->state) {
		case NOT_STARTED:
			return 0;

		default:
		case RUNNING:
			return this->currentTimeMillis() - this->start_time - this->total_pause_time;

		case PAUSED:
		case RESUMING:
			return this->currentTimeMillis() - this->start_time - (this->currentTimeMillis() - this->last_pause_time) - this->total_pause_time;

		case STOPPED:
			return this->end_time - this->start_time - this->total_pause_time;
	}
	return this->end_time - this->start_time;
}

int64_t Task::getEstimatedTimeRemaining() {
	if (this->state == STOPPED) return 0;
	float progress = getProgress();
	if (progress == 0) return -1;
	return ((double)getElapsedTime() / progress) * (1.0f - progress);
}

Task::State Task::getState() const {
	return this->state;
}

float Task::getProgress() {
	return (getState() == STOPPED ? 1 : 0);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Dispatch methods
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void Task::dispatchStarting() {
	std::lock_guard<std::recursive_mutex> lock(this->state_lock);
	this->state = STARTING;
	this->condition_variable.notify_all();
}

void Task::dispatchStarted() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->start_time = currentTimeMillis();
	this->state = RUNNING;
	this->condition_variable.notify_all();
	lock.unlock();
	notifyOnTaskStarted();
	processRequestedState();
}

void Task::dispatchPausing() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->state = PAUSING;
	this->condition_variable.notify_all();
	lock.unlock();
	notifyOnTaskPausing();
}

void Task::dispatchPaused() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->last_pause_time = currentTimeMillis();
	this->state = PAUSED;
	this->condition_variable.notify_all();
	lock.unlock();
	notifyOnTaskPaused();
	processRequestedState();
}

void Task::dispatchResuming() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->state = RESUMING;
	this->condition_variable.notify_all();
	lock.unlock();
	notifyOnTaskResuming();
}

void Task::dispatchResumed() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->total_pause_time += (currentTimeMillis() - this->last_pause_time);
	this->state = RUNNING;
	this->condition_variable.notify_all();
	lock.unlock();
	notifyOnTaskResumed();
	processRequestedState();
}

void Task::dispatchStopping() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->state = STOPPING;
	this->condition_variable.notify_all();
	lock.unlock();
	notifyOnTaskStopping();
}

void Task::dispatchStopped() {
	std::unique_lock<std::recursive_mutex> lock(this->state_lock);
	this->end_time = currentTimeMillis();
	this->state = STOPPED;
	this->condition_variable.notify_all();
	lock.unlock();
	notifyOnTaskStopped();
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Notify methods
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

void Task::notifyOnTaskStarted() {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	for (TaskListener *listener : this->task_listeners) {
		listener->onTaskStarted(this);
	}
}

void Task::notifyOnTaskPausing() {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	for (TaskListener *listener : this->task_listeners) {
		listener->onTaskPausing(this);
	}
}

void Task::notifyOnTaskPaused() {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	for (TaskListener *listener : this->task_listeners) {
		listener->onTaskPaused(this);
	}
}

void Task::notifyOnTaskResuming() {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	for (TaskListener *listener : this->task_listeners) {
		listener->onTaskResuming(this);
	}
}

void Task::notifyOnTaskResumed() {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	for (TaskListener *listener : this->task_listeners) {
		listener->onTaskResumed(this);
	}
}

void Task::notifyOnTaskStopping() {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	for (TaskListener *listener : this->task_listeners) {
		listener->onTaskStopping(this);
	}
}

void Task::notifyOnTaskStopped() {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	for (TaskListener *listener : this->task_listeners) {
		listener->onTaskStopped(this);
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Listener methods
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

bool Task::addTaskListener(TaskListener* listener) {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	if (std::find(this->task_listeners.begin(), this->task_listeners.end(), listener) != this->task_listeners.end()) {
		return false;
	}
	this->task_listeners.push_back(listener);
	return true;
}

bool Task::removeTaskListener(TaskListener* listener) {
	std::lock_guard<std::recursive_mutex> lock(this->listener_mutex);
	if (std::find(this->task_listeners.begin(), this->task_listeners.end(), listener) != this->task_listeners.end()) {
		this->task_listeners.erase(std::remove(this->task_listeners.begin(), this->task_listeners.end(), listener));
		return true;
	}
	return false;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Helper methods
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

int64_t Task::currentTimeMillis() const {
	return std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
}