package io.github.yluttsev.jooq.testcontainers.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.testcontainers.containers.PostgreSQLContainer;

final class ManagedConnection implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedConnection.class);

    private final Connection delegate;
    private final PostgreSQLContainer<?> container;
    private final ConnectionOptions options;
    private final AtomicBoolean closed = new AtomicBoolean();

    private ManagedConnection(Connection delegate, PostgreSQLContainer<?> container, ConnectionOptions options) {
        this.delegate = delegate;
        this.container = container;
        this.options = options;
    }

    static Connection wrap(Connection delegate, PostgreSQLContainer<?> container, ConnectionOptions options) {
        return (Connection) Proxy.newProxyInstance(
                ManagedConnection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new ManagedConnection(delegate, container, options)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("close") && method.getParameterCount() == 0) {
            close();
            return null;
        }
        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private void close() throws Throwable {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        Throwable failure = closeDelegate();
        failure = stopContainer(failure);
        if (failure != null) {
            throw failure;
        }
    }

    private Throwable closeDelegate() {
        try {
            delegate.close();
            return null;
        } catch (Throwable failure) {
            return failure;
        }
    }

    private Throwable stopContainer(Throwable failure) {
        try {
            container.stop();
            if (options.logs(Level.INFO)) {
                LOGGER.info("PostgreSQL container stopped");
            }
        } catch (Throwable stopFailure) {
            if (failure == null) {
                return stopFailure;
            }
            failure.addSuppressed(stopFailure);
        }
        return failure;
    }
}
