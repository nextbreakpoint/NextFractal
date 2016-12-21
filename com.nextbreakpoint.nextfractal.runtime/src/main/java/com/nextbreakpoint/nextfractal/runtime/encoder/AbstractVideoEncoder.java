/*
 * NextFractal 2.0.0
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2017 Andrea Medeghini
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
import com.nextbreakpoint.ffmpeg4java.AVCodecID;
import com.nextbreakpoint.ffmpeg4java.AVCodecParameters;
import com.nextbreakpoint.ffmpeg4java.AVFormatContext;
import com.nextbreakpoint.ffmpeg4java.AVFrame;
import com.nextbreakpoint.ffmpeg4java.AVIOContext;
import com.nextbreakpoint.ffmpeg4java.AVMediaType;
import com.nextbreakpoint.ffmpeg4java.AVOutputFormat;
import com.nextbreakpoint.ffmpeg4java.AVPacket;
import com.nextbreakpoint.ffmpeg4java.AVPacketSideDataType;
import com.nextbreakpoint.ffmpeg4java.AVPixelFormat;
import com.nextbreakpoint.ffmpeg4java.AVRational;
import com.nextbreakpoint.ffmpeg4java.AVStream;
import com.nextbreakpoint.ffmpeg4java.FFmpeg4Java;
import com.nextbreakpoint.ffmpeg4java.SWIGTYPE_p_SwsContext;
import com.nextbreakpoint.ffmpeg4java.SWIGTYPE_p_p_AVIOContext;
import com.nextbreakpoint.ffmpeg4java.SWIGTYPE_p_p_void;
import com.nextbreakpoint.ffmpeg4java.SWIGTYPE_p_uint8_t;
import com.nextbreakpoint.nextfractal.core.encoder.Encoder;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderContext;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderDelegate;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderException;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderHandle;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andrea Medeghini
 */
public abstract class AbstractVideoEncoder implements Encoder {
	private static final Logger logger = Logger.getLogger(AbstractVideoEncoder.class.getName());

	private EncoderDelegate delegate;

	static {
		FFmpeg4Java.av_register_all();
		FFmpeg4Java.avcodec_register_all();
	}

