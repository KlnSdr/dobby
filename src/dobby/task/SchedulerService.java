package dobby.task;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    private static SchedulerService instance;
    private final ArrayList<ScheduledExecutorService> schedulers = new ArrayList<>();

    public static SchedulerService getInstance() {
        if (instance == null) {
            instance = new SchedulerService();
        }
        return instance;
    }

    public void addRepeating(Runnable task, int interval, TimeUnit unit) {
        final ScheduledExecutorService scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(task, 0, interval, unit);
        schedulers.add(scheduler);
    }

    public void stopAll() {
        schedulers.forEach(ExecutorService::shutdown);
    }
}
