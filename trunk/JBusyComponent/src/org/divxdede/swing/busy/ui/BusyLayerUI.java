/*
 * 
 * Copyright (c) 2007 ANDRE Sébastien (divxdede).  All rights reserved.
 * BusyLayerUI.java is a part of this JBusyComponent library
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
    public void setBusyModel(final BusyModel model);
    
    /** 
     *  Returns the BusyModel used by this ui
     *  @return BusyModel used by this ui
     */
    public BusyModel getBusyModel();
}