package dobby.observer;

public class Event<T> {
    private final T data;
    private final EventType type;

    public Event(EventType type, T data) {
        this.data = data;
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public EventType getType() {
        return type;
    }
}
