#pragma once
#include "task.h"

class DebugListener : public TaskListener {
	void onTaskStarted(Task* task);
	void onTaskPausing(Task* task);
	void onTaskPaused(Task* task);
	void onTaskResuming(Task* task);
	void onTaskResumed(Task* task);
	void onTaskStopping(Task* task);
	void onTaskStopped(Task* task);
};