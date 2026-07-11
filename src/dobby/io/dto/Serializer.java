package dobby.io.dto;

import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.util.json.NewJson;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RegisterFor(ISerializer.class)
public class Serializer implements ISerializer {
    private static final Logger LOGGER = new Logger(Serializer.class);

    @Override
    public NewJson serialize(Object object) {
        if (object == null) {
            return null;
        }

        final Field[] fields = object.getClass().getDeclaredFields();
        final NewJson json = new NewJson();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                final Object value = field.get(object);
                if (value == null) {
                    continue;
                }

                switch (value) {
                    case String s -> json.setString(field.getName(), s);
                    case Integer i -> json.setInt(field.getName(), i);
                    case Double d -> json.setFloat(field.getName(), d);
                    case Float f -> json.setFloat(field.getName(), f);
                    case Boolean b -> json.setBoolean(field.getName(), b);
                    case List<?> l -> serializeList(field.getName(), l, json);
                    case UUID u -> json.setString(field.getName(), u.toString());
                    default -> json.setJson(field.getName(), serialize(value));
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Error occurred while serializing object");
                LOGGER.trace(e);
            }
        }

        return json;
    }

    @Override
    public NewJson getJsonType(Object object) {
        if (object == null) {
            return null;
        }

        final Field[] fields = object.getClass().getDeclaredFields();
        final NewJson json = new NewJson();
        for (Field field : fields) {
            try {
                field.setAccessible(true);

                switch (field.getType().getSimpleName()) {
                    case "String" -> json.setString(field.getName(), String.class.getSimpleName());
                    case "Integer" -> json.setString(field.getName(), Integer.class.getSimpleName());
                    case "Double" -> json.setString(field.getName(), Double.class.getSimpleName());
                    case "Float" -> json.setString(field.getName(), Float.class.getSimpleName());
                    case "Boolean" -> json.setString(field.getName(), Boolean.class.getSimpleName());
                    case "List" -> json.setString(field.getName(), describeListType(field));
                    default -> json.setString(field.getName(), field.getType().getSimpleName());
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while getting JSON type for field " + field.getName());
                LOGGER.trace(e);
            }
        }

        return json;
    }

    private void serializeList(String key, List<?> list, NewJson json) {
        if (list.isEmpty()) {
            json.setList(key, List.of());
            return;
        }
        switch (list.getFirst()) {
            case String ignored -> json.setList(key, list.stream().map(String.class::cast).map(o -> (Object) o).toList());
            case Integer ignored -> json.setList(key, list.stream().map(Integer.class::cast).map(o -> (Object) o).toList());
            case Double ignored -> json.setList(key, list.stream().map(Double.class::cast).map(o -> (Object) o).toList());
            case Float ignored -> json.setList(key, list.stream().map(Float.class::cast).map(o -> (Object) o).toList());
            case Boolean ignored -> json.setList(key, list.stream().map(Boolean.class::cast).map(o -> (Object) o).toList());
            case UUID ignored -> json.setList(key, list.stream().map(UUID.class::cast).map(UUID::toString).map(o -> (Object) o).toList());
            default -> {
                final List<Object> serializedList = list.stream()
                        .map(this::serialize)
                        .map(j -> (Object) j)
                        .toList();
                json.setList(key, serializedList);
            }
        }
    }

    private String describeListType(Field field) {
        final Type genericType = field.getGenericType();
        return describeType(genericType);
    }

    private String describeType(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            final Type raw = parameterizedType.getRawType();
            final String rawName = (raw instanceof Class<?> rawClass) ? rawClass.getSimpleName() : raw.getTypeName();

            final Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length == 0) {
                return rawName;
            }

            final String argsDescription = Arrays.stream(typeArgs)
                    .map(this::describeType)
                    .collect(Collectors.joining(", "));

            return rawName + "<" + argsDescription + ">";
        }

        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }

        return type.getTypeName();
    }
}
