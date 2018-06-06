# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -Wall -fno-strict-aliasing -Wno-invalid-offsetof")

add_custom_target(
    run_rogo ALL
    DEPENDS ${CMAKE_CURRENT_BINARY_DIR}/__always_run.h
    )

add_custom_command(
    OUTPUT
      ${CMAKE_CURRENT_BINARY_DIR}/__always_run.h  # Non-existent; ensures we run each time
      ${CMAKE_SOURCE_DIR}/../../../Build/Android/Source/RogueProgram.h
      ${CMAKE_SOURCE_DIR}/../../../Build/Android/Source/RogueProgram.cpp
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}/../../..
    COMMAND rogo android
)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             plasmacore

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/AndroidPlasmacore.cpp
             ../../../Build/Android/Source/RogueProgram.cpp )

target_include_directories( plasmacore PRIVATE ../../../Build/Android/Source )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       plasmacore
                       GLESv2

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
