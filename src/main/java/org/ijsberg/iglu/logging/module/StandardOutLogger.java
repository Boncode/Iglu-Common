/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.logging.module;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.logging.Logger;

/**
 * Created by Jeroen on 24-2-14.
 */
public class StandardOutLogger implements Logger {

	private Level level = Level.TRACE;

	public StandardOutLogger() {
	}

	public StandardOutLogger(Level level) {
		this.level = level;
	}

	@Override
	public void log(LogEntry entry) {
		if(entry.getLevel().ordinal() >= level.ordinal()) {
			System.out.println(entry.toString());
		}
	}

    @Override
	public void addAppender(Logger appender) {

	}

	@Override
	public void removeAppender(Logger appender) {

	}
}
