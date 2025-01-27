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
package io.scriptor.instruction;

import io.scriptor.util.RTException;
import org.jetbrains.annotations.NotNull;

public class TypeID {

    public static final byte GET_ATTRIB_INSTRUCTION = 1;
    public static final byte SET_ATTRIB_INSTRUCTION = 2;
    public static final byte GET_REG_INSTRUCTION = 3;
    public static final byte SET_REG_INSTRUCTION = 4;
    public static final byte CALL_INSTRUCTION = 5;
    public static final byte GET_RESULT_INSTRUCTION = 6;
    public static final byte CONST_INSTRUCTION = 7;

    public static @NotNull Class<? extends Instruction> toClass(final byte typeId) {
        return switch (typeId) {
            case GET_ATTRIB_INSTRUCTION -> GetAttribInstruction.class;
            case SET_ATTRIB_INSTRUCTION -> SetAttribInstruction.class;
            case GET_REG_INSTRUCTION -> GetRegInstruction.class;
            case SET_REG_INSTRUCTION -> SetRegInstruction.class;
            case CALL_INSTRUCTION -> CallInstruction.class;
            case GET_RESULT_INSTRUCTION -> GetResultInstruction.class;
            case CONST_INSTRUCTION -> ConstInstruction.class;
            default -> throw new RTException("no class registered for type id '%d'", typeId);
        };
    }

    public static byte fromClass(final @NotNull Class<? extends Instruction> type) {
        if (type == GetAttribInstruction.class)
            return GET_ATTRIB_INSTRUCTION;
        if (type == SetAttribInstruction.class)
            return SET_ATTRIB_INSTRUCTION;
        if (type == GetRegInstruction.class)
            return GET_REG_INSTRUCTION;
        if (type == SetRegInstruction.class)
            return SET_REG_INSTRUCTION;
        if (type == CallInstruction.class)
            return CALL_INSTRUCTION;
        if (type == GetResultInstruction.class)
            return GET_RESULT_INSTRUCTION;
        if (type == ConstInstruction.class)
            return CONST_INSTRUCTION;
        throw new RTException("no type id registered for class '%s'", type);
    }

    private TypeID() {
    }
}
