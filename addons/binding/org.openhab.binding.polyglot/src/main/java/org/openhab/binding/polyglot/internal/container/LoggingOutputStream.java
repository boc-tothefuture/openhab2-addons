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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 *
 * @author Brian OConnell - Initial contribution
 */
class LoggingOutputStream extends OutputStream {

    private static final String LINE_SEPERATOR = System.lineSeparator();

    private StringBuilder buffer;

    private final Logger logger;
    private final Level defaultLevel;

    private final LogParser logParser;

    public LoggingOutputStream(Level level, Thing thing, Optional<Pattern> logRegex) {
        this.logger = LoggerFactory.getLogger(
                LoggerFactory.getLogger(LoggingOutputStream.class).getName() + "." + thing.getUID().getAsString());
        this.defaultLevel = level;
        this.logParser = new LogParser(logRegex);

        buffer = new StringBuilder();

        logger.debug("Created new LoggingOutputStream: {}", logParser);
    }

    /**
     * Get the log level and potentially mutates the StringBuilder
     *
     * @param logLine logLine to get level and potentially mutate
     * @return Log Level
     */
    private Level getLogLevel(StringBuilder logLine) {

        Level logLevel;

        if (logParser.logRegex.isPresent()) {

            Matcher matcher = logParser.logRegex.get().matcher(logLine);

            if (matcher.matches()) {

                String levelMatch = null;
                if (logParser.patternHasLevel) {
                    levelMatch = matcher.group("level");
                }

                // If a strip is specified, remove that match from the log line
                if (logParser.patternHasStrip) {
                    String stripMatch = matcher.group("strip");
                    if (stripMatch != null) {
                        logLine.delete(logLine.indexOf(stripMatch), stripMatch.length());
                    }
                }

                // If no level match is found, use default level.
                if (logParser.patternHasLevel) {
                    if (levelMatch == null) {
                        logLevel = defaultLevel;
                    } else {
                        try {
                            logLevel = Level.valueOf(levelMatch.trim());
                        } catch (IllegalArgumentException ex) {
                            logger.error("Error determining log level. {} did not match any levels.", levelMatch.trim(),
                                    ex);
                            logLevel = defaultLevel;
                        }
                    }
                } else {
                    logLevel = defaultLevel;
                }
            } else {
                logLevel = defaultLevel;
            }
        } else {
            logLevel = defaultLevel;
        }

        return logLevel;
    }

    @Override
    public void write(int b) {
        buffer.append((char) b);
        if (buffer.lastIndexOf(LINE_SEPERATOR) != -1) {
            buffer.delete(buffer.length() - LINE_SEPERATOR.length(), buffer.length());
            Level level = getLogLevel(buffer);
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

    private static class LogParser {
        private Optional<Pattern> logRegex;
        private final boolean patternHasLevel;
        private final boolean patternHasStrip;

        private final static String LEVEL_GROUP = "level";
        private final static String STRIP_GROUP = "strip";

        private LogParser(Optional<Pattern> logRegex) {
            this.logRegex = logRegex;

            if (logRegex.isPresent()) {
                String pattern = logRegex.get().pattern();
                patternHasLevel = pattern.contains("(?<" + LEVEL_GROUP + ">");
                patternHasStrip = pattern.contains("(?<" + STRIP_GROUP + ">");

            } else {
                patternHasLevel = false;
                patternHasStrip = false;
            }
        }

        @Override
        public String toString() {
            return "LogParser [logRegex=" + logRegex + ", patternHasLevel=" + patternHasLevel + ", patternHasStrip="
                    + patternHasStrip + "]";
        }

    }
}
