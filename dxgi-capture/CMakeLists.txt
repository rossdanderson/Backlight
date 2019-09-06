cmake_minimum_required(VERSION 3.14)
project(dxgi-capture VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_INSTALL_PREFIX ../../)

set_property(GLOBAL PROPERTY USE_FOLDERS ON)

add_subdirectory(lib)
add_subdirectory(java)