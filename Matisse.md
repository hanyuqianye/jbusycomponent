# Introduction #
We will introduce how use the `JBusyComponent` as a JavaBean in the palette manager.
And layout and configure the `JBusyComponent` with Matisse

# Configuring your netbeans #
Theses steps are done only one time and apply for your whole netbeans environment

## Create a JBusyComponent library ##
You need to create a library that represent our JBusyComponent project as follow:
  * Menu **Tools / Libraries**
  * Add a new library by clicking on button **New libraries...**
  * Library name : **JBusyComponent** and Library type : **Class library** and click **Ok**
  * Tab **ClassPath**, click on button **Add Jar/Folder**, select **jbusycomponent-1.0.2.jar**
  * Tab **Sources**, click on button **Add Jar/Folder**, select **jbusycomponent-1.0.2-sources.jar**
  * Tab **Javadoc**, click on button **Add Jar/Folder**, select **jbusycomponent-1.0.2-javadoc.jar**

## Create dependencies libraries ##
JBusyComponent project requires 2 other libraries, you must create 2 libraries on the same model:
  * Create a library JXLayer-3.0.4 for [JXLayer 3.0.4](https://jxlayer.dev.java.net/#download)
  * Create a library SwingX-1.6 for [SwingX 1.6](https://swingx.dev.java.net/servlets/ProjectDocumentList?folderID=11890&expandFolder=11890&folderID=6868)

## Adding JBusyComponent Bean to the palette manager ##
  * Menu **Tools / Palette / Swing/Awt Components**
  * Button **Add from library**
  * Select **JBusyComponent** library and click **Next** button
  * Select **JBusyComponent** bean and click **Next** button
  * Select **Beans** category (or anything else of your choice) and click **Finish** Button

# Configuring you projects #
This step is required for all project that want to use JBusyComponent with the palette manager

At this time, if you try to drag'n drop this bean into your design, you will receive a **can't instanciate this bean...**.

Your project must include the JBusyComponent library to be able to use it with the designer

  * Into you project **Properties**
  * Go to category **Libraries**
  * In tab **Compile** click into **Add library...**
  * Select **JBusyComponent** library and click on **Add Library**
  * Select **JXLayer-3.0.4** library and click on **Add Library**
  * Select **SwingX-1.6** library and click on **Add Library**

## Design with the palette ##
You can drag'n drop a JBusyComponent into your form. After what create the view component on you form and attach it in the **view** property of your JBusyComponent.
At this step, you can activate the **busy** property and the design will draw the busy state on you view components.