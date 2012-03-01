libogg-vorbis-android
=====================

This is a simple JNI wrapper for the libogg-vorbis library from Xiph.org.
It is set up to use the floating point instructions in an arm architecture,
and is plainly pointed at being used on Android. The intent of the wrapper
was to emulate a FileInputStream and FileOutputStream. Everybody knows how
to use them, and 95% of the time, this will do the job.

Anything that needs to be customized is done through the VorbisInfo class.
This class has defaults that work well for the Android environment.

VorbisInfo
----------

The VorbisInfo object includes several fields. These all are set to defaults
that make sense. Read the comments for more information.

Supported Devices
-----------------

After trying this with software-emulated floating point, it became apparent
that floating point operations are required to make this project worth your
time. The libraries are set to compile natively to requiring a floating point
coprocessor. This, unfortunately, means that you will crash on an emulator.

This code has been lightly tested on an HTC Incredible.


License
-------

The C code used to interface with Xiph.org's liboggvorbis is under whatever
license they are under, especially because the code is closely related to
their sample code. The Java code is also under the same license, just for
simplicity's sake.
