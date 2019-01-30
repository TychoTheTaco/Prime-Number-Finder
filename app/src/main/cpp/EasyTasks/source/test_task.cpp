#include "test_task.h"

void TestTask::run() {
	long start_value = 0;
	long end_value = 2000000;

	int primeCount = 0;
	long current = start_value;

	if (start_value <= 2) {
		++primeCount;
		current = 3;
	}

	while (this->isRunning() && current <= end_value) {
		int sqrtMax = (int)std::sqrt(current);

		// Assume the number is prime
		bool isPrime = true;

		// Check if the number is divisible by every odd number below it's square root.
		for (int i = 2; i <= sqrtMax; i += 1) {

			// Check if the number divides perfectly
			if (current % i == 0) {
				isPrime = false;
				break;
			}
		}

		// Check if the number was prime
		if (isPrime) {
			primeCount++;
		}

		current += 1;
	}
}