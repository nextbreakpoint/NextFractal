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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ASTOrbit extends ASTObject {
	private List<ASTOrbitTrap> traps = new ArrayList<>(); 
	private ASTOrbitBegin begin; 
	private ASTOrbitLoop loop; 
	private ASTOrbitEnd end; 
	private ASTRegion region; 

	public ASTOrbit(Token location, ASTRegion region) {
		super(location);
		this.region = region;
	}

	public List<ASTOrbitTrap> getTraps() {
		return traps;
	}

	public void setTraps(List<ASTOrbitTrap> traps) {
		this.traps = traps;
	}

	public ASTOrbitBegin getBegin() {
		return begin;
	}

	public void setBegin(ASTOrbitBegin begin) {
		this.begin = begin;
	}

	public ASTOrbitLoop getLoop() {
		return loop;
	}

	public void setLoop(ASTOrbitLoop loop) {
		this.loop = loop;
	}

	public ASTOrbitEnd getEnd() {
		return end;
	}

	public void setEnd(ASTOrbitEnd end) {
		this.end = end;
	}

	public ASTRegion getRegion() {
		return region;
	}

	public void addTrap(ASTOrbitTrap trap) {
		if (traps == null) {
			traps = new ArrayList<ASTOrbitTrap>();
		}
		traps.add(trap);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String suffix = "";
		if (region != null) {
			builder.append("contentRegion = ");
			builder.append(region);
			suffix = ",";
		}
		if (begin != null) {
			if (suffix.length() != 0) {
				builder.append(suffix);
			} else {
				suffix = ",";
			}
			builder.append("begin = {");
			builder.append(begin);
			builder.append("}");
		}
		if (loop != null) {
			if (suffix.length() != 0) {
				builder.append(suffix);
			} else {
				suffix = ",";
			}
			builder.append("loop = {");
			builder.append(loop);
			builder.append("}");
		}
		if (end != null) {
			if (suffix.length() != 0) {
				builder.append(suffix);
			} else {
				suffix = ",";
			}
			builder.append("end = {");
			builder.append(end);
			builder.append("}");
		}
		if (suffix.length() != 0) {
			builder.append(suffix);
		} else {
			suffix = ",";
		}
		builder.append("traps = [");
		for (int i = 0; i < traps.size(); i++) {
			ASTOrbitTrap trap = traps.get(i);
			builder.append("{");
			builder.append(trap);
			builder.append("}");
			if (i < traps.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}
}
