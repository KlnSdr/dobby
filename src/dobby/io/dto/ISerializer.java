package dobby.io.dto;

import dobby.util.json.NewJson;

public interface ISerializer {
    NewJson serialize(Object object);
}