	/**
	 * @see com.nextbreakpoint.nextfractal.core.encoder.Encoder#setDelegate(com.nextbreakpoint.nextfractal.core.encoder.EncoderDelegate)
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
	protected abstract String getFormatName();

	private class VideoEncoderHandle implements EncoderHandle {
		private EncoderContext context;
		private AVFormatContext format_context;
		private AVOutputFormat output_format;
		private AVCodecContext codec_context;
		private AVRational time_base;
		private AVStream stream;
		private AVCodec codec;
		private AVFrame rgb_frame;
		private AVFrame yuv_frame;
		private SWIGTYPE_p_uint8_t rgb_bit_buffer;
		private SWIGTYPE_p_uint8_t yuv_bit_buffer;
		private SWIGTYPE_p_SwsContext sws_context;
		private AVPacket packet;
		private long time;

		public VideoEncoderHandle(EncoderContext context, File path) throws EncoderException {
			this.context = Objects.requireNonNull(context);
			try {
				if (delegate != null) {
					delegate.didProgressChanged(0f);
				}
				if (AbstractVideoEncoder.logger.isLoggable(Level.FINE)) {
					AbstractVideoEncoder.logger.fine("Start encoding...");
				}
				time = System.currentTimeMillis();
				int fps = context.getFrameRate();
				int frame_width = context.getImageWidth();
				int frame_height = context.getImageHeight();
				format_context = FFmpeg4Java.avformat_alloc_context();
				output_format = FFmpeg4Java.av_guess_format(getFormatName(), null, null);
				if (format_context == null || output_format == null || output_format.getVideo_codec().equals(AVCodecID.AV_CODEC_ID_NONE)) {
					throw new EncoderException("Cannot find codec");
				}
				AbstractVideoEncoder.logger.fine("Format is " + output_format.getLong_name());
				format_context.setOformat(output_format);
				time_base = new AVRational();
				time_base.setNum(1);
				time_base.setDen(fps);
				codec = FFmpeg4Java.avcodec_find_encoder(output_format.getVideo_codec());
				codec_context = FFmpeg4Java.avcodec_alloc_context3(codec);
				if (codec == null || codec_context == null) {
					throw new EncoderException("Cannot allocate codec");
				}
				codec_context.setCodec_id(output_format.getVideo_codec());
				codec_context.setCodec_type(AVMediaType.AVMEDIA_TYPE_VIDEO);
				codec_context.setPix_fmt(AVPixelFormat.AV_PIX_FMT_YUV420P);
				codec_context.setWidth(frame_width);
				codec_context.setHeight(frame_height);
				codec_context.setTime_base(time_base);
				codec_context.setBit_rate(100 * 1024);
//				codec_context.setGop_size(12);
//				codec_context.setMb_decision(2);
//				codec_context.setMax_b_frames(4);
				codec_context.setB_quant_factor(0.1f);
				codec_context.setI_quant_factor(0.1f);
				codec_context.setProfile(FFmpeg4Java.FF_PROFILE_MPEG2_HIGH);
				stream = FFmpeg4Java.avformat_new_stream(format_context, codec);
				if (stream == null || format_context.getNb_streams() != 1) {
					throw new EncoderException("Cannot allocate stream");
				}
				AVCodecParameters params = new AVCodecParameters();
				params.setCodec_id(output_format.getVideo_codec());
				params.setCodec_type(AVMediaType.AVMEDIA_TYPE_VIDEO);
				params.setWidth(frame_width);
				params.setHeight(frame_height);
				stream.setTime_base(time_base);
				stream.setCodecpar(params);
				SWIGTYPE_p_uint8_t side_data = FFmpeg4Java.av_stream_new_side_data(stream, AVPacketSideDataType.AV_PKT_DATA_CPB_PROPERTIES, new AVCPBProperties().size_of());
				AVCPBProperties props = AVCPBProperties.asTypePointer(SWIGTYPE_p_uint8_t.asVoidPointer(side_data));
				props.setBuffer_size(frame_width * frame_height * 3 * 2);
				side_data = FFmpeg4Java.av_stream_get_side_data(stream, AVPacketSideDataType.AV_PKT_DATA_CPB_PROPERTIES, null);
				props = AVCPBProperties.asTypePointer(SWIGTYPE_p_uint8_t.asVoidPointer(side_data));
				AbstractVideoEncoder.logger.info("Buffer size " + props.getBuffer_size());
				if (FFmpeg4Java.avcodec_open2(codec_context, codec, null) != 0) {
					throw new EncoderException("Cannot open codec");
				}
				AbstractVideoEncoder.logger.info("Codec is " + codec.getName());
				SWIGTYPE_p_p_void void_p_p = FFmpeg4Java.swig_from_p_to_p_p(AVIOContext.asVoidPointer(format_context.getPb()));
				SWIGTYPE_p_p_AVIOContext avio_context_p_p = SWIGTYPE_p_p_AVIOContext.asTypePointer(SWIGTYPE_p_p_void.asVoidPointer(void_p_p));
				if (FFmpeg4Java.avio_open2(avio_context_p_p, path.getAbsolutePath(), FFmpeg4Java.AVIO_FLAG_WRITE, null, null) < 0) {
					throw new EncoderException("Cannot create codec io");
				}
				format_context.setPb(AVIOContext.asTypePointer(FFmpeg4Java.swig_from_p_p_to_p(SWIGTYPE_p_p_void.asTypePointer(SWIGTYPE_p_p_AVIOContext.asVoidPointer(avio_context_p_p)))));
				sws_context = FFmpeg4Java.sws_getCachedContext(null, codec_context.getWidth(), codec_context.getHeight(), AVPixelFormat.AV_PIX_FMT_RGB24, codec_context.getWidth(), codec_context.getHeight(), AVPixelFormat.AV_PIX_FMT_YUV420P, FFmpeg4Java.SWS_BILINEAR, null, null, null);
				if (sws_context == null) {
					throw new EncoderException("Cannot create rescale context");
				}
				rgb_frame = FFmpeg4Java.av_frame_alloc();
				yuv_frame = FFmpeg4Java.av_frame_alloc();
				if (rgb_frame == null || yuv_frame == null) {
					throw new EncoderException("Cannot allocate frame buffers");
				}
				rgb_frame.setWidth(codec_context.getWidth());
				rgb_frame.setHeight(codec_context.getHeight());
				rgb_frame.setFormat(AVPixelFormat.AV_PIX_FMT_RGB24.swigValue());
				yuv_frame.setWidth(codec_context.getWidth());
				yuv_frame.setHeight(codec_context.getHeight());
				yuv_frame.setFormat(AVPixelFormat.AV_PIX_FMT_YUV420P.swigValue());
				FFmpeg4Java.av_image_alloc(rgb_frame.getData(), rgb_frame.getLinesize(), codec_context.getWidth(), codec_context.getHeight(), AVPixelFormat.AV_PIX_FMT_RGB24, 1);
				FFmpeg4Java.av_image_alloc(yuv_frame.getData(), yuv_frame.getLinesize(), codec_context.getWidth(), codec_context.getHeight(), AVPixelFormat.AV_PIX_FMT_YUV420P, 1);
				int rgb_bit_buffer_size = FFmpeg4Java.av_image_get_buffer_size(AVPixelFormat.AV_PIX_FMT_RGB24, codec_context.getWidth(), codec_context.getHeight(), 1);
				int yuv_bit_buffer_size = FFmpeg4Java.av_image_get_buffer_size(AVPixelFormat.AV_PIX_FMT_YUV420P, codec_context.getWidth(), codec_context.getHeight(), 1);
				rgb_bit_buffer = SWIGTYPE_p_uint8_t.asTypePointer(FFmpeg4Java.av_mallocz(rgb_bit_buffer_size));
				yuv_bit_buffer = SWIGTYPE_p_uint8_t.asTypePointer(FFmpeg4Java.av_mallocz(yuv_bit_buffer_size));
				if (rgb_bit_buffer == null || yuv_bit_buffer == null) {
					throw new EncoderException("Cannot allocate bit buffers");
				}
				FFmpeg4Java.av_image_fill_arrays(rgb_frame.getData(), rgb_frame.getLinesize(), rgb_bit_buffer, AVPixelFormat.AV_PIX_FMT_RGB24, codec_context.getWidth(), codec_context.getHeight(), 1);
				FFmpeg4Java.av_image_fill_arrays(yuv_frame.getData(), yuv_frame.getLinesize(), yuv_bit_buffer, AVPixelFormat.AV_PIX_FMT_YUV420P, codec_context.getWidth(), codec_context.getHeight(), 1);
				packet = FFmpeg4Java.av_packet_alloc();
				if (packet == null) {
					throw new EncoderException("Cannot allocate packet");
				}
				packet.setStream_index(stream.getIndex());
				FFmpeg4Java.avformat_write_header(format_context, null);
			} catch (EncoderException e) {
				dispose();
				throw e;
			} catch (Exception e) {
				dispose();
				throw new EncoderException(e);
			}
		}

		public void encode(int frame_index, int frame_count) throws EncoderException {
			try {
				if (packet != null) {
					byte[] data = context.getPixelsAsByteArray(0, 0, 0, context.getImageWidth(), context.getImageHeight(), 3);
					FFmpeg4Java.swig_set_bytes(rgb_bit_buffer, data);
					FFmpeg4Java.sws_scale(sws_context, rgb_frame.getData(), rgb_frame.getLinesize(), 0, codec_context.getHeight(), yuv_frame.getData(), yuv_frame.getLinesize());
					for (int loop_count = 0; loop_count < frame_count; loop_count++) {
						int last_frame = frame_index + loop_count;
						if (delegate != null) {
							delegate.didProgressChanged(last_frame / (frame_count - 1));
						}
						if (FFmpeg4Java.avcodec_send_frame(codec_context, yuv_frame) == 0) {
							while (FFmpeg4Java.avcodec_receive_packet(codec_context, packet) == 0) {
								FFmpeg4Java.av_packet_rescale_ts(packet, codec_context.getTime_base(), stream.getTime_base());
								FFmpeg4Java.av_write_frame(format_context, packet);
								Thread.yield();
							}
						}
						if (delegate != null && delegate.isInterrupted()) {
							break;
						}
						Thread.yield();
					}
				}
			} catch (Exception e) {
				dispose();
				throw new EncoderException(e);
			}
		}

		public void close() throws EncoderException {
			try {
				if (packet != null) {
					if (FFmpeg4Java.avcodec_send_frame(codec_context, null) == 0) {
						while (FFmpeg4Java.avcodec_receive_packet(codec_context, packet) == 0) {
							FFmpeg4Java.av_packet_rescale_ts(packet, codec_context.getTime_base(), stream.getTime_base());
							FFmpeg4Java.av_write_frame(format_context, packet);
							Thread.yield();
						}
					}
					FFmpeg4Java.av_write_trailer(format_context);
				}
			} finally {
				dispose();
			}
			if (delegate == null || !delegate.isInterrupted()) {
				time = System.currentTimeMillis() - time;
				if (AbstractVideoEncoder.logger.isLoggable(Level.INFO)) {
					AbstractVideoEncoder.logger.info("Video exported: elapsed time " + String.format("%3.2f", time / 1000.0d) + "s");
				}
				if (delegate != null) {
					delegate.didProgressChanged(1f);
				}
			}
		}

		private void dispose() {
			if (packet != null) {
				FFmpeg4Java.av_freep(AVPacket.asVoidPointer(packet));
				packet.delete();
				packet = null;
			}
			if (sws_context != null) {
				FFmpeg4Java.sws_freeContext(sws_context);
				sws_context = null;
				if (format_context != null) {
					FFmpeg4Java.avio_close(format_context.getPb());
				}
				if (codec_context != null) {
					FFmpeg4Java.avcodec_close(codec_context);
				}
			}
			if (rgb_frame != null) {
				FFmpeg4Java.av_freep(AVFrame.asVoidPointer(rgb_frame));
				rgb_frame.delete();
				rgb_frame = null;
			}
			if (yuv_frame != null) {
				FFmpeg4Java.av_freep(AVFrame.asVoidPointer(yuv_frame));
				yuv_frame.delete();
				yuv_frame = null;
			}
			if (codec != null) {
				codec.delete();
				codec = null;
			}
			if (stream != null) {
				stream.delete();
				stream = null;
			}
			if (codec_context != null) {
				codec_context.delete();
				codec_context = null;
			}
			if (output_format != null) {
				output_format.delete();
				output_format = null;
			}
			if (format_context != null) {
				format_context.delete();
				format_context = null;
			}
		}
	}
}