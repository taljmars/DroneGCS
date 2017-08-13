package com.dronegcs.console.controllers.internalPanels.internal;

import com.dronegcs.console.controllers.EditingCell;
import javafx.util.StringConverter;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ColumnTypeAwareEditingCell<TE extends ReferredTableEntry,T> extends EditingCell<TE,T> {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ColumnTypeAwareEditingCell.class);

    private List<Class> legelClass;
    private List<Class> ignoreClass;
    private PostCommit postAction;
    private String getter;
    private String setter;

    public ColumnTypeAwareEditingCell(List<Class> ignoreClass, List<Class> legelClass, StringConverter<T> converter ,
                                      String setter, String getter, PostCommit postAction) {
        super(converter);
        this.ignoreClass = ignoreClass;
        this.legelClass = legelClass;
        this.postAction = postAction;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void startEdit() {
        TE entry = getTableView().getItems().get( getIndex() );
        if (isIgnored(entry) || !isPermit(entry))
            return;
        super.startEdit();
    }

    // null all all, empty allow everything
    private boolean isIgnored(TE entry) {
        if (ignoreClass == null) {
            return false;
        }

        for (Class clz : ignoreClass) {
            if (clz.getClass().getName().equals(entry.getClass().getName())) {
            //if (clz.isInstance(entry.getReferredItem())) {
                return true;
            }
        }
        return false;
    }

    // null allows all, empty allows nothing
    private boolean isPermit(TE entry) {
        if (legelClass == null) {
            return true;
        }

        for (Class clz : legelClass) {
            if (clz.getClass().getName().equals(entry.getClass().getName())) {
            //if (clz.isInstance(entry.getReferredItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateItem( T item, boolean empty ) {
        super.updateItem( item, empty );
        if ( !empty && getIndex() > 0 ) {
            TE entry = getTableView().getItems().get( getIndex() );
            LOGGER.debug("TALMA {}" , entry.getReferredItem());
            if (isIgnored(entry) || !isPermit(entry)) {
                setText(null);
                setGraphic(null);
                return;
            }

            if (!isEditing()) {
                T val = null;
                try {
                    Method method = entry.getReferredItem().getClass().getMethod(getter, null);
                    val = (T) method.invoke(entry.getReferredItem());
                    LOGGER.debug("TALMA call getter '{}', val '{}'", getter, val);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    LOGGER.error("Failed to handle altitude change", e);
                }
                setText("" + val);
            }
        }
    }

    @Override
    public boolean preCommit(T newValue) {
//        super.commitEdit(newValue);
        //post editing
        TE entry = getTableView().getItems().get( getIndex() );
        if (isIgnored(entry) || !isPermit(entry))
            return false;

        try {
            // Updating object
            Method method = entry.getReferredItem().getClass().getMethod(setter, newValue.getClass());
            method.invoke(entry.getReferredItem(), converter.fromString(getString()));
            LOGGER.debug("TALMA updating value to '{}', '{}'", getString(), setter);

            if (!postAction.call(entry.getReferredItem()))
                return false;

            return true;
        }
        catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            LOGGER.error("Failed to handle '{}' change", setter, e);
            return false;
        }
    }

    interface PostCommit<TE> {
        boolean call(TE entry);
    }
}
