package dobby.io.dto;

import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.util.json.NewJson;

import java.lang.reflect.Field;
import java.util.List;

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
                    default -> json.setJson(field.getName(), serialize(value));
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Error occurred while serializing object");
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
            default -> {
                final List<Object> serializedList = list.stream()
                        .map(this::serialize)
                        .map(j -> (Object) j)
                        .toList();
                json.setList(key, serializedList);
            }
        }
    }
}
