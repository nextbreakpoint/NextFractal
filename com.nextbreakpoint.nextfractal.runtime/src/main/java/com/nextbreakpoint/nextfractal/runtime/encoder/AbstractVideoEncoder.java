/*
 * NextFractal 2.3.1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2024 Andrea Medeghini
 *
 * This file is part of NextFractal.
 *
 * NextFractal is an application for creating fractals and other graphics artifacts.
 *
 * NextFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NextFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.nextbreakpoint.nextfractal.runtime.encoder;

import com.nextbreakpoint.ffmpeg4java.AVCPBProperties;
import com.nextbreakpoint.ffmpeg4java.AVCodec;
import com.nextbreakpoint.ffmpeg4java.AVCodecContext;
import com.nextbreakpoint.ffmpeg4java.AVCodecParameters;
import com.nextbreakpoint.ffmpeg4java.AVFormatContext;
import com.nextbreakpoint.ffmpeg4java.AVFrame;
import com.nextbreakpoint.ffmpeg4java.AVOutputFormat;
import com.nextbreakpoint.ffmpeg4java.AVPacket;
import com.nextbreakpoint.ffmpeg4java.AVRational;
import com.nextbreakpoint.ffmpeg4java.AVStream;
import com.nextbreakpoint.nextfractal.core.encode.Encoder;
import com.nextbreakpoint.nextfractal.core.encode.EncoderContext;
import com.nextbreakpoint.nextfractal.core.encode.EncoderDelegate;
import com.nextbreakpoint.nextfractal.core.encode.EncoderException;
import com.nextbreakpoint.nextfractal.core.encode.EncoderHandle;
import lombok.extern.java.Log;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.logging.Level;

import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.avcodec_alloc_context3;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.avcodec_close;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.avcodec_open2;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.avcodec_parameters_from_context;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.avcodec_receive_packet;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.avcodec_send_frame;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.sws_freeContext;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.sws_getCachedContext;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg.sws_scale;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.AV_PKT_DATA_CPB_PROPERTIES;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_frame_alloc;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_guess_format;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_packet_alloc;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_packet_rescale_ts;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_packet_unref;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_stream_new_side_data;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_write_frame;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.av_write_trailer;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avcodec_find_encoder;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avcodec_parameters_alloc;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avformat_alloc_context;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avformat_free_context;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avformat_new_stream;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avformat_write_header;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avio_close;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_1.avio_open2;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.AVIO_FLAG_WRITE;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.AVMEDIA_TYPE_VIDEO;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.AV_PIX_FMT_RGB24;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.AV_PIX_FMT_YUV420P;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.C_POINTER;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.SWS_BILINEAR;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.av_free;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.av_image_alloc;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.av_image_fill_arrays;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.av_image_get_buffer_size;
import static com.nextbreakpoint.ffmpeg4java.Libffmpeg_2.av_q2intfloat;
import static java.lang.foreign.MemorySegment.NULL;

/**
 * @author Andrea Medeghini
 */
@Log
public abstract class AbstractVideoEncoder implements Encoder {
	private EncoderDelegate delegate;

	/**
	 * @return
	 */
	public boolean isVideoSupported() {
		return true;
	}

