/*
 * 
 * Copyright (c) 2007 ANDRE Sébastien (divxdede).  All rights reserved.
 * FutureBusyModel.java is a part of this JBusyComponent library
 * ====================================================================
 * 
 * JBusyComponent library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 * 
 * This is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
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
 * @author Andr� S�bastien (divxdede)
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
    public synchronized void setFuture(final Future future ) {
        setFuture(future,true);
    }

    @Override
    public void setBusy(final boolean value) {
        this.setBusyImpl(value);
    }
    
    /** Change a busy state and return a ticket identifier of this attempt
     */
    private synchronized int setBusyImpl(final boolean value) {
        super.setBusy(value);
        return (++this.ticket);
    }
    
    /** Change a busy state only if the ticket parameter is always the last given ticket
     */
    private synchronized boolean compareAndSetBusy( final boolean value , final int ticketValue ) {
        if( ticketValue == this.ticket ) {
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
    public synchronized void setFuture(final Future future , final boolean cancellable ) {
        if( this.service == null ) this.service = Executors.newSingleThreadExecutor();
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
        final Runnable tracker = new Runnable() {
            
            public void run() {
                int myTicket = 0;
                try {
                    final Future myFuture = FutureBusyModel.this.trackedFuture;
                    while( ! myFuture.isDone() ) {
                        myTicket = setBusyImpl(true);
                        try {
                            myFuture.get();
                        }
                        catch(final Exception e) {
                           if( myFuture != FutureBusyModel.this.trackedFuture ) {
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
        
        this.trackerFuture = this.service.submit(tracker);
    }

    /** Cancel the current <code>future</code> under process
     */
    @Override
	public synchronized void cancel() {
        final Future toCancel = this.trackedFuture;
        if( toCancel != null ) {
            toCancel.cancel(true);
        }
        this.trackedFuture = null;
        this.trackerFuture = null;
    }
}
