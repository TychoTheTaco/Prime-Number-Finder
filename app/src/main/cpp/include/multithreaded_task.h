#pragma once
#include "task.h"
#include <vector>

class MultithreadedTask : public Task {

	public:

	virtual void run() = 0;

	virtual void pause();
	virtual void pauseAndWait();
	virtual void resume();
	virtual void resumeAndWait();
	virtual void stop();
	virtual void stopAndWait();

	virtual float getProgress();

	protected:

	void addSubTask(Task* task);
	std::vector<Task*> getTasks();
	void startSubTasks();
	void finishSubTasks();

	// Calculate the average progress of all attached sub-tasks.
	float getAverageProgress();

	private:
	
	// List of sub-tasks
	std::vector<Task*> tasks;
};