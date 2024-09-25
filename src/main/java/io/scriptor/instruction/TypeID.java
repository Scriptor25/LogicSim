package io.scriptor.instruction;

public class TypeID {

    public static final int GET_ATTRIB_INSTRUCTION = 1;
    public static final int SET_ATTRIB_INSTRUCTION = 2;
    public static final int GET_REG_INSTRUCTION = 3;
    public static final int SET_REG_INSTRUCTION = 4;
    public static final int CALL_INSTRUCTION = 5;
    public static final int GET_RESULT_INSTRUCTION = 6;
    public static final int CONST_INSTRUCTION = 7;

    public static Class<? extends Instruction> toClass(final int typeId) {
        return switch (typeId) {
            case GET_ATTRIB_INSTRUCTION -> GetAttribInstruction.class;
            case SET_ATTRIB_INSTRUCTION -> SetAttribInstruction.class;
            case GET_REG_INSTRUCTION -> GetRegInstruction.class;
            case SET_REG_INSTRUCTION -> SetRegInstruction.class;
            case CALL_INSTRUCTION -> CallInstruction.class;
            case GET_RESULT_INSTRUCTION -> GetResultInstruction.class;
            case CONST_INSTRUCTION -> ConstInstruction.class;
            default -> throw new IllegalStateException();
        };
    }

    public static int fromClass(final Class<? extends Instruction> type) {
        if (type == GetAttribInstruction.class) return GET_ATTRIB_INSTRUCTION;
        if (type == SetAttribInstruction.class) return SET_ATTRIB_INSTRUCTION;
        if (type == GetRegInstruction.class) return GET_REG_INSTRUCTION;
        if (type == SetRegInstruction.class) return SET_REG_INSTRUCTION;
        if (type == CallInstruction.class) return CALL_INSTRUCTION;
        if (type == GetResultInstruction.class) return GET_RESULT_INSTRUCTION;
        if (type == ConstInstruction.class) return CONST_INSTRUCTION;
        throw new IllegalStateException();
    }

    private TypeID() {
    }
}
