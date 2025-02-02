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

import java.util.UUID;

public class Constants {

    public static final int COLOR_POWERED = 0x770000ff;

    public static final byte NODE_ID_INVALID = -1;
    public static final byte NODE_ID_INPUT = 0;
    public static final byte NODE_ID_OUTPUT = 1;
    public static final byte NODE_ID_BLUEPRINT = 2;

    public static final UUID UUID_NULL = UUID.fromString("00000000-0000-0000-0000-00000000");
    public static final UUID UUID_NOT = UUID.fromString("00000000-0000-0000-0000-00000001");
    public static final UUID UUID_AND = UUID.fromString("00000000-0000-0000-0000-00000002");

    public static final String ID_CLIPBOARD_GET = "clipboard.get";
    public static final String ID_CLIPBOARD_SET = "clipboard.set";

    public static final String ID_BLUEPRINT_NEW = "blueprint.new";
    public static final String ID_BLUEPRINT_EDIT = "blueprint.edit";
    public static final String ID_BLUEPRINT_CLOSE = "blueprint.close";
    public static final String ID_BLUEPRINT_DELETE = "blueprint.delete";
    public static final String ID_BLUEPRINT_IS_OPEN = "blueprint.is-open";

    private Constants() {
    }
}
