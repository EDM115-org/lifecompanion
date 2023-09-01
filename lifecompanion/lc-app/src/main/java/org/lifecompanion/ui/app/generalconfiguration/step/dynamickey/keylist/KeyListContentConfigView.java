/*
 * LifeCompanion AAC and its sub projects
 *
 * Copyright (C) 2014 to 2019 Mathieu THEBAUD
 * Copyright (C) 2020 to 2021 CMRRF KERPAPE (Lorient, France)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lifecompanion.ui.app.generalconfiguration.step.dynamickey.keylist;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.lifecompanion.controller.configurationcomponent.dynamickey.KeyListController;
import org.lifecompanion.controller.editaction.KeyListActions;
import org.lifecompanion.controller.editmode.ConfigActionController;
import org.lifecompanion.controller.resource.GlyphFontHelper;
import org.lifecompanion.framework.commons.translation.Translation;
import org.lifecompanion.framework.commons.ui.LCViewInitHelper;
import org.lifecompanion.framework.utils.Pair;
import org.lifecompanion.model.api.configurationcomponent.dynamickey.KeyListNodeI;
import org.lifecompanion.model.impl.constant.LCGraphicStyle;
import org.lifecompanion.model.impl.notification.LCNotification;
import org.lifecompanion.ui.controlsfx.glyphfont.FontAwesome;
import org.lifecompanion.ui.notification.LCNotificationController;
import org.lifecompanion.util.javafx.FXControlUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class KeyListContentConfigView extends VBox implements LCViewInitHelper {
    private final ObjectProperty<KeyListNodeI> root;

    private boolean dirty;
    private final ObjectProperty<KeyListNodeI> cutOrCopiedNode;

    private Button buttonDelete, buttonMoveUp, buttonMoveDown, buttonCut, buttonCopy, buttonPaste, buttonExportKeys, buttonImportKeys, buttonShowHideProperties;

    private KeyListNodePropertiesEditionView keyListNodePropertiesEditionView;

    private final BooleanProperty propertiesShowing;

    private final ObjectProperty<KeyListNodeI> selected;
    private final ObjectProperty<KeyListNodeI> currentList;

    private KeyListContentPane keyListContentPane;
    private KeyListSelectionSearchView searchView;

    public KeyListContentConfigView() {
        this.root = new SimpleObjectProperty<>();
        this.selected = new SimpleObjectProperty<>();
        this.currentList = new SimpleObjectProperty<>();
        this.cutOrCopiedNode = new SimpleObjectProperty<>();
        this.propertiesShowing = new SimpleBooleanProperty(true);
        initAll();
    }


    ReadOnlyObjectProperty<KeyListNodeI> selectedProperty() {
        return selected;
    }

    ReadOnlyObjectProperty<KeyListNodeI> currentListProperty() {
        return currentList;
    }

    public ObjectProperty<KeyListNodeI> rootProperty() {
        return root;
    }

    /**
     * Implementation note : will true only if structural changes are made (add, remove, move) and false if only node properties are changed (for easier dev)
     *
     * @return true if the edited key list nodes were modified.<br>
     */
    public boolean isDirty() {
        return dirty;
    }

    void markDirty() {
        this.dirty = true;
    }

    // UI
    //========================================================================
    @Override
    public void initUI() {
        // Right : actions buttons
        buttonMoveUp = createActionButton(FontAwesome.Glyph.CHEVRON_UP, LCGraphicStyle.MAIN_PRIMARY, "tooltip.keylist.button.move.up");
        buttonMoveDown = createActionButton(FontAwesome.Glyph.CHEVRON_DOWN, LCGraphicStyle.MAIN_PRIMARY, "tooltip.keylist.button.move.down");
        buttonCopy = createActionButton(FontAwesome.Glyph.COPY, LCGraphicStyle.MAIN_PRIMARY, "tooltip.keylist.button.copy");
        buttonCut = createActionButton(FontAwesome.Glyph.CUT, LCGraphicStyle.MAIN_PRIMARY, "tooltip.keylist.button.cut");
        buttonPaste = createActionButton(FontAwesome.Glyph.PASTE, LCGraphicStyle.MAIN_DARK, "tooltip.keylist.button.paste");
        buttonDelete = createActionButton(FontAwesome.Glyph.TRASH, LCGraphicStyle.SECOND_DARK, "tooltip.keylist.button.delete");

        // Command buttons
        VBox boxActionButtons = new VBox(2.0, buttonDelete, buttonCopy, buttonCut, buttonPaste, new Separator(Orientation.HORIZONTAL), buttonMoveUp, buttonMoveDown);
        boxActionButtons.setAlignment(Pos.CENTER);
        HBox.setHgrow(boxActionButtons, Priority.SOMETIMES);
        boxActionButtons.setMinWidth(40.0);

        KeyListTreeView keyListTreeView = new KeyListTreeView(this);
        keyListTreeView.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(keyListTreeView, Priority.SOMETIMES);

        keyListContentPane = new KeyListContentPane(this);
        keyListContentPane.setMaxWidth(Double.MAX_VALUE);
        keyListContentPane.setMaxHeight(Double.MAX_VALUE);
        HBox.setHgrow(keyListContentPane, Priority.ALWAYS);

        this.buttonExportKeys = FXControlUtils.createLeftTextButton(Translation.getText("general.configuration.view.key.list.button.export.keys"),
                GlyphFontHelper.FONT_AWESOME.create(FontAwesome.Glyph.UPLOAD).size(14).color(LCGraphicStyle.MAIN_DARK),
                null);
        this.buttonExportKeys.setMaxWidth(Double.MAX_VALUE);
        this.buttonExportKeys.setAlignment(Pos.CENTER);

        this.buttonImportKeys = FXControlUtils.createLeftTextButton(Translation.getText("general.configuration.view.key.list.button.import.keys"),
                GlyphFontHelper.FONT_AWESOME.create(FontAwesome.Glyph.DOWNLOAD).size(14).color(LCGraphicStyle.MAIN_DARK),
                null);
        this.buttonImportKeys.setMaxWidth(Double.MAX_VALUE);
        this.buttonImportKeys.setAlignment(Pos.CENTER);

        searchView = new KeyListSelectionSearchView(this);
        VBox.setMargin(searchView, new Insets(10, 0, 0, 0));

        HBox boxExportImportsButtons = new HBox(10.0, buttonImportKeys, buttonExportKeys);
        boxExportImportsButtons.setAlignment(Pos.CENTER);

        SplitPane splitPane = new SplitPane(keyListTreeView, keyListContentPane);
        splitPane.setDividerPositions(0.3);
        HBox.setHgrow(splitPane, Priority.ALWAYS);
        HBox boxTreeAndCommands = new HBox(5.0, splitPane, boxActionButtons);
        boxTreeAndCommands.setAlignment(Pos.CENTER);
        VBox.setVgrow(boxTreeAndCommands, Priority.ALWAYS);

        buttonShowHideProperties = FXControlUtils.createLeftTextButton(Translation.getText("keylist.config.hide.key.properties"),
                GlyphFontHelper.FONT_AWESOME.create(FontAwesome.Glyph.EYE_SLASH).size(18).color(LCGraphicStyle.MAIN_DARK),
                null);
        buttonShowHideProperties.setAlignment(Pos.CENTER_RIGHT);

        keyListNodePropertiesEditionView = new KeyListNodePropertiesEditionView();
        keyListNodePropertiesEditionView.setPrefHeight(320.0);
        keyListNodePropertiesEditionView.setMinHeight(150.0);

        // Total
        this.setSpacing(2.0);
        this.getChildren().addAll(boxExportImportsButtons, searchView, new KeyListSelectionPathView(this), boxTreeAndCommands, buttonShowHideProperties, keyListNodePropertiesEditionView);
        this.setAlignment(Pos.CENTER);
    }

    private Button createActionButton(FontAwesome.Glyph glyph, Color color, String tooltip) {
        return createActionButton(GlyphFontHelper.FONT_AWESOME.create(glyph).size(22).color(color), tooltip);
    }

    private Button createActionButton(Node graphics, String tooltip) {
        final Button button = FXControlUtils.createGraphicButton(graphics, tooltip);
        button.setAlignment(Pos.CENTER);
        GridPane.setHalignment(button, HPos.CENTER);
        return button;
    }
    //========================================================================


    // LISTENER
    //========================================================================
    @Override
    public void initListener() {
        this.buttonMoveUp.setOnAction(createMoveNodeListener(-1));
        this.buttonMoveDown.setOnAction(createMoveNodeListener(+1));
        this.buttonDelete.setOnAction(e -> ifSelectedItemNotNull(selectedNode -> removeNode(selectedNode, "keylist.action.removed.action.notification.title")));
        this.buttonCopy.setOnAction(e -> ifSelectedItemNotNull(selectedNode -> {
            KeyListNodeI duplicated = (KeyListNodeI) selectedNode.duplicate(true);
            cutOrCopiedNode.set(duplicated);
        }));
        this.buttonCut.setOnAction(e -> ifSelectedItemNotNull(selectedNode -> {
            cutOrCopiedNode.set(selectedNode);
            removeNode(selectedNode, "keylist.action.cut.action.notification.title");
        }));
        this.buttonPaste.setOnAction(e -> {
            final KeyListNodeI toPaste = cutOrCopiedNode.get();
            if (toPaste != null) {
                cutOrCopiedNode.set(null);
                keyListContentPane.addNode(toPaste);
            }
        });
        this.buttonExportKeys.setOnAction(e ->
                ConfigActionController.INSTANCE.executeAction(new KeyListActions.ExportKeyListsAction(buttonExportKeys, this.root.get())));
        this.buttonImportKeys.setOnAction(e ->
                ConfigActionController.INSTANCE.executeAction(new KeyListActions.ImportKeyListsAction(buttonImportKeys, toAdd -> keyListContentPane.addNode(toAdd))));

        buttonShowHideProperties.setOnAction(e -> toggleProperties());

        List<Pair<KeyCombination, Button>> keyCombination = Arrays.asList(
                Pair.of(new KeyCodeCombination(KeyCode.DELETE), buttonDelete),
                Pair.of(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN), buttonCopy),
                Pair.of(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN), buttonCut),
                Pair.of(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN), buttonPaste),
                Pair.of(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), keyListContentPane.getButtonAddKey()),
                Pair.of(new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN), buttonMoveUp),
                Pair.of(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN), buttonMoveDown)
        );
        this.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            for (Pair<KeyCombination, Button> handler : keyCombination) {
                Button button = handler.getRight();
                if (!button.isDisabled() && handler.getLeft().match(keyEvent)) {
                    button.getOnAction().handle(null);
                    break;
                }
            }
        });
        this.keyListNodePropertiesEditionView.setAddRequestListener(() -> keyListContentPane.getButtonAddKey().fire());
    }


    private void removeNode(KeyListNodeI selectedNode, String notificationTitle) {
        markDirty();
        // Important to get the name before remove (as the level will be incorrect after)
        String keyName = selectedNode.getHumanReadableText();

        final KeyListNodeI parentNode = selectedNode.parentProperty().get();
        int previousIndex = parentNode.getChildren().indexOf(selectedNode);
        parentNode.getChildren().remove(selectedNode);

        clearSelection();
        searchView.executeSearch(true);

        LCNotificationController.INSTANCE.showNotification(LCNotification.createInfo(Translation.getText(notificationTitle, keyName),
                true,
                "keylist.action.remove.cancel",
                () -> {
                    if (!parentNode.getChildren().contains(selectedNode)) {
                        if (previousIndex > 0 && previousIndex <= parentNode.getChildren().size()) {
                            parentNode.getChildren().add(previousIndex, selectedNode);
                        } else {
                            parentNode.getChildren().add(0, selectedNode);
                        }
                        select(selectedNode);
                    }
                }));
    }

    private EventHandler<ActionEvent> createMoveNodeListener(final int indexMove) {
        return ae -> {
            ifSelectedItemNotNull(selectedNode -> {
                markDirty();
                final ObservableList<KeyListNodeI> children = selectedNode.parentProperty().get().getChildren();
                int index = children.indexOf(selectedNode);
                if (index + indexMove >= 0 && index + indexMove < children.size()) {
                    Collections.swap(children, index, index + indexMove);
                    select(selectedNode);
                }
            });
        };
    }

    private void ifSelectedItemNotNull(Consumer<KeyListNodeI> action) {
        if (selected.get() != null) {
            action.accept(selected.get());
        }
    }
    //========================================================================

    // PROP PANEL
    //========================================================================
    private void toggleProperties() {
        if (this.propertiesShowing.get()) hideProperties();
        else showProperties();
    }

    private void hideProperties() {
        keyListNodePropertiesEditionView.setVisible(false);
        keyListNodePropertiesEditionView.setManaged(false);
        buttonShowHideProperties.setText(Translation.getText("keylist.config.show.key.properties"));
        this.propertiesShowing.set(false);
    }

    private void showProperties() {
        keyListNodePropertiesEditionView.setVisible(true);
        keyListNodePropertiesEditionView.setManaged(true);
        buttonShowHideProperties.setText(Translation.getText("keylist.config.hide.key.properties"));
        this.propertiesShowing.set(true);
    }
    //========================================================================

    // BINDING
    //========================================================================
    @Override
    public void initBinding() {
        this.root.addListener((obs, ov, nv) -> {
            cutOrCopiedNode.set(null);
            this.dirty = false;
            selected.set(null);
            currentList.set(nv);
        });
        this.keyListNodePropertiesEditionView.selectedNodeProperty().bind(selected);
        this.buttonPaste.disableProperty().bind(this.cutOrCopiedNode.isNull());
        this.buttonCopy.disableProperty().bind(selected.isNull());
        this.buttonCut.disableProperty().bind(selected.isNull());
        this.buttonMoveUp.disableProperty().bind(selected.isNull());
        this.buttonMoveDown.disableProperty().bind(selected.isNull());
        this.buttonDelete.disableProperty().bind(selected.isNull());

    }
    //========================================================================


    // NAVIGATION
    //========================================================================
    public void select(KeyListNodeI item) {
        if (item != null && item.parentProperty().get() != null && currentList.get() != item.parentProperty().get()) {
            currentList.set(item.parentProperty().get());
        }
        selected.set(item);
    }

    public void selectById(String itemId) {
        if (itemId != null && root.get() != null) {
            final KeyListNodeI foundNode = KeyListController.findNodeByIdInSubtree(root.get(), itemId);
            if (foundNode != null) {
                select(foundNode);
            }
        }
    }

    public void clearSelection() {
        selected.set(null);
    }

    public void openList(KeyListNodeI item) {
        currentList.set(item);
        selected.set(null);
    }

    public void goToParent() {
        KeyListNodeI nodeV = currentList.get();
        if (nodeV != null && nodeV.parentProperty().get() != null) {
            currentList.set(nodeV.parentProperty().get());
        }
    }
    //========================================================================
}
