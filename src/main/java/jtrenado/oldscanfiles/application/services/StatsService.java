package jtrenado.oldscanfiles.application.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StatsService {

    public final static String COMPLETED_TASKS = "completedTasks";

    private AtomicInteger completedTasks = new AtomicInteger();

    public synchronized Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put(COMPLETED_TASKS, completedTasks.getAndSet(0));
        return stats;
    }

    public void incCompletedTasks(int delta) {
        completedTasks.addAndGet(delta);
    }
}
