package io.github.yluttsev.jooq.testcontainers.integration;

import java.lang.reflect.Method;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;

final class JooqJdbcConfiguration {

    private static final String DEFAULT_EXECUTION = "";
    private static final String JDBC_CLASS = "org.jooq.meta.jaxb.Jdbc";
    private static final String DRIVER_CLASS = "io.github.yluttsev.jooq.testcontainers.jdbc.PostgresContainerDriver";

    private JooqJdbcConfiguration() {
    }

    static void configure(Object extension, String url) {
        try {
            NamedDomainObjectContainer<?> executions = (NamedDomainObjectContainer<?>) call(extension, "getExecutions");
            Object execution = executions.getByName(DEFAULT_EXECUTION);
            Object configuration = call(execution, "getConfiguration");
            Object jdbc = getOrCreateJdbc(configuration);
            set(jdbc, "setDriver", DRIVER_CLASS);
            set(jdbc, "setUrl", url);
            set(jdbc, "setUrlProperty", null);
        } catch (ReflectiveOperationException exception) {
            throw new GradleException("Cannot configure JDBC for the official jOOQ plugin. "
                    + "Check that the plugin version is compatible.", exception);
        }
    }

    private static Object getOrCreateJdbc(Object configuration) throws ReflectiveOperationException {
        Object jdbc = call(configuration, "getJdbc");
        if (jdbc != null) {
            return jdbc;
        }

        Class<?> jdbcType = Class.forName(JDBC_CLASS, true, configuration.getClass().getClassLoader());
        jdbc = jdbcType.getConstructor().newInstance();
        configuration.getClass().getMethod("setJdbc", jdbcType).invoke(configuration, jdbc);
        return jdbc;
    }

    private static Object call(Object target, String method) throws ReflectiveOperationException {
        return target.getClass().getMethod(method).invoke(target);
    }

    private static void set(Object target, String method, String value) throws ReflectiveOperationException {
        Method setter = target.getClass().getMethod(method, String.class);
        setter.invoke(target, value);
    }
}
