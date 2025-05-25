package dobby.task;

import common.inject.annotations.RegisterFor;
import dobby.Config;
import common.logger.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RegisterFor(ISchedulerService.class)
public class SchedulerService implements ISchedulerService {
    private final ArrayList<ScheduledExecutorService> schedulers = new ArrayList<>();
    private static final Logger LOGGER = new Logger(SchedulerService.class);

    public SchedulerService() {
    }

    public void addRepeating(Runnable task, int interval, TimeUnit unit) {
        if (isDisabled()) {
            LOGGER.warn("Scheduler is disabled, not scheduling task");
            return;
        }
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(task, 0, interval, unit);
        schedulers.add(scheduler);
    }

    public void stopAll() {
        schedulers.forEach(ExecutorService::shutdown);
    }

    private boolean isDisabled() {
        return Config.getInstance().getBoolean("dobby.scheduler.disabled", false);
    }
}
