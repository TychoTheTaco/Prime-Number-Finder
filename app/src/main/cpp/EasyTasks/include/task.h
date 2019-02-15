#pragma once
#include <mutex>
#include <vector>
#include <condition_variable>
#include <iostream>
#include <thread>
#include "task_listener.h"

class Task {

	public:

	enum State {
		NOT_STARTED,
		STARTING,
		RUNNING,
		PAUSING,
		PAUSED,
		RESUMING,
		STOPPING,
		STOPPED
	};

	virtual void start();
	virtual std::thread* startOnNewThread();
	virtual void pause();
	virtual void pauseAndWait();
	virtual void resume();
	virtual void resumeAndWait();
	virtual void stop();
	virtual void stopAndWait();

	/*
	Wait for the task to finish, blocking the current thread until it finishes.
	*/
	void finish();

	bool addTaskListener(TaskListener* listener);
	bool removeTaskListener(TaskListener* listener);

	long getId() const;
	int64_t getStartTime() const;
	int64_t getEndTime() const;
	int64_t getElapsedTime() const;
	int64_t getEstimatedTimeRemaining() const;
	State getState() const;

	virtual float getProgress();

	protected:

	Task();

	State state = State::NOT_STARTED;

	State requested_state = State::NOT_STARTED;

	std::recursive_mutex state_lock;
	std::condition_variable_any condition_variable;

	virtual void run() = 0;
	void tryPause();
	bool shouldStop();
	bool isRunning();

	void dispatchPausing();
	void dispatchPaused();
	void dispatchResuming();
	void dispatchResumed();
	void dispatchStopping();

	void setProgress(float progress);

	private:

	static long next_id;
	long id;

	float progress = 0;

	int64_t start_time = 0;
	int64_t last_pause_time = 0;
	int64_t total_pause_time = 0;
	int64_t end_time = 0;

	// TODO: Thread safe? Java uses CopyOnWriteArrayList.
	std::vector<TaskListener*> task_listeners;

	// This mutex is used to ensure that all listeners for a given state are executed in order and not mixed together.
	std::mutex listener_mutex;

	void pauseThread();
	void processRequestedState();
	void dispatchStarting();
	void dispatchStarted();
	void dispatchStopped();

	void notifyOnTaskStarted();
	void notifyOnTaskPausing();
	void notifyOnTaskPaused();
	void notifyOnTaskResuming();
	void notifyOnTaskResumed();
	void notifyOnTaskStopping();
	void notifyOnTaskStopped();

	int64_t currentTimeMillis() const;
};