package dobby.io.dto;

public interface DTO<S, T> {
    T map(S source);
}
