/*
 * 
 * Copyright (c) 2007 ANDRE Sébastien (divxdede).  All rights reserved.
 * BasicBusyLayerUI.java is a part of this JBusyComponent library
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
package org.divxdede.swing.busy.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.divxdede.swing.busy.BusyModel;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;

/**
 * A Default implementation of BusyLayerUI.
 * <p>
 * This UI provide a busy animation, progress bar and cancellation button
 * regarding the <code>BusyModel</code>.
 * <p>
 * You can enhance any swing components with busy fonctionnality like it:
 * <pre>
 *      // your component to enhance
 *      JComponent comp = .....;
 * 
 *      // Create the JXLayer decorator
 *      JXLayer<JComponent> layer = new JXLayer<JComponent>(comp);
 * 
 *      // Create the Busy Layer UI delegate
 *      BusyLayerUI ui = new BasicBusyLayerUI();
 * 
 *      // Attach the UI to the decorator
 *      layer.setUI( (LayerUI)ui );
 * 
 *      // Add the decorator to the container instead of our component
 *      myContainer.add( layer );
 * 
 *      // Use the BusyModel for control the busy state on our component
 *      // If multiple components share the same BusyModel, all of theses will be
 *      // triggered by the same model
 *      BusyModel model = ui.getBusyModel();
 *      model.setBusy(true); // an animation over our component is shown
 * </pre>
 * 
 * @author André Sébastien
 */
public class BasicBusyLayerUI extends AbstractBusyLayerUI {

    /** Components
     */
    JXPanel        jXGlassPane       = new JXPanel();
    JXBusyLabel    jXBusyLabel       = new CustomBusyLabel();
    JProgressBar   jProgressBar      = new JProgressBar();
    JXHyperlink    jXHyperlinkCancel = new JXHyperlink();
    
    /** Listener for cancelling action
     */
    ActionListener cancelListener    = null;
    
    /** Veil members (color and alpha) to render
     *  A null color or a 0 alpha mean no veil
     */
    float                  veilAlpha          = 0;     // 0 < alpha > 1
    Color                  veilColor          = null;

    /** Shading members for rendering the veil with an animation
     *  A shadeDelayInterval <= 0 means no shading
     */
    int                    shadeDelayInterval = 0;  // in milliseconds
    int                    shadeDelayTotal    = 0;  // in milliseconds

    /** Internal members for manage shading & veil rendering
     */
    private int            alpha              = 0;
    private Timer          timer              = null;
    private Painter        painter            = null;    

    /** Insets used
     */
    private static final Border NO_SPACE = new EmptyBorder( new Insets(0,0,0,0) );
    private static final Border MARGIN   = new EmptyBorder( new Insets(0,10,0,0) );

    /** Basic Implementation with default values
     */
    public BasicBusyLayerUI() {
        this( 200 , 30 , 0.7f  , Color.WHITE );
    }

    /** Basic Implementation with shading configuration's
     *  @param shadeDelay Shading delay in milliseconds for render <code>busy</code> state change, 0 means no shading
     *  @param shadeFps Frame per Seconds to use for render the shading animation. 
     *  @param veilAlpha Alpha ratio to use for the veil when the model is <code>busy</code>
     *  @param veilColor Color to use for render the veil
     */ 
    public BasicBusyLayerUI(final int shadeDelay , final int shadeFps , final float veilAlpha  , final Color veilColor) {
        
        this.cancelListener     = createCancelListener();
        this.timer              = createBackgroundShadingTimer();
                                  createGlassPane();
        
        
        this.shadeDelayTotal    = shadeDelay;
        this.shadeDelayInterval = ( this.shadeDelayTotal <= 0 ? 0 : (int)(1000f / shadeFps) );
        
        this.veilAlpha          = veilAlpha;
        this.veilColor          = veilColor;
    }
    
    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
        
