LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CPPFLAGS += -std=c++11 \
				  -DTOMIC_DEVICE_V1 \
				  -DDNTDIAG_BUILD
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_MODULE    := dntdiag
LOCAL_SRC_FILES := RSerialPort.cpp 

include $(BUILD_SHARED_LIBRARY)
