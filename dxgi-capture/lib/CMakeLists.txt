add_library(dxgi-capture-lib
        src/main/cpp/capture.cpp
        )

target_include_directories(dxgi-capture-lib PUBLIC src/main/public)
target_include_directories(dxgi-capture-lib PRIVATE src/main/include)

target_link_libraries(dxgi-capture-lib d3d11 dxgi)