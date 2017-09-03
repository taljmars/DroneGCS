package com.dronegcs.console.controllers.internalPanels.internal;

import com.dronegcs.console.controllers.EditingCell;
import javafx.util.StringConverter;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ColumnTypeAwareEditingCell<TE extends ReferredTableEntry,T> extends EditingCell<TE,T> {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ColumnTypeAwareEditingCell.class);

    private List<Class> legalClass;
    private List<Class> ignoreClass;
    private PostCommit postAction;
    private String getter;
    private String setter;

    public ColumnTypeAwareEditingCell(List<Class> ignoreClass, List<Class> legalClass, StringConverter<T> converter ,
                                      String setter, String getter, PostCommit postAction) {
        super(converter);
        this.ignoreClass = ignoreClass;
        this.legalClass = legalClass;
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
            LOGGER.debug("{} is NOT ignored for {}", entry, this);
            return false;
        }

        for (Class clz : ignoreClass) {
            if (clz.getName().equals(entry.getReferredItem().getClass().getName())) {
            //if (clz.isInstance(entry.getReferredItem())) {
                LOGGER.debug("{} is ignored for {}", entry, this);
                return true;
            }
        }

        LOGGER.debug("{} is NOT ignored for {}", entry, this);
        return false;
    }

    // null allows all, empty allows nothing
    private boolean isPermit(TE entry) {
        if (legalClass == null) {
            LOGGER.debug("{} is permitted for {}", entry, this);
            return true;
        }

        for (Class clz : legalClass) {
//            LOGGER.debug("Comparing {} and {}", clz.getName(), entry.getReferredItem().getClass().getName());
            if (clz.getName().equals(entry.getReferredItem().getClass().getName())) {
            //if (clz.isInstance(entry.getReferredItem())) {
                LOGGER.debug("{} is permitted for {}", entry, this);
                return true;
            }
        }

        LOGGER.debug("{} is NOT permitted for {}", entry, this);
        return false;
    }

    @Override
    public void updateItem( T item, boolean empty ) {
        super.updateItem( item, empty );
        if ( !empty && getIndex() >= 0 ) {
            TE entry = getTableView().getItems().get( getIndex() );
            LOGGER.debug("TALMA {} ColumnTypeAddress {}" , entry.getReferredItem(), this);
            if (isIgnored(entry) || !isPermit(entry)) {
                LOGGER.debug("TALMA not call getter");
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
            else {
                LOGGER.debug("TALMA not call getter - not editing");
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
            method.invoke(entry.getReferredItem(), converter.fromString(newValue.toString()));
            LOGGER.debug("TALMA updating value to '{}', '{}'", newValue, setter);

            if (!postAction.call(entry.getReferredItem())) {
                LOGGER.debug("TALMA failed to call getter");
                return false;
            }

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
