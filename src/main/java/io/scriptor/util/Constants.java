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

    public static int TICKS_PER_FRAME = 1;
    public static int TICKS_PER_CLOCK = 50;
    public static int TICK_THRESHOLD = 10;
    public static int TICK = 0;

    public static final Color COLOR_ZERO_POWER = new Color(0.25f, 0.45f, 0.75f);
    public static final Color COLOR_FULL_POWER = new Color(1.0f, 0.1f, 0.1f);

    public static int getPowerLevel(final int data, final int bitwidth, final float alpha) {
        final var t = (float) data / (float) ((1 << bitwidth) - 1);
        return Color.mix(COLOR_ZERO_POWER, COLOR_FULL_POWER, t).asInt(alpha);
    }

    public static final byte NODE_ID_UNDEFINED = -1;
    public static final byte NODE_ID_INPUT = 0;
    public static final byte NODE_ID_OUTPUT = 1;
    public static final byte NODE_ID_BLUEPRINT = 2;

    public static final UUID UUID_NULL = UUID.fromString("00000000-0000-0000-0000-00000000");
    public static final UUID UUID_NOT = UUID.fromString("00000000-0000-0000-0000-00000001");
    public static final UUID UUID_AND = UUID.fromString("00000000-0000-0000-0000-00000002");
    public static final UUID UUID_CLOCK = UUID.fromString("00000000-0000-0000-0000-00000003");
    public static final UUID UUID_MERGE_2 = UUID.fromString("00000000-0000-0000-0000-00000010");
    public static final UUID UUID_MERGE_4 = UUID.fromString("00000000-0000-0000-0000-00000011");
    public static final UUID UUID_MERGE_8 = UUID.fromString("00000000-0000-0000-0000-00000012");
    public static final UUID UUID_MERGE_16 = UUID.fromString("00000000-0000-0000-0000-00000013");
    public static final UUID UUID_MERGE_32 = UUID.fromString("00000000-0000-0000-0000-00000014");
    public static final UUID UUID_SPLIT_2 = UUID.fromString("00000000-0000-0000-0000-00000020");
    public static final UUID UUID_SPLIT_4 = UUID.fromString("00000000-0000-0000-0000-00000021");
    public static final UUID UUID_SPLIT_8 = UUID.fromString("00000000-0000-0000-0000-00000022");
    public static final UUID UUID_SPLIT_16 = UUID.fromString("00000000-0000-0000-0000-00000023");
    public static final UUID UUID_SPLIT_32 = UUID.fromString("00000000-0000-0000-0000-00000024");

    public static final UUID[] UUID_MERGE = {UUID_MERGE_2, UUID_MERGE_4, UUID_MERGE_8, UUID_MERGE_16, UUID_MERGE_32};
    public static final UUID[] UUID_SPLIT = {UUID_SPLIT_2, UUID_SPLIT_4, UUID_SPLIT_8, UUID_SPLIT_16, UUID_SPLIT_32};

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