        final JXLayer layer = (JXLayer)c;
        layer.setGlassPane( this.jXGlassPane );
    }

    @Override
    public void uninstallUI(final JComponent c) {
        super.uninstallUI(c);
        final JXLayer layer = (JXLayer)c;
        layer.setGlassPane( null );
    }

    /** 
     * Returns the busy painter to use for render the busy animation
     * @return BusyPainter used for render the friendly busy animation
     */
    public BusyPainter getBusyPainter() {
        return this.jXBusyLabel.getBusyPainter();
    }
    
    /** 
     * Define the busy painter to use for render the friendly busy animation
     * @param busyPainter New busy painter to use for render the friendly busy animation
     */
    public void setBusyPainter(final BusyPainter busyPainter) {
        this.setBusyPainter(busyPainter,null);
    }
    
    /** 
     * Define the busy painter to use for render the friendly busy animation
     * @param busyPainter New busy painter to use for render the friendly busy animation
     * @param preferredSize Preferred Size to use for this painter animation
     */
    public void setBusyPainter(final BusyPainter busyPainter , final Dimension preferredSize ) {
        if( preferredSize != null ) {
            this.jXBusyLabel.setIcon( new EmptyIcon( preferredSize.width , preferredSize.height ) );
        }
        else
            this.jXBusyLabel.setIcon(null);
        
        this.jXBusyLabel.setBusyPainter( busyPainter );
        this.updateUI();
    }
    
    @Override
    protected void paintLayer(final Graphics2D g2, final JXLayer<JComponent> l) {
        super.paintLayer(g2, l);
        final Painter painter = getBackGroundPainter();
        if( painter != null ) {
            painter.paint(g2, null , l.getWidth(), l.getHeight() );
        }
    }
    
    @Override
    protected void updateUIImpl() {
        final BusyModel myModel = getBusyModel();
        final boolean   isBusy  = myModel == null ? false : myModel.isBusy();

        /** Visible states
         */
        this.jXBusyLabel.setVisible( isBusy );
        this.jProgressBar.setVisible( isBusy && myModel.isDeterminate() );

        { final boolean hyperlinkVisible = isBusy && myModel.isCancellable();
          if( hyperlinkVisible && !this.jXHyperlinkCancel.isVisible() ) 
              this.jXHyperlinkCancel.setClicked(false);
        
          this.jXHyperlinkCancel.setVisible( hyperlinkVisible );
        }

        /** Busy animation (start or stop)
         */
        this.jXBusyLabel.setBusy( isBusy ); 

        /** Background shading animation (check if start needed)
         */
        this.manageBackgroundVeil();
        
        /** If cancellable, update it's border regarding the progress bar visible state
         */
        if( isBusy ) {
            if( myModel.isCancellable() )
                this.jXHyperlinkCancel.setBorder( this.jProgressBar.isVisible() ? MARGIN : NO_SPACE );

            /** Update the % 
             */
            String descr = myModel.getDescription();
            this.jXBusyLabel.setText( descr == null ? getPercentProgressionString() : descr );
        }
        super.updateUIImpl();
    }

    @Override
    public void setBusyModel(final BusyModel model) {
        super.setBusyModel(model);
        
        if( getBusyModel() != null ) 
            this.jProgressBar.setModel( model );
    }

    /** Create our glasspane
     */
    private JComponent createGlassPane() {
        
        final GridBagLayout layout = new GridBagLayout();
        this.jXGlassPane.setLayout( layout );
        this.jXGlassPane.setOpaque(false);

        final Insets emptyInsets = new Insets(0,0,0,0);
        final GridBagConstraints gbcLabel = new GridBagConstraints(1,1,2,1,0,0, GridBagConstraints.CENTER , GridBagConstraints.NONE , emptyInsets , 0 , 0 );
        final GridBagConstraints gbcBar   = new GridBagConstraints(1,2,1,1,0,0, GridBagConstraints.CENTER , GridBagConstraints.NONE , emptyInsets , 0 , 0 );
        final GridBagConstraints gbcLink  = new GridBagConstraints(2,2,1,1,0,0, GridBagConstraints.CENTER , GridBagConstraints.NONE , emptyInsets , 0 , 0 );
        
        this.jXGlassPane.add( this.jXBusyLabel , gbcLabel );
        this.jXGlassPane.add( this.jProgressBar , gbcBar );
        this.jXGlassPane.add( this.jXHyperlinkCancel , gbcLink );

        this.jXBusyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        this.jXBusyLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        this.jXBusyLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        this.jXHyperlinkCancel.setText( UIManager.getString("OptionPane.cancelButtonText") );
        this.jXHyperlinkCancel.addActionListener( this.cancelListener );

        updateUI();
        return this.jXGlassPane;
    }
    
    /** Return the percent progression string
     */
    private String getPercentProgressionString() {
        final BusyModel myModel = getBusyModel();
        final boolean   isBusy  = myModel == null ? false : myModel.isBusy();
        if( ! isBusy )                 return null;
        if( !myModel.isDeterminate() ) return null;
        
        final int   range   = myModel.getMaximum() - myModel.getMinimum();
        final int   value   = myModel.getValue();
        final float percent = ( 100f / range ) * ( value - myModel.getMinimum() );
        
        return  ((int)percent) + " %";
    }
    
    /** Create the Listener managing the cancel action when click on the hyperlink
     */
    private ActionListener createCancelListener() {
        return new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                try {
                    getBusyModel().cancel();
                }
                catch(final Exception e2) {
                    e2.printStackTrace();
                }
            }
        };
    }
    
    /** Indicate if this layer should be placed in a locked state.
     *  This default implementation return <code>true</code> if the model is "busy"
     *  OR the background animation is not ended.
     */
    @Override
    protected boolean shouldLock() {
        return super.shouldLock() || isBackgroundPainterDirty();
    }
    
    /** Get the painter ready for render over the specified component.
     *  If the internal painter is not compatible with the specified component, 
     *  then another one painter will be created
     */
    private synchronized Painter getBackGroundPainter() {
        return this.painter;
    }
    
    /** Overridable method that neeed to create a painter with a specified alpha level.
     *  <code>BasicBusyLayerUI</code> invoke this method each time requested
     *  for paint the shadowing animation. 
     *  @param alpha The alpha value (0 ~ 255) requested for the painter
     *  @return painter the new painter with the correct alpha value
     */
    protected Painter createBackgroundPainter( final int alpha ) {
        return new MattePainter( 
                new Color( this.veilColor.getRed() ,
                           this.veilColor.getGreen() ,
                           this.veilColor.getBlue() ,
                           alpha ) 
               );
    }
    
    /** Manage the background shading by starting the dedicated timer if needed.
     *  This method can only start the timer, never it stop it.
     *  <p>
     *  For start a timer, the background painter must be dirty (shading not completed)
     *  and the timer must not already running.
     *  <p>
     *  If no shading is requested (shadeDelayTotal <= 0 ) then the background is
     *  updated directly by this method without using any timer)
     */
    private synchronized void manageBackgroundVeil() {
        if( ! this.isBackgroundPainterDirty() ) return;
        
        if( this.shadeDelayTotal <= 0 ) {
            /** Do it directly without using a timer (because no animation is needed)
             */
            this.updateBackgroundPainter();
        }
        else {
            if( this.timer.isRunning() ) return;
            this.timer.start();
        }
    }
    
    /** Create the Timer responsible to animate the white veil shading.
     *  <p>
     *  This timer update the background painter at regular intervals (shadeDelayInterval).
     *  After each update, this layer is updated for repaint it.
     *  <p>
     *  When the shading is completed, this timer stop itself.
     */
    private synchronized Timer createBackgroundShadingTimer() {
        final ActionListener actionListener = new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                synchronized( BasicBusyLayerUI.this ) {
                    updateBackgroundPainter();
                    if( ! isBackgroundPainterDirty() ) {
                        ((Timer)e.getSource()).stop();
                    }
                }
            }
        };
        return new Timer(this.shadeDelayInterval , actionListener );
    }
    
    /** Indicate if the background painter is dirty.
     *  This method consider the painter as dirty along the shading is not completed.
     *  If no veil is request by this UI, this method return <code>false</code>
     *  <p>
     *  The shading is considered as completed when
     *    - the painter is opaque and busy  (opaque is relative from the veilAlpha)
     *    - the painter is translucent and not busy 
     *  If it's the case, a new painter must be created and this layer UI should be repainted with it
     */
    private synchronized boolean isBackgroundPainterDirty() {
        if( this.veilColor == null || this.veilAlpha == 0f ) return false;
        
        final BusyModel myModel = getBusyModel();
        final boolean   isBusy  = myModel == null ? false : myModel.isBusy();
        
        if( isBusy  && this.alpha < 255 ) return true;
        if( !isBusy && this.alpha > 0 )   return true;
        return false;
    }
    
    /** Update the painter for paint the next step of the shading.
     *  <p>
     *  This method request an updateUI() if a new painter is created.
     *  The method update the alpha of the white veil depending <code>shadeDelayTotal</code> delay
     *  and <code>shadeDelayInterval</code> delay
     */
    private synchronized void updateBackgroundPainter() {
        final BusyModel myModel = getBusyModel();
        final boolean   isBusy  = myModel == null ? false : myModel.isBusy();
        
        final Painter oldPainter = this.painter;
        
        if( isBusy && this.alpha < 255 ) {
            if( this.shadeDelayTotal <= 0 ) {
                this.alpha = 255;
            }
            else {
                this.alpha += 255 / (this.shadeDelayTotal / this.shadeDelayInterval);
                if( this.alpha > 255 ) this.alpha = 255;
            }
            this.painter = createBackgroundPainter( (int)(this.alpha * this.veilAlpha) );
        }
        else if( !isBusy && this.alpha > 0 ) {
            if( this.shadeDelayTotal <= 0 ) {
                this.alpha = 0;
            }
            else {
                this.alpha-= 255 / (this.shadeDelayTotal / this.shadeDelayInterval );
            }
            
            if( this.alpha > 0 )
                this.painter = createBackgroundPainter( (int)(this.alpha * this.veilAlpha) );
            else {
                this.alpha = 0;
                this.painter = null;
            }
        }
        else {
            this.painter = null;
        }
        
        if( oldPainter != this.painter )
            updateUI();
    }    
    
    /** Overide the JXBusyLabel that must perform an updateUI() on the layer for
     *  repainting the animation
     */
    private class CustomBusyLabel extends JXBusyLabel {

        @Override
        protected void frameChanged() {
            BasicBusyLayerUI.this.updateUI();
        }
    }
}