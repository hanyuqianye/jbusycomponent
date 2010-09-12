/*
 * Copyright (c) 2010 ANDRE Sébastien (divxdede).  All rights reserved.
 * RemainingTimeMonitor.java is a part of this JBusyComponent library
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
package org.divxdede.swing;

import java.util.concurrent.TimeUnit;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.divxdede.collection.CyclicBuffer;
import org.divxdede.commons.Disposable;

/**
 * Tools class that compute remaining time of a long duration task.<br>
 * The task progression is represented by the common interface {@link BoundedRangeModel}.
 * <p>
 * <code>RemainingTimeMonitor</code> store few past samples of the advance progression's speed
 * and use it for compute the remaining time.
 * <p>
 * By default the sample period is to <strong>500ms</strong> and can use <strong>10</strong> samples.<br>
 * In this configuration, that mean we use the last 5s of the task to compute the remaining task.
 * <p>
 * Exemple:
 * <pre>
 *          // Create a tracker
 *          RemainingTimeMonitor rtp = new RemainingTimeMonitor( myModel );
 *
 *          // Just simply call #getRemainingTime()
 *          long remainingTime = getRemainingTime();
 *          if( remainingTime != -1 ) {
 *              // you have a remaining time, you ca reinvoke this method for update the remaining time
 *          }
 * </pre>
 *
 * @author André Sébastien (divxdede)
 * @since 1.2
 */
public class RemainingTimeMonitor implements Disposable {

    private BoundedRangeModel model = null;
    private int bulkDelay = 500;

    private CyclicBuffer<Float> bulks = null;
    private long  bulkStartTime  = 0L;
    private float bulkStartRatio = 0f;

    private ChangeListener listener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            tick(true);
        }
    };

    /** Create a <code>RemainingTimeMonitor</code> for the specified {@link BoundedRangeModel}.<br>
     *  This instance will take samples of <strong>500ms</strong> and will use a maximum of <strong>10</strong> for compute remaining time.
     *
     * @param model BoundedRangeModel for which compute the remaining time
     */
    public RemainingTimeMonitor(BoundedRangeModel model) {
        this(model,500,10);
    }

    /** Create a <code>RemainingTimeMonitor</code> for the specified {@link BoundedRangeModel}.<br>
     *  This constructor allow to configure how this tool will monitor the model.
     *
     *  @param model BoundedRangeModel for which compute the remaining time
     *  @param samplePeriodDelay Delay of a sample
     *  @param sampleCount How many samples at maximum can be used for compute the remaining time.
     */
    public RemainingTimeMonitor(BoundedRangeModel model , int samplePeriodDelay , int sampleCount ) {
         this.model = model;
         this.bulkDelay = samplePeriodDelay;
         this.bulks = new CyclicBuffer<Float>(sampleCount);
         this.model.addChangeListener( this.listener );
    }

    /** Return the monitored model by this <code>RemainingTimeMonitor</code>.<br>
     * @return Monitored model
     */
    public BoundedRangeModel getModel() {
        return this.model;
    }

    /** Internal method that manages sample snapshot
     */
    private synchronized void tick(boolean starteable) {
        if( bulkStartTime == 0L ) {
            if( !starteable ) return;
            bulkStartTime  = System.currentTimeMillis();
            bulkStartRatio = getRatio( getModel() );
            return;
        }
        long currentTime = System.currentTimeMillis();
        long delay       = currentTime - bulkStartTime;
        if( delay > this.bulkDelay ) {
            float normalization_factor = (float)this.bulkDelay / (float)delay;
            float currentRatio         = getRatio( getModel() );
            float bulkAdvance          = (currentRatio - bulkStartRatio) * normalization_factor;

            bulks.add(bulkAdvance);
            bulkStartTime  = currentTime;
            bulkStartRatio = currentRatio;

            if( getModel().getValue() + getModel().getExtent() >= getModel().getMaximum() ) {
                dispose();
            }
        }
    }

    /** Free resources.<br>
     *  After this method call, this tool don't monitor anymore the underlying {@link BoundedRangeModel}
     */
    public void dispose() {
        if( this.listener != null ) {
            getModel().removeChangeListener( this.listener );
            this.listener = null;
        }
    }

    /** Compute the remaining time of the task underlying the {@link BoundedRangeModel}.<br>
     *  This tool monitor and analysys the task advance speed and compute a predicted remaining time.<br>
     *  If it has'nt sufficient informations in order to compute the remaining time and will return <code>-1</code>
     *
     *  @param unit Specificy the time unit to use for return the remaining time (ex: TimeUnit.SECONDS)
     *  @return Remaining time in milliseconds of the task underlying the {@link BoundedRangeModel}
     */
    public long getRemainingTime(TimeUnit unit) {
        return unit.convert( getRemainingTime() , TimeUnit.MILLISECONDS );
    }

    /** Compute the remaining time of the task underlying the {@link BoundedRangeModel}.<br>
     *  This tool monitor and analysys the task advance speed and compute a predicted remaining time.<br>
     *  If it has'nt sufficient informations in order to compute the remaining time and will return <code>-1</code>
     *
     *  @return Remaining time in milliseconds of the task underlying the {@link BoundedRangeModel}
     */
    public synchronized long getRemainingTime() {
        tick(false);
        if( bulks.isEmpty() ) return -1L;
        float currentRatio = getRatio( getModel() );
        if( currentRatio >= 1.0f ) return 0L;

        float advance = 0f;
        long  time    = 0L;

        for(int i = 0 ; i < bulks.size() ; i++ ) {
            advance += bulks.get(i).floatValue();
            time    += bulkDelay;
        }

        float remainingRatio = 1.0f - currentRatio;

        // Exemple "Has a advance of 10% in 5 seconds"
        // If it rest 25%, it should take 5 / 10 * 25 = 12.5 seconds
        float div = (float)advance * (float)remainingRatio;
        if( div == 0f ) return -1L; // can't compute infinite time
        return (long)( (float)time / div);
    }

    /** Return the current advance ratio of the specified {@link BoundedRangeModel}.
     *  This advance is given as a ratio [0 ~ 1] where 0 = 0% and 1 == 100%
     *
     *  @param model BoundedRangeModel for which we want to determine the current advance ratio
     *  @return Curent advance of the specidied{@link BoundedRangeModel}.
     *  @see #getSignificantRatioOffset()
     */
    public static float getRatio(BoundedRangeModel brm) {
        if( brm != null ) {
            int   length    = brm.getMaximum() - brm.getMinimum();
            int   value     = brm.getValue() + brm.getExtent();
            return (float)value / (float)length;
        }
        else return 0f;
    }
}