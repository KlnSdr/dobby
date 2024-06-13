package dobby.observer;

public interface Observable<T> {
    void addObserver(Observer<T> observer);
    void removeObserver(Observer<T> observer);
    void fireEvent(Event<T> event);
}
