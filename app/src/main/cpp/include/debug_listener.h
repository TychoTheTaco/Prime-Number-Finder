#pragma once

#include "task.h"

class DebugListener : public TaskListener {
    void onTaskStarted(Task* task) override;

    void onTaskPausing(Task* task) override;

    void onTaskPaused(Task* task) override;

    void onTaskResuming(Task* task) override;

    void onTaskResumed(Task* task) override;

    void onTaskStopping(Task* task) override;

    void onTaskStopped(Task* task) override;
};