	/**
	 * @see com.nextbreakpoint.nextfractal.core.encode.Encoder#setDelegate(com.nextbreakpoint.nextfractal.core.encode.EncoderDelegate)
	 */
	public void setDelegate(EncoderDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	public EncoderHandle open(EncoderContext context, File path) throws EncoderException {
		return new VideoEncoderHandle(context, path);
	}

	@Override
	public void close(EncoderHandle handle) throws EncoderException {
		((VideoEncoderHandle) handle).close();
	}

	@Override
	public void encode(EncoderHandle handle, int index, int count) throws EncoderException {
		((VideoEncoderHandle) handle).encode(index, count);
	}

	/**
	 * @return
	 */
	protected abstract int getCodecID();

	/**
	 * @return
	 */
	protected abstract String getFormatName();

	/**
	 * @param pCodecContext
	 */
	protected abstract void configureCodecContext(MemorySegment pCodecContext);

	private class VideoEncoderHandle implements EncoderHandle {
		private Arena arena;
		private EncoderContext context;
		private MemorySegment pFormatContext;
		private MemorySegment pCodecContext;
		private MemorySegment pCodecParams;
		private MemorySegment pStream;
		private MemorySegment pCodec;
		private MemorySegment pRGBFrame;
		private MemorySegment pYUVFrame;
		private MemorySegment pSwsContext;
		private MemorySegment pAVIOContext;
		private MemorySegment pPacket;
		private long time;

		public VideoEncoderHandle(EncoderContext context, File path) throws EncoderException {
			this.context = Objects.requireNonNull(context);
			this.arena = Arena.ofShared();

			try {
				if (delegate != null) {
					delegate.didProgressChanged(0f);
				}
				if (AbstractVideoEncoder.log.isLoggable(Level.FINE)) {
					AbstractVideoEncoder.log.fine("Encoding video...");
				}
				time = System.currentTimeMillis();
				final int bytesPerPixel = 3;
				final int fps = context.getFrameRate();
				final int frameWidth = context.getImageWidth();
				final int frameHeight = context.getImageHeight();
				final var pFileName = arena.allocateFrom(path.getAbsolutePath());
				if (pFileName.equals(NULL)) {
					throw new EncoderException("Can't allocate file name");
				}
				final var pFormatName = arena.allocateFrom(getFormatName());
				if (pFormatName.equals(NULL)) {
					throw new EncoderException("Can't allocate format name");
				}
				pFormatContext = avformat_alloc_context();
				if (pFormatContext.equals(NULL)) {
					throw new EncoderException("Can't allocate format context");
				}
				final var pOutputFormat = av_guess_format(NULL, pFileName, NULL);
				if (pOutputFormat.equals(NULL)) {
					throw new EncoderException("Can't find format " + getFormatName());
				}
				AbstractVideoEncoder.log.info("Format is " + AVOutputFormat.long_name(pOutputFormat).getString(0));
				AVFormatContext.oformat(pFormatContext, pOutputFormat);
				final var pTimeBase = arena.allocate(AVRational.layout());
				if (pTimeBase.equals(NULL)) {
					throw new EncoderException("Can't allocate time base");
				}
				AVRational.num(pTimeBase, 1);
				AVRational.den(pTimeBase, fps);
				final var pFrameRate = arena.allocate(AVRational.layout());
				if (pFrameRate.equals(NULL)) {
					throw new EncoderException("Can't allocate frame rate");
				}
				AVRational.num(pFrameRate, fps);
				AVRational.den(pFrameRate, 1);
				AbstractVideoEncoder.log.info("FPS is " + fps);
                pCodec = avcodec_find_encoder(getCodecID());
				if (pCodec.equals(NULL)) {
					throw new EncoderException("Can't find encoder " + getCodecID());
				}
				pStream = avformat_new_stream(pFormatContext, pCodec);
				if (pStream.equals(NULL)) {
					throw new EncoderException("Can't allocate stream");
				}
				if (AVFormatContext.nb_streams(pFormatContext) != 1) {
					throw new EncoderException("Invalid number of streams");
				}
				AVStream.id(pStream, 0);
				pCodecContext = avcodec_alloc_context3(pCodec);
				if (pCodecContext.equals(NULL)) {
					throw new EncoderException("Can't allocate codec context");
				}
				AVCodecContext.codec_id(pCodecContext, AVCodec.id(pCodec));
				AVCodecContext.codec_type(pCodecContext, AVMEDIA_TYPE_VIDEO());
				AVCodecContext.pix_fmt(pCodecContext, AV_PIX_FMT_YUV420P());
				AVCodecContext.width(pCodecContext, frameWidth);
				AVCodecContext.height(pCodecContext, frameHeight);
				AVCodecContext.time_base(pCodecContext, pTimeBase);
				configureCodecContext(pCodecContext);
				pCodecParams = avcodec_parameters_alloc();
				if (pCodecParams.equals(NULL)) {
					throw new EncoderException("Can't allocate codec parameters");
				}
				if (avcodec_parameters_from_context(pCodecParams, pCodecContext) != 0) {
					throw new EncoderException("Can't copy codec parameters");
				}
				AVCodecParameters.codec_id(pCodecParams, AVCodec.id(pCodec));
				AVCodecParameters.codec_type(pCodecParams, AVMEDIA_TYPE_VIDEO());
				AVCodecParameters.width(pCodecParams, frameWidth);
				AVCodecParameters.height(pCodecParams, frameHeight);
				final long bitRate = frameWidth * frameHeight * bytesPerPixel * av_q2intfloat(pFrameRate) * 8L;
				AVCodecParameters.bit_rate(pCodecParams, bitRate);
				AVStream.codecpar(pStream, pCodecParams);
				AVStream.time_base(pStream, pTimeBase);
				AVStream.avg_frame_rate(pStream, pFrameRate);
				final var pProperties = av_stream_new_side_data(pStream, AV_PKT_DATA_CPB_PROPERTIES(), AVCPBProperties.sizeof());
				AVCPBProperties.buffer_size(pProperties, frameWidth * frameHeight * bytesPerPixel * 2L);
//				final var pProperties = av_stream_get_side_data(pStream, AV_PKT_DATA_CPB_PROPERTIES(), NULL);
				AbstractVideoEncoder.log.info("Buffer size " + AVCPBProperties.buffer_size(pProperties));
				if (avcodec_open2(pCodecContext, pCodec, NULL) != 0) {
					throw new EncoderException("Can't open encoder");
				}
				AbstractVideoEncoder.log.info("Codec name " + AVCodec.name(pCodec).getString(0));
				final var ppOutputAVIOCtx = arena.allocate(C_POINTER);
				if (avio_open2(ppOutputAVIOCtx, pFileName, AVIO_FLAG_WRITE(), NULL, NULL) < 0) {
					throw new EncoderException("Can't open IO context");
				}
				pAVIOContext = ppOutputAVIOCtx.get(C_POINTER, 0);
				if (pAVIOContext.equals(NULL)) {
					throw new EncoderException("IO context is null");
				}
				AVFormatContext.pb(pFormatContext, pAVIOContext);
				pSwsContext = sws_getCachedContext(NULL, frameWidth, frameHeight, AV_PIX_FMT_RGB24(), frameWidth, frameHeight, AV_PIX_FMT_YUV420P(), SWS_BILINEAR(), NULL, NULL, NULL);
				if (pSwsContext.equals(NULL)) {
					throw new EncoderException("Can't create scale context");
				}
				pRGBFrame = av_frame_alloc();
				pYUVFrame = av_frame_alloc();
				if (pRGBFrame.equals(NULL)) {
					throw new EncoderException("Can't allocate RGB frame");
				}
				if (pYUVFrame.equals(NULL)) {
					throw new EncoderException("Can't allocate YUV frame");
				}
				AVFrame.width(pRGBFrame, frameWidth);
				AVFrame.height(pRGBFrame, frameHeight);
				AVFrame.format(pRGBFrame, AV_PIX_FMT_RGB24());
				AVFrame.width(pYUVFrame, frameWidth);
				AVFrame.height(pYUVFrame, frameHeight);
				AVFrame.format(pYUVFrame, AV_PIX_FMT_YUV420P());
				av_image_alloc(AVFrame.data(pRGBFrame), AVFrame.linesize(pRGBFrame), frameWidth, frameHeight, AV_PIX_FMT_RGB24(), 1);
				av_image_alloc(AVFrame.data(pYUVFrame), AVFrame.linesize(pYUVFrame), frameWidth, frameHeight, AV_PIX_FMT_YUV420P(), 1);
				final int rgbByteSize = av_image_get_buffer_size(AV_PIX_FMT_RGB24(), frameWidth, frameHeight, 1);
				final int yuvByteSize = av_image_get_buffer_size(AV_PIX_FMT_YUV420P(), frameWidth, frameHeight, 1);
				final var pRGBBuffer = arena.allocate(rgbByteSize);
				final var pYUVBuffer = arena.allocate(yuvByteSize);
				if (pRGBBuffer.equals(NULL)) {
					throw new EncoderException("Can't allocate RGB buffer");
				}
				if (pYUVBuffer.equals(NULL)) {
					throw new EncoderException("Can't allocate YUB buffer");
				}
				av_image_fill_arrays(AVFrame.data(pRGBFrame), AVFrame.linesize(pRGBFrame), pRGBBuffer, AV_PIX_FMT_RGB24(), frameWidth, frameHeight, 1);
				av_image_fill_arrays(AVFrame.data(pYUVFrame), AVFrame.linesize(pYUVFrame), pYUVBuffer, AV_PIX_FMT_YUV420P(), frameWidth, frameHeight, 1);
				pPacket = av_packet_alloc();
				if (pPacket.equals(NULL)) {
					throw new EncoderException("Can't allocate packet");
				}
				AVPacket.stream_index(pPacket, AVStream.index(pStream));
				avformat_write_header(pFormatContext, NULL);
			} catch (EncoderException e) {
				dispose();
				log.log(Level.WARNING, "Failed to encode video", e);
				throw e;
			} catch (Exception e) {
				dispose();
				log.log(Level.WARNING, "Failed to encode video", e);
				throw new EncoderException(e);
			}
		}

		public void encode(int frameIndex, int frameCount) throws EncoderException {
			try {
				if (!pPacket.equals(NULL)) {
					final var pOutputData = AVFrame.data(pRGBFrame);
					final int rgbByteSize = av_image_get_buffer_size(AV_PIX_FMT_RGB24(), context.getImageWidth(), context.getImageHeight(), 1);
					final byte[] data = context.getPixelsAsByteArray(0, 0, 0, context.getImageWidth(), context.getImageHeight(), 3, true);
					MemorySegment.copy(MemorySegment.ofArray(data), 0, pOutputData.get(C_POINTER, 0), 0, rgbByteSize);
					sws_scale(pSwsContext, AVFrame.data(pRGBFrame), AVFrame.linesize(pRGBFrame), 0, context.getImageHeight(), AVFrame.data(pYUVFrame), AVFrame.linesize(pYUVFrame));
					for (int count = 0; count < frameCount; count++) {
						int lastFrame = frameIndex + count;
						if (delegate != null) {
							delegate.didProgressChanged(lastFrame / (frameCount - 1f));
						}
						if (avcodec_send_frame(pCodecContext, pYUVFrame) == 0) {
							while (avcodec_receive_packet(pCodecContext, pPacket) == 0) {
								av_packet_rescale_ts(pPacket, AVCodecContext.time_base(pCodecContext), AVStream.time_base(pStream));
								av_write_frame(pFormatContext, pPacket);
								log.fine("1) pts " + AVPacket.pts(pPacket) + ", dts " + AVPacket.dts(pPacket));
								Thread.yield();
							}
							av_write_frame(pFormatContext, NULL);
						}
						if (delegate != null && delegate.isInterrupted()) {
							break;
						}
						Thread.yield();
					}
				}
			} catch (Exception e) {
				dispose();
				log.log(Level.WARNING, "Failed to encode video", e);
				throw new EncoderException(e);
			}
		}

		public void close() throws EncoderException {
			try {
				if (!pPacket.equals(NULL)) {
					if (avcodec_send_frame(pCodecContext, NULL) == 0) {
						while (avcodec_receive_packet(pCodecContext, pPacket) == 0) {
							av_packet_rescale_ts(pPacket, AVCodecContext.time_base(pCodecContext), AVStream.time_base(pStream));
							av_write_frame(pFormatContext, pPacket);
							log.fine("2) pts " + AVPacket.pts(pPacket) + ", dts " + AVPacket.dts(pPacket));
							Thread.yield();
						}
						av_write_frame(pFormatContext, NULL);
					}
					av_write_trailer(pFormatContext);
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "Failed to encode video", e);
				throw new EncoderException(e);
			} finally {
				dispose();
			}
			if (delegate == null || !delegate.isInterrupted()) {
				time = System.currentTimeMillis() - time;
				if (AbstractVideoEncoder.log.isLoggable(Level.INFO)) {
					AbstractVideoEncoder.log.info("Video exported: elapsed time " + String.format("%3.2f", time / 1000.0d) + "s");
				}
				if (delegate != null) {
					delegate.didProgressChanged(1f);
				}
			}
		}

		private void dispose() throws EncoderException {
			try {
				if (!pPacket.equals(NULL)) {
					av_packet_unref(pPacket);
					pPacket = NULL;
				}
				if (!pAVIOContext.equals(NULL)) {
					avio_close(pAVIOContext);
					pAVIOContext = NULL;
				}
				if (!pCodecContext.equals(NULL)) {
					avcodec_close(pCodecContext);
					pCodecContext = NULL;
				}
				if (!pSwsContext.equals(NULL)) {
					sws_freeContext(pSwsContext);
					pSwsContext = NULL;
				}
				if (!pRGBFrame.equals(NULL)) {
					av_free(pRGBFrame);
					pRGBFrame = NULL;
				}
				if (!pYUVFrame.equals(NULL)) {
					av_free(pYUVFrame);
					pYUVFrame = NULL;
				}
				if (!pFormatContext.equals(NULL)) {
					avformat_free_context(pFormatContext);
					pFormatContext = NULL;
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "Failed to encode video", e);
				throw new EncoderException(e);
			} finally {
				if (arena != null) {
					arena.close();
				}
			}
		}
	}
}
