package compiler;

public final class TypeUtil {

    private TypeUtil() {
        throw new UnsupportedOperationException();
    }

    public static String asJavaType(String type) {
        return switch (type) {
            case "Int" -> "Integer";
            case "Boolean" -> "Boolean";
            case "String" -> "String";
            case "Unit" -> "Void";
            default -> type;
        };
    }

}
