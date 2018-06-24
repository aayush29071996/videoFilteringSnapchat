package com.example.aayush.videoplayer;

/**
 * Created by aayush on 13/6/17.
 */

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by aayush on 7/6/17.
 */
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

class DecodeEncode {
    private boolean VERBOSE = false;
    private static final String TAG = "DecodeEncode";
    private String filename = "";

    // where to find files (note: requires WRITE_EXTERNAL_STORAGE permission)
    private static final File FILES_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final String INPUT_FILE = "";
    private static final int MAX_FRAMES = 10;       // stop extracting after this many

    private MediaCodec codecEncode = null;
    private MediaFormat mediaFormat;
    private String mimeType = "video/avc";
    private int encWd = 320;
    private int encHt = 240;
    private MediaCodec.BufferInfo bufferInfo = null;
    private MediaMuxer mMuxer = null;
    private int mTrackIndex = -1;
    int frameSize = 1;
    byte[] frameData = null;

    DecodeEncode(String inpFilename) {
        filename = inpFilename;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void initializeEnc() {
        {
            mediaFormat = MediaFormat.createVideoFormat(mimeType, encWd, encHt);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 10);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);

            if (codecEncode == null) {
                try {
                    codecEncode = MediaCodec.createEncoderByType(mimeType);
                    codecEncode.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                    codecEncode.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            bufferInfo = new MediaCodec.BufferInfo();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    ArrayList<byte[]> extractMpegFrames(ArrayList<byte[]> allFrameData) throws IOException {
        MediaCodec decoder = null;
        MediaExtractor extractor = null;
        int saveWidth = 640;
        int saveHeight = 480;


        int trackIndex;
        try {
            //File inputFile = new File(FILES_DIR, INPUT_FILE);   // must be an absolute path
            File inputFile = new File(filename);   // must be an absolute path

            // The MediaExtractor error messages aren't very useful.  Check to see if the input
            // file exists so we can throw a better one if it's not there.
            if (!inputFile.canRead()) {
                throw new FileNotFoundException("Unable to read " + inputFile);
            }

            extractor = new MediaExtractor();
            extractor.setDataSource(inputFile.toString());
            trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + inputFile);
            }
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);
            if (VERBOSE) {
                Log.d(TAG, "Video size is " + format.getInteger(MediaFormat.KEY_WIDTH) + "x" +
                        format.getInteger(MediaFormat.KEY_HEIGHT));
            }

            // Create a MediaCodec decoder, and configure it with the MediaFormat from the
            // extractor.  It's very important to use the format from the extractor because
            // it contains a copy of the CSD-0/CSD-1 codec-specific data chunks.
            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, null, null, 0);
            decoder.start();

            return doExtract(extractor, trackIndex, decoder,allFrameData);


        } finally {
            // release everything we grabbed
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }

            if (codecEncode != null) {
                codecEncode.stop();
                codecEncode.release();
                codecEncode = null;
            }
            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
            }
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }

        return -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    ArrayList<byte[]> doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,ArrayList<byte[]>allFrameData) throws IOException {
        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int inputChunk = 0;
        int decodeCount = 0;
        long frameSaveTime = 0;
        int decodedFrameWidth =0;
        int decodedFrameHeight =0;

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            if (VERBOSE) Log.d(TAG, "loop");

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    int chunkSize = extractor.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor.getSampleTrackIndex() != trackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                        }
                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        extractor.advance();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }

            if (!outputDone) {
                int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder.getOutputFormat();
                    decodedFrameWidth = newFormat.getInteger(MediaFormat.KEY_WIDTH);
                    decodedFrameHeight = newFormat.getInteger(MediaFormat.KEY_HEIGHT);
                    frameSize = (decodedFrameWidth * decodedFrameHeight * 3) >> 1;
                    frameData = new byte[frameSize];
                    encWd = decodedFrameWidth;
                    encHt = decodedFrameHeight;
                    //initializeEnc();
                    //mMuxer = new MediaMuxer("/sdcard/Download/encodedOut.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    if (VERBOSE) Log.d(TAG, "unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + info.size + ")");
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
                        outputDone = true;
                    }

                    ByteBuffer buffer = null;
                    if(Build.VERSION.SDK_INT >= 21) {
                        buffer = decoder.getOutputBuffer(decoderStatus);
                    } else {
                        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
                        buffer = outputBuffers[decoderStatus];
                    }

                    buffer.position(info.offset);
                    buffer.limit(info.offset + info.size);
                    if((info.size <= frameSize) && (info.size > 0)) {
                        byte[] tt = new byte[frameSize];

                        buffer.get(tt, 0, frameSize);

                        //ArrayList<byte[]> allframeData1 = new ArrayList<>(100);
                        allFrameData.add(tt);



                        //encodeFrame(decodeCount, allFrameData.get(decodeCount));
                        decodeCount++;
                    }

                    boolean doRender = (info.size != 0);

                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                    // that the texture will be available before the call returns, so we
                    // need to wait for the onFrameAvailable callback to fire.
                    decoder.releaseOutputBuffer(decoderStatus, doRender);
                }
            }
        }

        int numSaved = (MAX_FRAMES < decodeCount) ? MAX_FRAMES : decodeCount;
        Log.d(TAG, "Saving " + numSaved + " frames took " +
                (frameSaveTime / numSaved / 1000) + " us per frame");

        return allFrameData ;
    }

    int getDecodedWidth() {
        return encWd;
    }

    int getDecodedHeight() {
        return encHt;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    void encodeAllFrames(ArrayList<byte[]>allFrameData, int encoderWd, int encoderHt){

        encWd = encoderWd;
        encHt = encoderHt;

        try {
            initializeEnc();
            mMuxer = new MediaMuxer("/sdcard/LightMetrics/encodedOut.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            for (int i = 0; i < allFrameData.size(); i++) {
                encodeFrame(i, allFrameData.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (codecEncode != null) {
            codecEncode.stop();
            codecEncode.release();
            codecEncode = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void encodeFrame(int decodeCount, byte[] frameData) {

        long presentationTime = (((long) decodeCount) * 1000000L / ((long) 10));

        int inputBufferIndex = codecEncode.dequeueInputBuffer(10000); //10000
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = null;
            if (Build.VERSION.SDK_INT >= 21) {
                inputBuffer = codecEncode.getInputBuffer(inputBufferIndex);
            } else {
                ByteBuffer[] inpBuffers = codecEncode.getInputBuffers();
                inputBuffer = inpBuffers[inputBufferIndex];
            }

            inputBuffer.clear();

            inputBuffer.put(frameData);
            codecEncode.queueInputBuffer(inputBufferIndex, 0, frameData.length, presentationTime, 0);
        }

        int outputBufferIndex = codecEncode.dequeueOutputBuffer(bufferInfo, 100);

        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat newFormat = codecEncode.getOutputFormat();
            mTrackIndex = mMuxer.addTrack(newFormat);
            mMuxer.start();
        }

        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = null;
            if(Build.VERSION.SDK_INT >= 21) {
                outputBuffer = codecEncode.getOutputBuffer(outputBufferIndex);
            } else {
                ByteBuffer[] outBuffers = codecEncode.getOutputBuffers();
                outputBuffer = outBuffers[outputBufferIndex];
            }

            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
            //bufferInfo.set(0, bufferInfo.size, presentationTime, MediaCodec.BUFFER_FLAG_SYNC_FRAME);

            mMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);

            codecEncode.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = codecEncode.dequeueOutputBuffer(bufferInfo, 100);
        }

    }
}
