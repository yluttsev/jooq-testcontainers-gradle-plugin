package io.github.yluttsev.jooq.testcontainers.config;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

/** Settings for migrations applied before jOOQ connects to PostgreSQL. */
public abstract class LiquibaseSettings {

    /** Default changelog path relative to the search root. */
    public static final String DEFAULT_CHANGE_LOG = "db/changelog/db.changelog-master.yaml";

    /** Default search root relative to the project directory. */
    public static final String DEFAULT_SEARCH_PATH = "src/main/resources";

    /** Default empty map of changelog parameters. */
    public static final Map<String, String> DEFAULT_PARAMETERS = Collections.emptyMap();

    /**
     * Applies the default changelog, search root, and parameters.
     *
     * @param layout project layout used to locate the default search root
     */
    @Inject
    public LiquibaseSettings(ProjectLayout layout) {
        getChangeLog().convention(DEFAULT_CHANGE_LOG);
        getSearchPath().from(layout.getProjectDirectory().dir(DEFAULT_SEARCH_PATH));
        getParameters().convention(DEFAULT_PARAMETERS);
    }

    /**
     * Selects the root changelog.
     *
     * @return changelog path relative to a search root
     */
    public abstract Property<String> getChangeLog();

    /**
     * Selects where Liquibase looks for changelogs and included resources.
     *
     * @return roots searched for the changelog and included resources;
     *         includes the project {@code src/main/resources} directory by default
     */
    public abstract ConfigurableFileCollection getSearchPath();

    /**
     * Filters changesets by context.
     *
     * @return optional Liquibase contexts; unset means no context filter
     */
    public abstract Property<String> getContexts();

    /**
     * Filters changesets by label.
     *
     * @return optional Liquibase labels; unset means no label filter
     */
    public abstract Property<String> getLabels();

    /**
     * Supplies values for changelog parameters.
     *
     * @return changelog parameters, empty by default
     */
    public abstract MapProperty<String, String> getParameters();
}
