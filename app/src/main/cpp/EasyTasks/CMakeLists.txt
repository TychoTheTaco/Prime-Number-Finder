# CMakeList.txt : CMake project for EasyTasks, include source and define
# project specific logic here.
#
cmake_minimum_required (VERSION 3.8)

# set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} /SUBSYSTEM:WINDOWS")
# set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -save-temps")

#include_directories("examples")

# Add source to this project's executable.
SET(SOURCE_FILES
"source/EasyTasks.cpp"
"source/task.cpp"
"source/find_primes_task.cpp"
"source/task_listener.cpp"
"source/test_task.cpp"
"source/debug_listener.cpp"
"source/multithreaded_task.cpp"
)

set(HEADER_FILES
"include/task.h"
"include/find_primes_task.h"
"include/task_listener.h"
"include/test_task.h"
"include/debug_listener.h"
"include/multithreaded_task.h"
)

include_directories("include")

#add_library(Taskr STATIC ${SOURCE_FILES})
#set_target_properties(Taskr PROPERTIES
#VERSION ${PROJECT_VERSION}
#PUBLIC_HEADER ${HEADER_FILES}
#)
#target_include_directories(Taskr PRIVATE include)
#target_include_directories(Taskr PRIVATE source)

add_executable (EasyTasks ${SOURCE_FILES})

# target_compile_options(EasyTasks PUBLIC "-save-temps")

# TODO: Add tests and install targets if needed.

#if(msvc) # if using the vs compiler...
#    set_target_properties(easytasks properties link_flags_release "/subsystem:console")
#endif()