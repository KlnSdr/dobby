package dobby.logging;

public enum LogLevel {
    ERROR(Colors.RED), WARN(Colors.YELLOW), INFO(Colors.GREEN), DEBUG(Colors.BLUE);

    private final Colors color;
    LogLevel(Colors color) {
        this.color = color;
    }

    public String getColorized() {
        return color.getColor() + this.name() + Colors.RESET.getColor();
    }
}
