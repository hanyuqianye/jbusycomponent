# Introduction #

`JBusyComponent` class is just a wrapper, you can use instead `JXLayer` directly.
We use `JXlayer` for manage a translucent layer (overlay) on any swing components.

The Busy animation (and all business methods) are implemented in the `JXLayer`'s UI.

The `BasicBusyLayerUI` maintain a `BusyModel` to let you control the busy state like any `JBusyComponent`

# Details #

```
>   // your component to enhance 
>   JComponent comp = ...; 
>
>
>   // Create the JXLayer decorator 
>   JXLayer<JComponent> layer = new JXLayer<JComponent>(comp); 
>
>
>   // Create the Busy Layer UI delegate 
>   BusyLayerUI ui = new BasicBusyLayerUI(); 
>
>
>   // Attach the UI to the decorator 
>   layer.setUI( (LayerUI)ui ); 
>
>
>   // Add the decorator to the container instead of our component 
>   myContainer.add( layer ); 
>
>
>   // Use the BusyModel for control the busy state on our component 
>   // If multiple components share the same BusyModel, all of theses will be 
>   // triggered by the same model 
>   BusyModel model = ui.getBusyModel(); 
>
>
>   model.setBusy(true); // an animation over our component is shown 
```