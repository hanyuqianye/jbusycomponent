**History**



---


# Version 1.2.3 (20/04/2013) #

## Enhancement ##
  * The `getMillisToDecideToPopup()` of `BasicBusyLayerUI` is now effective with undeterminate model. Theses models waits the time `MillisToDecideToPopup` before to render the busy state.


---


# Version 1.2.2 (29/03/2011) #

## Bug Fix ##
  * Fix `FutureBusyModel` that lmust now be disposed when it's no more used for release it's own dedicated thread. The JVM will now terminate correctly if all `FutureBusyModel` are disposed.
  * Fix `RemainingTimeMonitor` and `BasicBusyLayerUI` that may cause the view to change state (busy / not busy) along the task execution.
  * Update dependency of jarvis-commons that fix a bug in `CyclicBuffer` class

## Enhancement ##
  * A new `BusySwingWorker` class that allow simply to create a `SwingWorker` using a `BusyModel` for disabling view while it's running.
  * A full review of `RemainingTimeMonitor` for more accuracy in the estimation process.
  * A `BusyModel` can now register `ActionListener` and fire events when the model start, is canceled or when it's done.


---


# Version 1.2.1 (12/12/2010) #

## Bug Fix ##
  * Fix remaining time feature that may cause the busy icon not showing when it would be busy.

## Enhancement ##
  * Optimize **repaint** events on the `BasicBusyLayerUI` implementation.


---


# Version 1.2 (02/12/2010) #

## Enhancement ##
  * Delayed progress bar popup. After few times, the `BasicBusyLayerUI` predict remaining time and if it's long enough show the progress bar.
    * A delay of **300 ms** is used by default to compute remaining time.
    * A minimum of **1200 ms** of remaining time is required in order to show the progress bar.
    * Theses delays are modifiables with `setMillisToDecideToPopup(int)` and `setMillisToPopup(int)` methods.
    * The `JBusyComponent` view is locked (can't be accessed anymore) instantly when the model become busy even if the progress bar is not yet shown.
  * Capability to show the remaining time above the progress bar using the `setRemainingTimeVisible(boolean)` on the `BasicBusyLayerUI`
    * Introduction of `RemainingTimeMonitor` tool for computing remaining time from any `BoundedRangeModel` and usable outside the `JBusyComponent`
  * JDK 1.5 integration is now compatible
    * Use SwingX 1.0  dependency when the JDK is 1.5 for compatibility issue
    * Use SwingX 1.6+ dependency when the JDK is 1.6+


---


# Version 1.1 (23/07/2010) #

## Enhancement ##
  * Introduction of BusyIcon (DefaultBusyIcon,RadialBusyIcon,InfiniteBusyIcon)
  * Introduction of BoundedRangeModelHub
  * New demo


---


# Version 1.0.2 (08/05/2010) #

## Bug Fix ##
  * Fix mouse cursor updating

## Enhancement ##
  * Project build on top of maven instead of ant
  * Update dependencies with JXLayer 3.0.4 and SwingX 1.6
  * JBusyComponent implements Scrollable interface for respect view constraint from JTable or JTree


---


# Version 1.0.1 (05/04/2009) #

## Bug Fix ##
  * Now the `BasicBusyLayerUI` render the cancel button as unvisited when the animation is showing.
  * Update pom.xml to refer SwingX-0.9.6 and JXLayer-3.0.3

## Enhancement ##
  * Add BusyModel#getDescription() allowing to render a description when the model is busy


---


# Version 1.0 (31/07/2008) #
  * First release of **JBusyComponent**


---