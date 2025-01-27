/*
 * This file is part of https://github.com/Scriptor25/LogicSim
 *
 * Copyright (C) 2025  Felix Schreiber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package io.scriptor.util;

import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

    private static Logger logger;

    public static Logger getLogger() {
        if (logger != null)
            return logger;

        logger = Logger.getLogger("io.scriptor");

        final var handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(final LogRecord rec) {
                return "[%s][%s]%n%s%n".formatted(
                        new Date(rec.getMillis()),
                        rec.getLevel(),
                        rec.getMessage());
            }
        });

        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        return logger;
    }

    private Log() {
    }
}
