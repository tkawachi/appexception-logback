# appexception-logback

Long stack frames in an exception log bores me.
appexception-logback shows only my frames.

## Install

Add following to `build.sbt` if you're using sbt:

    libraryDependencies += "com.github.tkawachi" % "appexception-logback" % "0.0.2"

## Pattern configuration

Add following to `logback.xml` under `<configuration>`:

    <conversionRule conversionWord="appEx"
      converterClass="com.github.tkawachi.appexception.AppThrowableProxyConverter" />

Then `%appEx` can be used to format an exception in `<pattern>`.
Example:

    <pattern>%level - %logger - %message%n%appEx{my.pkg1, my.pkg2.MyClass}</pattern>

Only stack frames which full qualified class name starts with `my.pkg1` or `my.pkg2.MyClass` will be logged by this pattern.

A class name pattern can also be defined as a property.

    <property name="appClassPrefixes" value="
      controllers,
      views
      "/>
    <pattern>%level - %logger - %message%n%appEx{${appClassPrefixes}}</pattern>

