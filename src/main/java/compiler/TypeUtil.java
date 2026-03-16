package compiler;

public final class TypeUtil {

    private TypeUtil() {
        throw new UnsupportedOperationException();
    }

    public static String asJavaType(String type) {
        // NOTE: this must not be primitive types
        //  as they are used in the continuation generic param
        return switch (type) {
            case "Int" -> "Integer";
            case "Boolean" -> "Boolean";
            case "String" -> "String";
            case "Unit" -> "Void";
            default -> type;
        };
    }

}
