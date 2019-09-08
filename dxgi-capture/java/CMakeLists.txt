find_package(SWIG REQUIRED)
include(UseSWIG)

find_package(Java COMPONENTS Development REQUIRED)
find_package(JNI REQUIRED)

set(CMAKE_SWIG_FLAGS "")
set(PACKAGE_NAME com.github.rossdanderson.backlight.app.screen.source.dxgi.generated)
list(APPEND CMAKE_SWIG_FLAGS "-package;${PACKAGE_NAME}")

set_property(SOURCE
        src/main/swig/Capture.i
        PROPERTY CPLUSPLUS ON)

set(CAPTURE_NAME dxgi-capture-java)

swig_add_library(${CAPTURE_NAME}
        TYPE SHARED
        LANGUAGE java
        SOURCES
        src/main/swig/Capture.i)

set_target_properties(${SWIG_MODULE_${CAPTURE_NAME}_REAL_NAME} PROPERTIES SWIG_USE_TARGET_INCLUDE_DIRECTORIES TRUE)
target_link_libraries(${SWIG_MODULE_${CAPTURE_NAME}_REAL_NAME} dxgi-capture-lib)
target_include_directories(${SWIG_MODULE_${CAPTURE_NAME}_REAL_NAME} PRIVATE ${JNI_INCLUDE_DIRS})

install(TARGETS dxgi-capture-java
        DESTINATION app/native-libs)
install(DIRECTORY ${CMAKE_BINARY_DIR}/java/
        DESTINATION app/src/generated/java/com/github/rossdanderson/backlight/app/screen/source/dxgi/generated
        FILES_MATCHING PATTERN "*.java")