package com.cryo.cache.loaders.interfaces;

/**
 * Interface tool
 * paolo 04/08/2019
 * #Shnek6969
 */
public class ContainerHelper {

    /**
     * checks if the component has the scrollbar script
     * @param component
     * @return
     */
    public static boolean isScrollBar(IComponentDefinitions component){
        if(component.onLoadScript!= null && Integer.parseInt(component.onLoadScript[0].toString()) == 30)
            return true;
        return false;
    }

    /**
     * checks if the component is a button
     * @param component
     * @return
     */
    public static boolean isButton(IComponentDefinitions component){
        if(component.onLoadScript!= null && Integer.parseInt(component.onLoadScript[0].toString()) == 92)
            return true;
        return false;
    }
}
