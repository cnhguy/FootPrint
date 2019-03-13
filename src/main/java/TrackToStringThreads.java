import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used by DebugExtractor to track threads attempting to access toString information from objects.
 */
public class TrackToStringThreads {
    private AtomicBoolean skipBreakpoints;
    private AtomicBoolean restartExecution;

    private AtomicBoolean finishedAddingThreads;
    private AtomicBoolean thisRestartExecution;
    private Runnable runOnResume;
    private AtomicBoolean allThreadsFinished;

    public ThreadGroup group;

    /**
     * Creates a new thread tracker that will not resume execution when all threads have finished
     * @param skipBreakpoints reference to DebugListener's skipBreakpoints
     * @param restartExecution reference to DebugExtractor's restartExecution
     */
    public TrackToStringThreads(AtomicBoolean skipBreakpoints, AtomicBoolean restartExecution) {
        this.skipBreakpoints = skipBreakpoints;
        this.restartExecution = restartExecution;

        this.finishedAddingThreads = new AtomicBoolean(false);
        this.thisRestartExecution = new AtomicBoolean(false);
        allThreadsFinished = new AtomicBoolean(false);

        this.group = new ThreadGroup("toString" + this.hashCode());
    }

    /**
     * Adds the given thread to the group of currently running threads and starts it.
     * @param runnable the thread to add
     */
    public void addThreadAndStart(Runnable runnable) {
        skipBreakpoints.compareAndSet(false, true);
        restartExecution.compareAndSet(true, false);
        Thread newThread = new Thread(group, () ->{
            runnable.run();
            doActionsIfAllThreadsFinished();
        });
        newThread.start();
    }

    private synchronized void doActionsIfAllThreadsFinished() {
        if (finishedAddingThreads.get()) {
            if (group.activeCount() == 1)
                allThreadsFinished.set(true);
            if (allThreadsFinished.get()) {
                skipBreakpoints.set(false);
                restartExecution.set(true);
                if (thisRestartExecution.get()) {
                    System.out.println("thread tracker resume after threads finished");
                    runOnResume.run();
                }
            }
        }
    }

    /**
     * Should be called only if the DebugExtractor has finished adding new threads to the tracker
     * @param value
     */
    public void setFinishedAddingThreads(boolean value) {
        finishedAddingThreads.set(value);
    }

    /**
     * Should be called only if the DebugExtractor expects the thread tracker to restart execution when all its threads
     * have finished.
     * @param resume code that will resume execution
     */
    public void resumeExecutionOnAllThreadsFinished(Runnable resume) {
        System.out.println("thread tracker resume execution");
        runOnResume = resume;
        thisRestartExecution.set(true);
    }
}
