#pragma once
#include <mutex>

class SyncedStream {

	static std::mutex mutex;

	SyncedStream() {}
	~SyncedStream() {}

};