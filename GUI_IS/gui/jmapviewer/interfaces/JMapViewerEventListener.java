// License: GPL. For details, see Readme.txt file.
package gui.jmapviewer.interfaces;

import gui.jmapviewer.events.JMVCommandEvent;

import java.util.EventListener;

/**
 * Must be implemented for processing commands while user
 * interacts with map viewer.
 *
 * @author Jason Huntley
 *
 */
public interface JMapViewerEventListener extends EventListener {
    void processCommand(JMVCommandEvent command);
}
