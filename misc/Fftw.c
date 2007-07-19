#include <stdio.h>
#include <stdlib.h>
#include <fftw3.h>
#include <math.h>
#include "Fftw.h"

static const unsigned int plan_flags = 0x01 | 0x06;

JNIEXPORT void JNICALL Java_misc_Fftw_fft(JNIEnv *env, jclass obj, jdoubleArray in, jdoubleArray out) {
	fftw_plan plan;
	double *in_buf, *out_buf;
	int length;
	
	in_buf = (*env)->GetDoubleArrayElements(env, in, 0);
	out_buf = (*env)->GetDoubleArrayElements(env, out, 0);
	length = (*env)->GetArrayLength(env, in);
	
	plan = fftw_plan_dft_r2c_1d(length, in_buf, (fftw_complex*)out_buf, plan_flags);
	
	fftw_execute(plan);
	
	fftw_destroy_plan(plan);
		
	(*env)->ReleaseDoubleArrayElements(env, in, in_buf, 0);
	(*env)->ReleaseDoubleArrayElements(env, out, out_buf, 0);
}

