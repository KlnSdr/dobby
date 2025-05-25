package dobby.task;

import java.util.concurrent.TimeUnit;

public interface ISchedulerService {
    void addRepeating(Runnable task, int interval, TimeUnit unit);
    void stopAll();
}
