// License: GPL. For details, see Readme.txt file.
package gui.jmapviewer.checkBoxTree;

import java.io.Serializable;

import gui.jmapviewer.AbstractLayer;
import gui.jmapviewer.LayerGroup;

/**
 * Node Data for checkBox Tree
 *
 * @author galo
 */
public class CheckBoxNodeData implements Serializable /*TALMA add serilizebae*/{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1139254877798791745L;
	private AbstractLayer layer;

    public CheckBoxNodeData(final AbstractLayer layer) {
        this.layer = layer;
    }

    public CheckBoxNodeData(final String txt) {
        this(new LayerGroup(txt));
    }

    public CheckBoxNodeData(final String txt, final Boolean selected) {
        this(new LayerGroup(txt));
        layer.setVisible(selected);
    }

    public Boolean isSelected() {
        return layer.isVisible();
    }

    public void setSelected(final Boolean newValue) {
        layer.setVisible(newValue);
    }

    public String getText() {
        return layer.getName();
    }

    public AbstractLayer getAbstractLayer() {
        return layer;
    }

    public void setAbstractLayer(final AbstractLayer layer) {
        this.layer = layer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getText() + '/' + isSelected() + ']';
    }
}
