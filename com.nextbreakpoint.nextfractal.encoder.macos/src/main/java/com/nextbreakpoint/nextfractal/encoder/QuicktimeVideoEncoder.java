/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.encoder;

import com.nextbreakpoint.ffmpeg4java.macos.AVCodecContext;

import java.lang.foreign.MemorySegment;

import static com.nextbreakpoint.ffmpeg4java.macos.Libffmpeg_1.AV_CODEC_ID_MPEG2VIDEO;
import static com.nextbreakpoint.ffmpeg4java.macos.Libffmpeg_2.FF_PROFILE_MPEG2_HIGH;

/**
 * @author Andrea Medeghini
 */
public class QuicktimeVideoEncoder extends AbstractVideoEncoder {
	public String getSuffix() {
		return ".mov";
	}

	@Override
	public String getId() {
		return "MOV";
	}

	@Override
	public String getName() {
		return "Quicktime";
	}

	@Override
	protected int getCodecID() {
		return AV_CODEC_ID_MPEG2VIDEO();
	}

	@Override
	protected String getFormatName() {
		return "mov";
	}

	@Override
	protected void configureCodecContext(MemorySegment pCodecContext) {
		AVCodecContext.gop_size(pCodecContext, 15);
		AVCodecContext.bit_rate(pCodecContext, 400000);
		AVCodecContext.mb_decision(pCodecContext, 2);
		AVCodecContext.i_quant_factor(pCodecContext, 0.1f);
		AVCodecContext.b_quant_factor(pCodecContext, 0.1f);
		AVCodecContext.profile(pCodecContext, FF_PROFILE_MPEG2_HIGH());
//		AVCodecContext.strict_std_compliance(pCodecContext, AVCodecContext.strict_std_compliance(pCodecContext) | FF_COMPLIANCE_VERY_STRICT());
//	    AVCodecContext.flags(pCodecContext, AVCodecContext.flags(pCodecContext) | AV_CODEC_FLAG_GLOBAL_HEADER());
	}
}
