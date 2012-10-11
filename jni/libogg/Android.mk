LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)

LOCAL_MODULE := libogg
LOCAL_CFLAGS += -I$(LOCAL_PATH)/../include -fsigned-char
LOCAL_CFLAGS += -march=armv6 -marm -mfloat-abi=softfp -mfpu=vfp


LOCAL_SRC_FILES := \
	bitwise.c \
	framing.c

include $(BUILD_SHARED_LIBRARY)
