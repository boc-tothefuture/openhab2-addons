/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.polyglot.internal.container;

import java.io.OutputStream;

import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 *
 * @author Brian OConnell - Initial contribution
 */
class LoggingOutputStream extends OutputStream {

    // Newline bytes, reversed for easy reverse checking.
    private static final String LINE_SEPERATOR = System.lineSeparator();

    private StringBuilder buffer;

    private final Logger logger;
    private final Level level;

    public LoggingOutputStream(Level level, Thing thing) {
        this.logger = LoggerFactory.getLogger(thing.getUID().getAsString());
        this.level = level;
        buffer = new StringBuilder();
    }

    @Override
    public void write(int b) {
        buffer.append((char) b);
        if (buffer.lastIndexOf(LINE_SEPERATOR) != -1) {
            buffer.delete(buffer.length() - LINE_SEPERATOR.length(), buffer.length());
            String logLine = buffer.toString();
            switch (level) {
                case DEBUG:
                    logger.debug(logLine);
                    break;
                case ERROR:
                    logger.error(logLine);
                    break;
                case INFO:
                    logger.info(logLine);
                    break;
                case TRACE:
                    logger.trace(logLine);
                    break;
                case WARN:
                    logger.warn(logLine);
                    break;
                default:
                    break;
            }
            buffer = new StringBuilder();
        }
    }

}
