add_library(dxgi-capture-c-lib
        src/main/cpp/wrapper.cpp
        src/main/cpp/wrapper.cpp)

target_include_directories(dxgi-capture-c-lib PUBLIC src/main/public)
target_include_directories(dxgi-capture-c-lib PRIVATE src/main/include)

target_link_libraries(dxgi-capture-c-lib dxgi-capture-lib)