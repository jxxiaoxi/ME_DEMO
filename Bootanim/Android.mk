LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_JAVA_LIBRARIES := mediatek-framework

LOCAL_SHARED_LIBRARIES += libfileopt

LOCAL_STATIC_JAVA_LIBRARIES := com.mediatek.settings.ext \
                               com.mediatek.keyguard.ext 
LOCAL_STATIC_LIBRARIES := libcutils \
                            liblog

LOCAL_PACKAGE_NAME := Bootanim
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := full

include $(BUILD_PACKAGE)
