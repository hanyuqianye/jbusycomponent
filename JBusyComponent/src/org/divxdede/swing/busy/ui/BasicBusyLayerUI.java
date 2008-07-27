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
    public BasicBusyLayerUI(int shadeDelay , int shadeFps , float veilAlpha  , Color veilColor) {
        
        this.cancelListener     = createCancelListener();
        this.timer              = createBackgroundShadingTimer();
                                  createGlassPane();
        
        
        this.shadeDelayTotal    = shadeDelay;
        this.shadeDelayInterval = ( shadeDelayTotal <= 0 ? 0 : (int)(1000f / shadeFps) );
        
        this.veilAlpha          = veilAlpha;
        this.veilColor          = veilColor;
    }
    
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        
        JXLayer layer = (JXLayer)c;
        layer.setGlassPane( this.jXGlassPane );
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        JXLayer layer = (JXLayer)c;
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
    public void setBusyPainter(BusyPainter busyPainter) {
        this.setBusyPainter(busyPainter,null);
    }
    
    /** 
     * Define the busy painter to use for render the friendly busy animation
     * @param busyPainter New busy painter to use for render the friendly busy animation
     * @param preferredSize Preferred Size to use for this painter animation
     */
    public void setBusyPainter(BusyPainter busyPainter , Dimension preferredSize ) {
        if( preferredSize != null ) {
            this.jXBusyLabel.setIcon( new EmptyIcon( preferredSize.width , preferredSize.height ) );
        }
        else
            this.jXBusyLabel.setIcon(null);
        
        this.jXBusyLabel.setBusyPainter( busyPainter );
        this.updateUI();
    }
    
    @Override
    protected void paintLayer(Graphics2D g2, JXLayer<JComponent> l) {
        super.paintLayer(g2, l);
        Painter painter = getBackGroundPainter();
        if( painter != null ) {
            painter.paint(g2, null , l.getWidth(), l.getHeight() );
        }
    }
    
    @Override
    protected void updateUIImpl() {
        BusyModel myModel = getBusyModel();
        boolean   isBusy  = myModel == null ? false : myModel.isBusy();

        /** Visible states
         */
        this.jXBusyLabel.setVisible( isBusy );
        this.jProgressBar.setVisible( isBusy && myModel.isDeterminate() );
        this.jXHyperlinkCancel.setVisible( isBusy && myModel.isCancellable() );

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
            this.jXBusyLabel.setText( getPercentProgressionString() );
        }
        super.updateUIImpl();
    }

    @Override
    public void setBusyModel(BusyModel model) {
        super.setBusyModel(model);
        
        if( getBusyModel() != null ) 
            jProgressBar.setModel( model );
    }

    /** Create our glasspane
     */
    private JComponent createGlassPane() {
        
        GridBagLayout layout = new GridBagLayout();
        jXGlassPane.setLayout( layout );
        jXGlassPane.setOpaque(false);

        Insets emptyInsets = new Insets(0,0,0,0);
        GridBagConstraints gbcLabel = new GridBagConstraints(1,1,2,1,0,0, GridBagConstraints.CENTER , GridBagConstraints.NONE , emptyInsets , 0 , 0 );
        GridBagConstraints gbcBar   = new GridBagConstraints(1,2,1,1,0,0, GridBagConstraints.CENTER , GridBagConstraints.NONE , emptyInsets , 0 , 0 );
        GridBagConstraints gbcLink  = new GridBagConstraints(2,2,1,1,0,0, GridBagConstraints.CENTER , GridBagConstraints.NONE , emptyInsets , 0 , 0 );
        
        jXGlassPane.add( jXBusyLabel , gbcLabel );
        jXGlassPane.add( jProgressBar , gbcBar );
        jXGlassPane.add( jXHyperlinkCancel , gbcLink );

        jXBusyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jXBusyLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jXBusyLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jXHyperlinkCancel.setText( UIManager.getString("OptionPane.cancelButtonText") );
        jXHyperlinkCancel.addActionListener( this.cancelListener );

        updateUI();
        return jXGlassPane;
    }
    
    /** Return the percent progression string
     */
    private String getPercentProgressionString() {
        BusyModel myModel = getBusyModel();
        boolean   isBusy  = myModel == null ? false : myModel.isBusy();
        if( ! isBusy )                 return null;
        if( !myModel.isDeterminate() ) return null;
        
        int   range   = myModel.getMaximum() - myModel.getMinimum();
        int   value   = myModel.getValue();
        float percent = ( 100f / (float)range ) * (float)( value - myModel.getMinimum() );
        
        return  ((int)percent) + " %";
    }
    
    /** Create the Listener managing the cancel action when click on the hyperlink
     */
    private ActionListener createCancelListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    getBusyModel().cancel();
                }
                catch(Exception e2) {
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
    protected Painter createBackgroundPainter( int alpha ) {
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
            if( timer.isRunning() ) return;
            timer.start();
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
        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                synchronized( BasicBusyLayerUI.this ) {
                    updateBackgroundPainter();
                    if( ! isBackgroundPainterDirty() ) {
                        System.out.println("STOPPING TIMER");
                        ((Timer)e.getSource()).stop();
                    }
                }
            }
        };
        return new Timer(shadeDelayInterval , actionListener );
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
        
        BusyModel myModel = getBusyModel();
        boolean   isBusy  = myModel == null ? false : myModel.isBusy();
        
        if( isBusy  && alpha < 255 ) return true;
        if( !isBusy && alpha > 0 )   return true;
        return false;
    }
    
    /** Update the painter for paint the next step of the shading.
     *  <p>
     *  This method request an updateUI() if a new painter is created.
     *  The method update the alpha of the white veil depending <code>shadeDelayTotal</code> delay
     *  and <code>shadeDelayInterval</code> delay
     */
    private synchronized void updateBackgroundPainter() {
        BusyModel myModel = getBusyModel();
        boolean   isBusy  = myModel == null ? false : myModel.isBusy();
        
        Painter oldPainter = this.painter;
        
        if( isBusy && alpha < 255 ) {
            if( shadeDelayTotal <= 0 ) {
                alpha = 255;
            }
            else {
                alpha += 255 / (shadeDelayTotal / shadeDelayInterval);
                if( alpha > 255 ) alpha = 255;
            }
            this.painter = createBackgroundPainter( (int)(alpha * veilAlpha) );
        }
        else if( !isBusy && alpha > 0 ) {
            if( shadeDelayTotal <= 0 ) {
                alpha = 0;
            }
            else {
                alpha-= 255 / (shadeDelayTotal / shadeDelayInterval );
            }
            
            if( alpha > 0 )
                this.painter = createBackgroundPainter( (int)(alpha * veilAlpha) );
            else {
                alpha = 0;
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