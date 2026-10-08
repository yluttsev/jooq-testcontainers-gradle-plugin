package ru.luttsev.jooq.testcontainers.config;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

public abstract class LiquibaseSettings {

    public static final String DEFAULT_CHANGE_LOG = "db/changelog/db.changelog-master.yaml";
    public static final String DEFAULT_SEARCH_PATH = "src/main/resources";
    public static final Map<String, String> DEFAULT_PARAMETERS = Collections.emptyMap();

    @Inject
    public LiquibaseSettings(ProjectLayout layout) {
        getChangeLog().convention(DEFAULT_CHANGE_LOG);
        getSearchPath().from(layout.getProjectDirectory().dir(DEFAULT_SEARCH_PATH));
        getParameters().convention(DEFAULT_PARAMETERS);
    }

    public abstract Property<String> getChangeLog();
    public abstract ConfigurableFileCollection getSearchPath();
    public abstract Property<String> getContexts();
    public abstract Property<String> getLabels();
    public abstract MapProperty<String, String> getParameters();
}
