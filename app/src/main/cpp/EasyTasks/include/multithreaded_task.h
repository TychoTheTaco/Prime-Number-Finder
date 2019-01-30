#pragma once
#include "task.h"

class MultithreadedTask : public Task {

	public:

	virtual void run() = 0;

	virtual void pause();
	virtual void pauseAndWait();
	virtual void resume();
	virtual void stop();

	protected:

	void addSubTask(Task* task);
	void startSubTasks();
	void finishSubTasks();

	private:
	
	std::vector<Task*> tasks;

};