package org.divxdede.swing.busy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A BusyModel implementation allowing to reflet the execution of a Future task.
 * While the job task underlying the Future is running, this model will be set to a <code>busy</code> state.
 * <p>
 * Use <code>setFuture</code> for defining the <code>Future</code> to reflet.
 * 
 * @author André Sébastien
 */
public class FutureBusyModel extends DefaultBusyModel {
    
    /** Members
     */
    private ExecutorService service             = null;
    private int             ticket              = 0;
    private Future          trackedFuture       = null;
    private Future          trackerFuture       = null;
    
    /** 
     * Reflet a new <code>Future</code> to reflet.
     * This model will be set as <code>undeterminate</code> but <code>cancellable</code> model.
     * @param future New Future to reflet.
     */
    public synchronized void setFuture(Future future ) {
        setFuture(future,true);
    }

    @Override
    public void setBusy(boolean value) {
        this.setBusyImpl(value);
    }
    
    /** Change a busy state and return a ticket identifier of this attempt
     */
    private synchronized int setBusyImpl(boolean value) {
        super.setBusy(value);
        return (++ticket);
    }
    
    /** Change a busy state only if the ticket parameter is always the last given ticket
     */
    private synchronized boolean compareAndSetBusy( boolean value , int ticketValue ) {
        if( ticketValue == ticket ) {
            setBusy(value);
            return true;
        }
        return false;
    }
    
    /** 
     * Reflet a new <code>Future</code> to reflet.
     * This model will be set as <code>undeterminate</code> and <code>cancellable</code> if specified.
     * @param future New Future to reflet.
     * @param cancellable true for let this future cancellable by the JBusyComponent
     */
    public synchronized void setFuture(Future future , boolean cancellable ) {
        if( service == null ) service = Executors.newSingleThreadExecutor();
        if( future == null ) return;
        
        /** Hold the tracked future
         */
        this.trackedFuture = future;

        /** Cancel the previous tracker
         */
        if( this.trackerFuture != null ) {
            this.trackerFuture.cancel(true);
        }
        this.trackerFuture = null;
        
        /** undeterminate but cancelable task
         */
        this.setDeterminate(false);
        this.setCancellable(cancellable);

        /** Tracker job to execute on our dedicated thread
         */
        Runnable tracker = new Runnable() {
            
            public void run() {
                int myTicket = 0;
                try {
                    Future myFuture = trackedFuture;
                    while( ! myFuture.isDone() ) {
                        myTicket = setBusyImpl(true);
                        try {
                            myFuture.get();
                        }
                        catch(Exception e) {
                           if( myFuture != trackedFuture ) {
                               /** probably the model must reflet now a different Future
                                *  We must stop to reflet this one
                                */
                               break;
                           }
                        }
                    }
                }
                finally {
                    compareAndSetBusy( false, myTicket );
                }
            }
        };
        
        this.trackerFuture = service.submit(tracker);
    }

    /** Cancel the current <code>future</code> under process
     */
    public synchronized void cancel() {
        Future toCancel = trackedFuture;
        if( toCancel != null ) {
            toCancel.cancel(true);
        }
        trackedFuture = null;
        trackerFuture = null;
    }
}
