/* Programmer: Nicholas Wertzberger
 *
 * The Java interface (a la outputstream) for vorbis.
 */
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <math.h>
#include <vorbis/codec.h>

#include <stream/util.h>

/* I really don't want to figure out what vorbis is storing in their structs.
 * Let's just store it all in this here array and call it good.
 */

/* This is arbitrary, and it's overkill */
#define SAMPLE_SIZE 1024
#define MAX_OUTPUTSTREAMS 4
struct output_stream {
	FILE * 				fh;
	vorbis_info 		vi;
	vorbis_comment 		vc;
	vorbis_dsp_state 	vd;
	vorbis_block 		vb;
	ogg_stream_state 	os;
	ogg_page 			og;
	ogg_packet 			op;
	int 				channels;
};
static struct output_stream output_streams[MAX_OUTPUTSTREAMS];

/* Based on code from:
 * http://svn.xiph.org/trunk/vorbis/examples/encoder_example.c
 * Returns a pointer to the stream struct related to that current vorbis file.
 */
jint Java_org_ideaheap_io_VorbisFileOutputStream_create(JNIEnv* env,
		jobject this, jstring path, jobject options) {
	/* Configuration structs */
	struct output_stream * optr = NULL;

	/* packet stream structs */
	ogg_packet header;
	ogg_packet header_comm;
	ogg_packet header_code;

	int ret; /* Return code storage for function calls */
	int eos = 0; /* End of Stream */
	int stream_idx;

	/* Find an unused output_stream */
	for (stream_idx = 0; stream_idx < MAX_OUTPUTSTREAMS; stream_idx++) {
		if (output_streams[stream_idx].fh == NULL) {
			const jbyte * pchars = (*env)->GetStringUTFChars(env, path, NULL);
			if (pchars == NULL) {
				/* Exception Already thrown */
				return;
			}
			/* We found one! */
			optr = &output_streams[stream_idx];
			optr->fh = fopen(pchars, "w");
			if (optr->fh == NULL) {
				JNU_ThrowByName(env, "java/io/IOException",
						"Error Creating File Handle", 0);
				return;
			}
			(*env)->ReleaseStringUTFChars(env, path, pchars);
			break;
		}
	}
	if (stream_idx == MAX_OUTPUTSTREAMS) {
		JNU_ThrowByName(env, "java/io/IOException",
				"Too Many Vorbis OutputStreams", stream_idx);
		return;
	}

	/* Step 1. According to documented workflow.
	 * http://xiph.org/vorbis/doc/libvorbis/overview.html
	 */
	vorbis_info_init(&optr->vi);

	/* TODO: make these options passed in. We definitely don't need stereo
	 * most of the time.
	 */
	optr->channels = 2;
	ret = vorbis_encode_init_vbr(&optr->vi, optr->channels, 44100, 0.1);
	if (ret) {
		JNU_ThrowByName(env, "java/io/IOException", "Bad Bitrate", ret);
		return;
	}

	/* Step 2. */
	vorbis_analysis_init(&optr->vd, &optr->vi);
	vorbis_block_init(&optr->vd, &optr->vb);

	/* Step 3. */
	vorbis_comment_init(&optr->vc);

	/* A 0 means all is well. */
	srand(time(NULL));
	ogg_stream_init(&optr->os, rand());

	ret = vorbis_analysis_headerout(&optr->vd, &optr->vc, &header, &header_comm,
			&header_code);

	if (ret) {
		JNU_ThrowByName(env, "java/io/IOException", "header init error", ret);
		return;
	}

	ogg_stream_packetin(&optr->os, &header); /* placed in its own page */
	ogg_stream_packetin(&optr->os, &header_comm);
	ogg_stream_packetin(&optr->os, &header_code);

	/* This ensures the actual
	 * audio data will start on a new page, as per spec
	 */
	while (1) {
		int result = ogg_stream_flush(&optr->os, &optr->og);
		if (result == 0)
			break;
		/* TODO: Tie this into the file handle passed in... Or whatever */
		fwrite(optr->og.header, 1, optr->og.header_len, optr->fh);
		fwrite(optr->og.body, 1, optr->og.body_len, optr->fh);
	}
	return stream_idx;
}

/* Write out to the file handle
 *
 */
jint Java_org_ideaheap_io_VorbisFileOutputStream_writeStreamIdx(JNIEnv* env,
		jobject this, jint sidx, jshortArray pcm) {

	jshort * pcmShorts 			= (*env)->GetShortArrayElements(env, pcm, NULL);
	long samples 				= (*env)->GetArrayLength(env, pcm);
	struct output_stream * optr = &output_streams[sidx];
	int channels;
	int i,j;
	int eos = 0;

	if (sidx >= MAX_OUTPUTSTREAMS || sidx < 0 || optr->fh == NULL) {
		JNU_ThrowByName(env, "java/io/IOException", "Invalid Stream Index",
				sidx);
		return;
	}

	channels = optr->channels;

	if (samples != 0) {
		/* data to encode */

		/* expose the buffer to submit data */
		float ** buffer = vorbis_analysis_buffer(&optr->vd, SAMPLE_SIZE*optr->channels);

		/* uninterleave samples */
		for (i = 0; i < samples / channels; i++) {
			for (j = 0; j < channels; j++) {
				buffer[j][i] = pcmShorts[i*channels + j] / 32768.f;
			}
		}

		/* tell the library how much we actually submitted */
		vorbis_analysis_wrote(&optr->vd, i);
	}

	(*env)->ReleaseShortArrayElements(env, pcm, pcmShorts, 0);
	/* vorbis does some data preanalysis, then divvies up blocks for
	 more involved (potentially parallel) processing.  Get a single
	 block for encoding now */
	while (vorbis_analysis_blockout(&optr->vd, &optr->vb) == 1) {

		/* analysis, assume we want to use bitrate management */
		vorbis_analysis(&optr->vb, NULL);
		vorbis_bitrate_addblock(&optr->vb);

		while (vorbis_bitrate_flushpacket(&optr->vd, &optr->op)) {

			/* weld the packet into the bitstream */
			ogg_stream_packetin(&optr->os, &optr->op);

			/* write out pages (if any) */
			while (!eos) {
				int result = ogg_stream_pageout(&optr->os, &optr->og);
				if (result == 0)
					break;
				fwrite(optr->og.header, 1, optr->og.header_len, optr->fh);
				fwrite(optr->og.body, 1, optr->og.body_len, optr->fh);

				/* this could be set above, but for illustrative purposes, I do
				 it here (to show that vorbis does know where the stream ends) */

				if (ogg_page_eos(&optr->og))
					eos = 1;
			}
		}
	}
}
/*
 * Clean up stream info.
 */
void Java_org_ideaheap_io_VorbisFileOutputStream_closeStreamIdx(
		JNIEnv* env, jobject this, jint sidx) {
	struct output_stream * optr = &output_streams[sidx];
	if (sidx >= MAX_OUTPUTSTREAMS || sidx < 0 || optr->fh == NULL) {
		JNU_ThrowByName(env, "java/io/IOException", "Invalid Stream Index", sidx);
		return;
	}
	vorbis_analysis_wrote(&optr->vd, 0);
	ogg_stream_clear(&optr->os);
	vorbis_block_clear(&optr->vb);
	vorbis_dsp_clear(&optr->vd);
	vorbis_comment_clear(&optr->vc);
	vorbis_info_clear(&optr->vi);
	fclose(optr->fh);
	optr->fh = NULL;
}
