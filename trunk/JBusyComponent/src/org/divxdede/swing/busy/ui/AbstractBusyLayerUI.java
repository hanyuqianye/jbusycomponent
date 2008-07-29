package org.divxdede.swing.busy.ui;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.divxdede.swing.busy.BusyModel;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;

/**
 * Abstract implementation of <code>BusyLayerUI</code>.
 * <p>
 * This implementation subclass <code>LockableUI</code> for protecting the view
 * across any access during the <code>busy</code> state.
 * <p>
 * <code>setBusyModel</code> and <code>getBusyModel</code> are provided with
 * default implementation that use the final <code>updateUI</code> method.
 * <p>
 * You must override <code>updateUIImpl</code> method for complete the layer update
 * when needed. This method is called for any changes on the model or ui.
 * 
 * @author André Sébastien
 */
public abstract class AbstractBusyLayerUI extends LockableUI implements BusyLayerUI {
    
    /** Busy model
     */
    private BusyModel model = null;
        
    /** Model listener
     */
    private ChangeListener modelListener = null;
    
    /** Default constructor
     */
    public AbstractBusyLayerUI() {
        this.modelListener = createModelListener();
    }
    
    /** 
     *  Define the BusyModel used by this ui
     *  @param model New BusyModel to use by this ui
     */
    public void setBusyModel( BusyModel model ) {
        
        BusyModel oldValue = this.getBusyModel();
        if( getBusyModel() != null ) {
            getBusyModel().removeChangeListener( this.modelListener );
        }
        
        this.model = model;
        
        if( getBusyModel() != null ) {
            getBusyModel().addChangeListener( this.modelListener );
            updateUI();
        }
    }
    
    /** 
     *  Returns the BusyModel used by this ui
     *  @return BusyModel used by this ui
     */    
    public BusyModel getBusyModel() {
        return this.model;
    }
    
    /** Internal "update" of this UI.
     *  This method should update this layer ui from the BusyModel properties.
     */
    protected final void updateUI() {
       if( SwingUtilities.isEventDispatchThread() ) updateUIImpl();
       else {
           Runnable doRun = new Runnable() {
               public void run() {
                   updateUIImpl();
               }
           };
           SwingUtilities.invokeLater(doRun);
       }
    }
    
    @Override
    public void updateUI(JXLayer<JComponent> l) {
        this.updateUI();
        super.updateUI(l);
    }    
    
    /** Overridable method for customize updateUI()
     */
    protected void updateUIImpl() {
        setLocked( shouldLock() );
        setDirty(true);
    }
    
    /** Indicate if this layer should be placed in a locked state.
     *  This default implementation lock the layer when the model is <code>busy</code>.
     */
    protected boolean shouldLock() {
        return getBusyModel() == null ? false : getBusyModel().isBusy();
    }
    
    /** Returns the ModelListener usable by this UI
     */
    private ChangeListener createModelListener() {
        return new ChangeListener() {
           public void stateChanged(ChangeEvent e) {
              updateUI();
           }
        };
    }    
}