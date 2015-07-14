LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

# Include res dir from chips
chips_dir := ../../../frameworks/ex/chips/res
res_dirs := $(chips_dir) res

LOCAL_STATIC_JAVA_LIBRARIES := com.android.services.telephony.common \
        guava \
        com.mediatek.incallui.ext

# Include chips
LOCAL_STATIC_JAVA_LIBRARIES += android-common-chips

LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += telephony-common voip-common

LOCAL_SRC_FILES := $(call all-java-files-under, src)

# Include chips
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages com.android.ex.chips

LOCAL_PACKAGE_NAME := InCallUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVELEGED_MODULE := false

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))
