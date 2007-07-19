JAVA_INC = /System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home/include
FFTW_INC = /sw/include
CFLAGS = -Wall 
INC = -I${JAVA_INC} -I${FFTW_INC}
LIB = -L/sw/lib -lm -lfftw3

all:
	gcc ${CFLAGS} ${INC} ${LIB} -framework JavaVM -dynamiclib -o libfftw.jnilib Fftw.c 
 
