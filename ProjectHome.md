# Enhance **any swing components** with a **busy** state [![](http://java.sun.com/products/jfc/tsc/sightings/images/webstart.small.jpg)](http://jbusycomponent.googlecode.com/svn/demo/jnlp/demo.jnlp) #

JBusyComponent works as follow:
  * Provide a `BusyModel` that allow you to maintain and control busy properties for your component.
  * A `view` that is the enhanced component that can now be busy
  * An `UI` for render by overlay the busy state
  * `BusyIcon` for smart and simple to use icon that can render a progress bar
  * Extrapolate remaining time on determinate task
  * Provide a `BusySwingWorker` for an easier integration with `SwingWorker` tasks

## The BusyModel ##
A BusyModel is the `data model` controlling busy properties of some components.
When the model is on a busy state, the view becomes inaccessible and a smart overlay  animation show it.

This animation can include a progress bar that reflet the underlying job progression or use a BusyIcon. If you enable the `cancellable` property, a **cancel** button will be shown also.

The BusyModel manage all busy properties
  * **`isBusy()`** / **`setBusy(boolean)`** defines if the model is currently busy or not
  * **`isDeterminate()`** / **`setDeterminate(boolean)`** defines if the model is **determinate** and provide `BoundedRangeModel` interface
  * **`isCancellable()`** / **`setCancellable(boolean)`** indicate if the underlying job when busy is cancellable

## Maven project integration ##
  * Add in your **pom.xml** a dependency into JBusyComponent artifact:
```
<dependency>
    <groupId>org.divxdede</groupId>
    <artifactId>jbusycomponent</artifactId>
    <version>1.2.3</version>
</dependency> 
```

The [Maven Central Repository](http://repo2.maven.org/maven2/org/divxdede/jbusycomponent/) contains released versions. If you want use snapshot or staged versions, refer to the [Nexus OSS Repository](http://oss.sonatype.org/content/groups/staging/org/divxdede/jbusycomponent).

## Manual integration ##
This project requires 3 libraries that you can donwload manually:
  * [SwingX 1.6.1](http://swinglabs.org/) for JDK 1.6+ or [SwingX 1.0](http://swinglabs.org/) for JDK 1.5
  * [JXLayer 3.0.4](http://jxlayer.dev.java.net/)
  * [Jarvis-Commons 0.2.3](http://code.google.com/p/jarvis-commons/)