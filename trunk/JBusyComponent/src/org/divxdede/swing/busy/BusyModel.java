package org.divxdede.swing.busy;

import javax.swing.BoundedRangeModel;

/**
 * DataModel describe a <strong>busy</strong> state behaviour.
 * A busy state represent a disabled state (inacessible) for a while.
 * <p>
 * This state is commonly bound to a swing component that can't be used while it
 * is busy. Typically a pretty animation will be show.
 * <p>
 * When the model is gone to busy, it can be determinate that allow to track the
 * progress and time remaining like a <code>JProgressBar</code>.
 * In fact, a BusyModel is a  BoundedRangeModel that allow it to be bounded to a
 * <code>JProgressBar</code>.
 * <p>
 * BusyModel can be cancellable to allow the controller of this model to cancel the 
 * underlying task. 
 * 
 * @author André Sébastien
 */
public interface BusyModel extends BoundedRangeModel {

    /** 
     * Define if the model is on a "busy" state
     * @param value true to going in a busy state
     */
    public void setBusy(boolean value);
    
    /**
     * Returns true if the model is currently on a <code>busy</code> state
     * @return tue if the model is currently busy
     */
    public boolean isBusy();
    
    /** 
     * Define if the model is in a <code>determinate mode</code> or not
     * @param value true for change this model in a determinate mode
     */
    public void setDeterminate(boolean value);
    
    /** 
     * Returns true if the model is in a <code>determinate mode</code>.
     * @returns true if the model is in a determinate mode.
     */
    public boolean isDeterminate();

    /** 
     * Returns true if the model is <code>cancellable</code> the performing the job responsible on the <code>busy</code> state.
     * @return true is the model is cancellable
     */
    public boolean isCancellable();
    
    /** 
     * Define if this model is <code>cancellable</code>
     * @param value true for set this model cancellable.
     */
    public void setCancellable(boolean value);
    
    /** Invoke this method to cancel the current job responsible of the <code>busy</code> state.
     *  You need to override this method for implements you own cancellation process.
     */
    public void cancel();
}
