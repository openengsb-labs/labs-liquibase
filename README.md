How to build
====================
* Install JDK 7 or higher

You can install [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or
[OpenJDK](http://openjdk.java.net/install/index.html) depending on the OS you use.
Other JVM implementations should also work, but are untested.

* Install [Maven 3 or higher](http://maven.apache.org/download.html)

Be sure to follow the provided [installation instructions](http://maven.apache.org/download.html#Installation)

* configure **JAVA_HOME** and **PATH** environment variables

make sure the JAVA_HOME environment variable points to the path of your JDK installation and that both **javac** and
**mvn** are available in your PATH-variable

* Run **mvn install** from the project's root directory

That's it. You can now even use the features.xml to install org.openengsb.labs.liquibase.extender and liquibase-osgi
into Karaf or, if you prefer you can also install it manually.

How to run
====================
Simply install org.openengsb.labs.liquibase.extender.jar and liquibase-osgi into your osgi container. Please also add
some SLF4J implementation like [Pax Logging](http://team.ops4j.org/wiki/display/paxlogging/Download), and some configuration
admin implementation like [Felix Config Admin](https://felix.apache.org/site/apache-felix-config-admin.html). For the
migrations the first javax.sql.DataSource found in the registry will be used if not configured otherwise!

Basically the same limitations apply to liquibase as if you're using it manually from multiple files. The file name
is required to be the same and you shouldn't change the any parts already deployed into your environment.

To run liquibase you've to do two things:

1) Add some header entries to your bundle (required)
2) Add a liquibase configuration file (optional)

Bundle Header
------------------------
You've the following two parameters available you might want to add to your header:

* Liquibase-Persistence (required)
* Liquibase-StartLevel (optional)

The "Liquibase-Persistence" property requires a path to your liquibase.xml file. This could look like:

<pre>
Liquibase-Persistence: OSGI-INF/liquibase/master.xml
</pre>

By default those liquibase files are applied as they are loaded. If you want to load them in a specific order use the
"Liquibase-StartLevel" property. This start level works completely equivalent to the bundle start level from the OSGi
specification and is used to define the order in which database changes from different bundles should be applied.

<pre>
<Liquibase-StartLevel>2</Liquibase-StartLevel>
</pre>

Configuration File
-----------------------
While liquibase comes with quite sane default settings it might be interesting to configure some parts of the plugin
on the fly. The property file needs to be registered using "org.openengsb.labs.liquibase" as PID for the configuration
admin. The following properties could be modified:

<pre>
#
# Basically this property defines if liquibase changes are applied as soon as they're found or if you want to write
# your own UI and apply those changes manually using the DatabaseMigrationCenter service.
#
# Per default this property is set to false (which means you have to apply those changes manually)
#

useLiquibase=false

#
# The osgi.jndi.service.name property for the javax.sql.DataSource service which should be used to apply the database
# migration. Please be aware that those changes are most likely to require create/alter table permissions!
#

datasource=jdbc/rx

#
# The log level to use (only use one of those properties at once)
#

# loglevel=SEVERE
loglevel=INFO
# loglevel=DEBUG
# loglevel=OFF
# loglevel=WARNING

#
# Additional liquibase properties (please use the liquibase documentation to lookup those)
#

# defaultSchema=public
# contexts=
# parameter.XXX=
</pre>

Interact with Liquibase
========================
To interact with liquibase at runtime (e.g. if you want to provide some UI for it) use the
org.openengsb.labs.liquibase.extender.DatabaseMigrationCenter service available via the OSGi registry:

<pre>
/**
     * Checks all available liquibase files and compare them with the current database. If any differences
     * are found this method returns true. By default this method only checks once if a migration is required
     * (this allows to check every time a specific service is called or a web page should be displayed). Some events
     * require this method to reevaluate anyhow. E.g. if a new bundle is installed/uninstalled.
     */
    boolean isMigrationRequired() throws DatabaseMigrationException;

    /**
     * There are situations when the automatic refresh algorithm in the #isMigrationRequired method isn't enough;
     * one example is if you delete the md5sums in the database itself. In that (or a similar) case simply call this
     * method. The next #isMigrationRequired call will force a reevaluation.
     */
    void forceMigrationRevaluation();

    /**
     * This call executes all liquibase changes not already applied.
     */
    void executeMigration() throws DatabaseMigrationException;

    /**
     * This method can be used to display all entries in all liquibase.xml files AND if they're already applied
     * to the database or not.
     */
    MigrationDescription printMigrationDescription() throws DatabaseMigrationException;
</pre>
