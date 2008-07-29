package org.divxdede.swing.busy;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import org.divxdede.swing.busy.ui.BusyLayerUI;
import org.divxdede.swing.busy.ui.BasicBusyLayerUI;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.LayerUI;

/**
 * Component decorator that enhance <strong>any swing components</strong> with
 * <strong>busy</strong> feature.
 * <p>
 * This decorator enhance a view (swing component) that provide a smart animation
 * when it's view is busy and restrict any acces to it.
 * The decorator take parts on the components hierarchy and must be added to the 
 * container instead of the original component that is now a simple view of the 
 * <code>JBusyComponent</code>
 * <p>
 * Your component still the same as before and keep all of theses features and
 * behaviour. The main difference is that now you can refer and use a 
 * <code>BusyModel</code> from the <code>JBusyComponent</code> decorator.<br>
 * This model allow you to control the <strong>busy property</strong> and some
 * other related informations.
 * <p>
 * Typically, a busy component is locked (can't be accessed anymore) and show
 * on an overlay a smart animation showing this busy state. Regarding the 
 * <code>BusyModel</code> configuration, you can have also a progress bar (if
 * the <code>BusyModel</code> is on a determinate state) and/or a cancel button 
 * (if the <code>BusyModel</code> is cancellable</code).
 * <p>
 * <code>JBusyComponent</code> is at the top of this API.
 * But in fact, it's just a wrapper of <code>JXLayer</code> and a <code>LayerUI</code> implementation.<br>
 * All business implementation are done by the <code>LayerUI</code> and you can use directly
 * a <code>JXLayer</code> instead of a <code>JBusyComponent</code>.
 * <p>
 * This is a little example:
 * <pre>
 *      // your component to enhance
 *      JTree comp = .....;
 * 
 *      // Create the JBusyComponent enhancer
 *      JBusyComponent<JTree> busyComp = new JXLayer<JTree>(comp);
 * 
 *      // Add our JBusyComponent to the container instead of our component
 *      myContainer.add( layer );
 * 
 *      // Use the BusyModel for control the busy state on our component
 *      BusyModel model = busyComp.getBusyModel();
 * 
 *      // Let's got to put our original component to a busy state
 *      model.setBusy(true); // an animation over our component is shown
 * </pre>
 * 
 * @see BusyModel
 * @see JXLayer
 * @see BusyLayerUI
 * @author André Sébastien
 */
public class JBusyComponent<C extends JComponent> extends JComponent {

    /** Members
     */
    private JXLayer<JComponent> layer = null;
    private BusyLayerUI         ui    = null;
    
    /** 
     * Create a <code>JBusyComponent</code> with no view
     * @see #setView
     */
    public JBusyComponent() {
        this(null);
    }
    
    /** 
     * Create a <code>JBusyComponent</code> with a specified view
     * @param view The view of this component
     */
    public JBusyComponent( C view ) {
        this( view , new BasicBusyLayerUI() );
    }
    
    /** 
     * Create a <code>JBusyComponent</code> with a specified view and a BusyLayerUI
     * @param view The view of this component
     * @param ui The ui of this component
     */
    public JBusyComponent( C view  , BusyLayerUI ui) {
        
        /** Create the layer
         */
        this.layer = new JXLayer<JComponent>(view);
        
        /** Configure it's fixed contents
         */
        super.setLayout( new BorderLayout() );
        super.add( this.layer );
        super.setOpaque(false);
        
        /** Install the UI
         */
        setBusyLayerUI(ui);

        /** Create a default model
         */
        setBusyModel( new DefaultBusyModel() );
    }
    
    /** 
     * Returns the View of this JBusyComponent
     * @return the underlying view of this JBusyComponent
     */
    public C getView() {
        return (C)this.layer.getView();
    }
    
    /**
     * Define the view of this JBusyComponent
     * @param view the new view of this component
     */
    public void setView(C view) {
        this.layer.setView(view);
    }
    
    /** 
     * Returns the BusyLayerUI used by this component.
     * @returns BusyLayerUI used by this component, this ui subclass LayerUI
     * @see LayerUI
     */
    public BusyLayerUI getBusyLayerUI() {
        return this.ui;
    }
    
    /** 
     * Define which BusyLayerUI this component must used for render the "busy" state
     * @param newUI New BusyLayerUI to use
     */
    public void setBusyLayerUI( BusyLayerUI newUI) {
        
        if( newUI == null )  newUI = new BasicBusyLayerUI();
        else {
            if( ! (newUI instanceof LayerUI ) ) {
                throw new IllegalArgumentException("newUI must subclass LayerUI");
            }
        }

        /** Keep track to our "Busy Model"
         */
        BusyModel model = null;
        if( getBusyLayerUI() != null ) {
            model = getBusyLayerUI().getBusyModel();
            getBusyLayerUI().setBusyModel(null);
        }

        /** Change the UI
         */
        this.ui = newUI;
        this.layer.setUI( (LayerUI)getBusyLayerUI() );
        
        /** Update the BusyLayerUI with our "Busy Model"
         */
        if( getBusyLayerUI() != null ) {
            getBusyLayerUI().setBusyModel(model);
        }
    }
    
    /** 
     *  Define the BusyModel used by this component
     *  @param model New BusyModel to use by this component
     */
    public void setBusyModel(BusyModel model) {
        BusyLayerUI myUI = getBusyLayerUI();
        if( myUI == null ) throw new IllegalStateException("Can't set a BusyModel on a JBusyComponent without a BusyLayerUI");
        myUI.setBusyModel( model );
    }
    
    /** 
     *  Returns the BusyModel used by this component
     *  @return BusyModel used by this component
     */
    public BusyModel getBusyModel() {
        BusyLayerUI myUI = getBusyLayerUI();
        if( myUI == null ) return null;
        
        return myUI.getBusyModel();
    }
    
    /**
     * Returns <code>true</code> if this component is on a busy state
     * @return <code>true</code> if this component is on a busy state
     */
    public boolean isBusy() {
        return getBusyModel() != null && getBusyModel().isBusy();
    }
    
    /** 
     *  Define if this component is in a busy state or not
     *  @param value <code>true</code> value set this component in a busy state
     */
    public void setBusy(boolean value) {
        BusyModel model = getBusyModel();
        if( model != null ) model.setBusy(value);
    }  
    
    @Override
    public Component add(Component comp) {
        throw new UnsupportedOperationException("JBusyComponent.add() is not supported.");
    }    
    
    @Override
    public void remove(Component comp) {
        throw new UnsupportedOperationException("JBusyComponent.remove(Component) is not supported.");
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException("JBusyComponent.removeAll() is not supported.");
    }    
}
