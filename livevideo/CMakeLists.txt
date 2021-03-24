cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

add_library( # 编译的库的名称是 rtmp
             rtmp

             # 编译的 rtmp 库是静态库
             STATIC

             # rtmp 库的源文件
             amf.c
             hashswf.c
             log.c
             parseurl.c
             rtmp.c )
# 链接 src/main/cpp/librtmp 目录下的构建脚本
add_subdirectory(librtmp)

add_library( # 函数库名称
        native-lib

        # 动态库类型
        SHARED

        # 源文件
        native-lib.cpp )

find_library( # 日志库
        log-lib

        log )

target_link_libraries( # 链接动态库
        native-lib

        # 编译的 rtmp 静态库
        rtmp

        ${log-lib} )
