

# Introduction #

We will introduce how use JBusyComponent easily.
JBusyComponent wrap any swing component (called `view`) and take place on the components hierarchy. The JBusyComponent let you to manage `busy` properties on the `view` and provide **`BusyModel`**.

## This Library requires some dependancies ##
You need to download independently theses libraries in order to use `JBusyComponent`
  * [SwingX 1.6](http://swinglabs.org/)
  * [JXLayer 3.0.4](http://jxlayer.dev.java.net/)
  * [Jarvis-Commons 0.2.3](http://code.google.com/p/jarvis-commons/)
For maven users see [DeveloperMavenNotes](http://code.google.com/p/jbusycomponent/wiki/DeveloperMavenNotes)

## The first busy component ##
Let's see how to set busy a JTree :
```
>       /** The frame of our example
>        */
>       JFrame frame = new JFrame();
>       frame.setDefaultCloseOperation( frame.EXIT_ON_CLOSE );
>       
>       /** Create the JTree that will be our view
>        */
>       JTree tree = new JTree();
>
>       /** Create our JBusyComponent that wrap the JTree 
>        */
>       JBusyComponent<JTree> busyComponent = new JBusyComponent<JTree>(tree);
>
>       /** Add the JBusyComponent instead of the JTree to the frame
>        */
>       frame.getContentPane().add( busyComponent );
>       
>       /** Set our JTree busy !!!!
>        */
>       busyComponent.setBusy(true);
>       
>       /** show it
>        */
>       frame.pack();
>       frame.setVisible(true);
```

## Use the `BusyModel` for more **fun** features ##
A JBusyComponent refer a `BusyModel` to provide more functionnality:
  * **Start / Stop** the busy state: `setBusy( boolean )`
  * **Cancellable** property tells if the busy state can be cancelled: `setCancellable( boolean )`
  * **Determinate** property tells if the busy state can be tracked:  `setDeterminate( boolean )`
  * **Cancel** the task (only if it is cancellable) that is implementation dependant: `cancel()`

Exemple with a cancellable `BusyModel`:
```
>     /** Set our JTree busy but cancellable
>      */
>     BusyModel model = busyComponent.getBusyModel();
>     model.setCancellable(true);
>     model.setBusy(true);
```
## Using a determinate `BusyModel` ##
A `BusyModel` extends `BoundedRangeModel`.
That means that a `BusyModel` can manage the busy progression like any JProgressBar do.

Before all, for enable this feature you need to set the `BusyModel` in a determinate mode `BusyModel.setDeterminate(true)` and you can control the range of the progression and the current value.

This exemple set the `BusyModel` determinate to 50%:
```
>    model.setDeterminate(true);
>    model.setMinimum(0);
>    model.setMaximum(200);
>    model.setValue(100);  // 100 is at 50% from the range [0 - 200]
>    model.setBusy(true);
```

The `DefaultBusyModel` implementation help you to use determinate model with the `auto-completion` property as follow :
  * When the busy state is set to `true`, the `current value` is set to the `minimum`
  * When the `current value` reach the `maximum`, the busy state is set to `false`
Here an example:
```
>    /** Create our determinate BusyModel 
>     */
>    DefaultBusyModel model = new DefaultBusyModel();
>    busyComponent.setBusyModel( model );
>     
>    /** configure it as determinate and auto-completion
>     */
>    model.setAutoCompletionEnabled(true);
>    model.setDeterminate(true);
>    model.setMinimum(0);
>    model.setMaximum(200);
>       
>    /** Start a busy state, 
>     *  The current value will be set to 0 (minimum)
>     */
>    model.setBusy(true);
>     
>    while( model.isBusy() ) {
>        /** When the value reach the maximum, the model will stop automatically
>         *  the busy state
>         */
>        model.setValue( model.getValue() + 1 );
>    }
```

## Using a `SwingWorker` with `BusyModel` ##
TODO

## Change the icon to use with your JBusyComponent ##
You can change the default icon used by JBusyComponent for render the busy animation overlay.
For achieve this, you need to provide a BusyLayerUI to the JBusyComponent like it:

```
>       /** Create our JBusyComponent that wrap the JTree 
>        */
>       JBusyComponent<JTree> busyComponent = new JBusyComponent<JTree>(tree);
>
>       /** Create a LayerUI
>        */
>       LayerUI ui = new BasicLayerUI();
> 
>       /** Set an icon
>        */
>       ui.setBusyIcon( new RadialBusyIcon( anIcon ) );
>       
>       /** Set the UI
>        */
>       busyComponent.setBusyLayerUI(ui);
>
```

This API provide 3 default implementations of 'BusyIcon'
  * **`InfiniteBusyIcon`**: Default icon rendering a infinite animation
  * **`RadialBusyIcon`** : An icon with a radial progress bar beside it
  * **`DefaultBusyIcon`** : An Icon with an horizontal progress bar.

Theses icons can be used like any other icons and it's not mandatory to use theses with a JBusyComponent. You can use theses with `JLabel` or all other components

## Track a concurrent task with a `FutureBusyModel` ##
One from the coolest package that came with the JDK 1.5 is the package `java.util.concurrent`.
When you use some `ExecutorService`, you can enqueue some tasks to execute when it is possible. The `ExecutorService` implementation is responsible to manage theses tasks (use one or more threads, manage the pool size and so more).

The nice design with theses `ExecutorService` is the `Future` instance resulting in the task submitted. This object allow to monitor and control the enqueued task. You can know if the task is completed or not and you can also cancel it.

We provide a **really cool** `BusyModel` implementation that can track a `Future` like it:
```
>     /** Create our BusyModel that can track a Future
>      *  And bound it to our JBusyComponent
>      */
>     FutureBusyModel model = new FutureBusyModel();
>     busyComponent.setBusyModel( model );
>        
>     /** Create a dummy task that sleep 5s with a Runnable
>      */
>     Runnable task = new Runnable() {
>         public void run() {
>            try { 
>                 Thread.sleep(5000);
>            }
>            catch(InterruptedException ie) {
>                // Canceled task
>                // In a morecomplex task, see also the 
>                // Thread.currentThread().isInterrupted() flag
>            }
>         }
>     };
>       
>     /** Create an ExecutorService and submit our task
>      */
>     ExecutorService service = Executors.newSingleThreadExecutor();
>     Future          future  = service.submit(task);
>       
>     /** Let our model to reflet this task
>      *  The Model will be busy until the task will complete or be cancelled
>      *  This method will set automatically the model cancellable.
>      */
>     model.setFuture( future );
>
>     ...
>     ...
>
>     // When your program is exiting or the gui using the model is no more used, 
>     // you must dispose the FutureBusyModel in order to free all resources (unless your JVM will not exiting correctly)
>     model.dispose();
>
>
```

The `FutureBusyModel` implement the **`cancel()`** method that cancel the underlying task by the `Future`