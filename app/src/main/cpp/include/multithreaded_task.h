#pragma once

#include "task.h"
#include <vector>

class MultithreadedTask : public Task {

    public:

    void run() override = 0;

    void pause() override;

    void pauseAndWait() override;

    void resume() override;

    void resumeAndWait() override;

    void stop() override;

    void stopAndWait() override;

    float getProgress() override;

    protected:

    void addSubTask(Task* task);

    const std::vector<std::unique_ptr<Task>> &getTasks();

    void startSubTasks();

    void finishSubTasks();

    // Calculate the average progress of all attached sub-tasks.
    float getAverageProgress();

    private:

    // List of sub-tasks
    std::vector<std::unique_ptr<Task>> tasks;
};