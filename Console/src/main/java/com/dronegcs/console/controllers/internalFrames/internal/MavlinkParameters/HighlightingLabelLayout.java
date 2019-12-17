package com.dronegcs.console.controllers.internalFrames.internal.MavlinkParameters;

import com.dronegcs.console.controllers.dashboard.FloatingNodeManager;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class HighlightingLabelLayout extends Pane {

    private static final PseudoClass HIGHLIGHTED = PseudoClass.getPseudoClass("highlighted");

    private boolean needsRebuild = true ;

    private final StringProperty text = new StringPropertyBase() {

        @Override
        public String getName() {
            return "text" ;
        }

        @Override
        public Object getBean() {
            return HighlightingLabelLayout.this ;
        }

        @Override
        protected void invalidated() {
            super.invalidated();
            needsRebuild = true ;
            requestLayout();
        }
    };

    private final StringProperty highlightText = new StringPropertyBase() {

        @Override
        public String getName() {
            return "highlightText" ;
        }

        @Override
        public Object getBean() {
            return HighlightingLabelLayout.this ;
        }

        @Override
        protected void invalidated() {
            super.invalidated();
            needsRebuild = true ;
            requestLayout();
        }
    };

    public HighlightingLabelLayout() {
        //getStylesheets().add(getClass().getResource("com/dronegcs/console/application.css").toExternalForm());
    }

    public final StringProperty textProperty() {
        return this.text;
    }


    public final String getText() {
        return this.textProperty().get();
    }


    public final void setText(final String text) {
        this.textProperty().set(text);
    }


    public final StringProperty highlightTextProperty() {
        return this.highlightText;
    }


    @Override
    protected void layoutChildren() {
        if (needsRebuild) {
            rebuild() ;
        }
    }


    // Performance could probably be improved by caching and reusing the labels...
    private void rebuild() {
        String[] words = text.get().split("\\s");
        String highlight = highlightText.get();
        getChildren().clear();
        StringBuffer buffer = new StringBuffer();
        boolean addLeadingSpace = false ;
        for (int i = 0 ; i < words.length ; i++) {
            if (words[i].equals(highlight)) {
                if ( i > 0) {
                    getChildren().add(new Label(buffer.toString()));
                    buffer.setLength(0);
                }
                Label label = new Label(words[i]);
//                label.pseudoClassStateChanged(HIGHLIGHTED, true);
                label.setStyle("-fx-background-color: yellow ;");
                addLeadingSpace = true ;
                getChildren().add(label);
            } else {
                if (addLeadingSpace) {
                    buffer.append(' ');
                }
                buffer.append(words[i]);
                if (i < words.length - 1) {
                    buffer.append(' ');
                }
                addLeadingSpace = false ;
            }
        }
        if (buffer.length() > 0) {
            getChildren().add(new Label(buffer.toString()));
        }

        needsRebuild = false ;
    }


}
