package dobby.observer;

public interface Observer<T> {
    void onEvent(Event<T> event);
}
