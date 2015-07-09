LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
LOCAL_MODULE := libfileopt
LOCAL_SRC_FILES := FileOPT.c
 
LOCAL_LDLIBS += -llog
LOCAL_STATIC_LIBRARIES := libcutils liblog
 
include $(BUILD_SHARED_LIBRARY)
