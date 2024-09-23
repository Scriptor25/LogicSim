package io.scriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

public class ObjectIO {

    public static void write(final Context context, final PrintWriter out, final Object object)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        out.println(object.getClass().getName());
        object.getClass().getMethod("write", Context.class, PrintWriter.class).invoke(object, context, out);
    }

    public static boolean read(final Context context, final BufferedReader in)
            throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final var className = in.readLine();
        if (className == null) return false;
        final var type = ClassLoader.getSystemClassLoader().loadClass(className);
        type.getMethod("read", Context.class, BufferedReader.class).invoke(null, context, in);
        return true;
    }

    private ObjectIO() {
    }
}
