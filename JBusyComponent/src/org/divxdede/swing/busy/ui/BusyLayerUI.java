package org.divxdede.swing.busy.ui;

import org.divxdede.swing.busy.*;
import org.jdesktop.jxlayer.plaf.LayerUI;

/**
 * LayerUI for JXLayer API providing <strong>busy</strong> feature 
 * to any swing components.
 * <p>
 * A BusyLayerUI <strong>must</strong> subclass LayerUI.<br>
 * But <code>LayerUI</code> is a class and this interface can't formalize this
 * specification anyway.
 * 
 * @see LayerUI
 * @author André Sébastien
 */
public interface BusyLayerUI {

    /** 
     *  Define the BusyModel used by this ui
     *  @param model New BusyModel to use by this ui
     */
    public void setBusyModel(BusyModel model);
    
    /** 
     *  Returns the BusyModel used by this ui
     *  @return BusyModel used by this ui
     */
    public BusyModel getBusyModel();
}