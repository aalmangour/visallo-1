package org.visallo.web.initializers;

import com.google.inject.Inject;
import org.visallo.core.config.Configuration;
import org.visallo.core.model.user.UserRepository;
import org.visallo.core.user.User;
import org.visallo.core.util.VisalloLogger;
import org.visallo.core.util.VisalloLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphPropertyWorkerRunnerInitializer extends ApplicationBootstrapInitializer {
    public static final String CONFIG_THREAD_COUNT = GraphPropertyWorkerRunnerInitializer.class.getName() + ".threadCount";
    public static final int DEFAULT_THREAD_COUNT = 1;
    private static final VisalloLogger LOGGER = VisalloLoggerFactory.getLogger(GraphPropertyWorkerRunnerInitializer.class);
    private final Configuration config;
    private final UserRepository userRepository;
    private List<GraphPropertyWorkerRunnerHelper> workerHelpers = new ArrayList<>();

    @Inject
    public GraphPropertyWorkerRunnerInitializer(
            Configuration config,
            UserRepository userRepository
    ) {
        this.config = config;
        this.userRepository = userRepository;
    }

    @Override
    public void initialize() {
        LOGGER.debug("setupGraphPropertyWorkerRunner");

        int threadCount = config.getInt(CONFIG_THREAD_COUNT, DEFAULT_THREAD_COUNT);
        final User user = userRepository.getSystemUser();

        LOGGER.debug("starting graph property worker runners: %d", threadCount);
        for (int i = 0; i < threadCount; i++) {
            GraphPropertyWorkerRunnerHelper workerHelper = new GraphPropertyWorkerRunnerHelper(user);
            workerHelpers.add(workerHelper);
            Thread t = new Thread(workerHelper);
            t.setName("graph-property-worker-runner-" + t.getId());
            t.setDaemon(true);
            LOGGER.debug("starting graph property worker runner thread: %s", t.getName());
            t.start();
        }
    }

    @Override
    public void close() throws IOException {
        for (GraphPropertyWorkerRunnerHelper runner : workerHelpers) {
            runner.stop();
        }
    }
}
