LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := TimeWidgetProviderThree
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

