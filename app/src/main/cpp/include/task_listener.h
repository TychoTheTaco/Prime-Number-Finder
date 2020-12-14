#pragma once

#include <atomic>

class Task;

class TaskListener {

    public:

    virtual void onTaskStarted(Task* task);

    virtual void onTaskPausing(Task* task);

    virtual void onTaskPaused(Task* task);

    virtual void onTaskResuming(Task* task);

    virtual void onTaskResumed(Task* task);

    virtual void onTaskStopping(Task* task);

    virtual void onTaskStopped(Task* task);

};