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

package org.lifecompanion.ui.common.pane.specific.selectionmode;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.lifecompanion.ui.controlsfx.control.ToggleSwitch;
import org.lifecompanion.model.api.configurationcomponent.SelectionModeUserI;
import org.lifecompanion.model.api.selectionmode.FireEventInput;
import org.lifecompanion.model.api.selectionmode.ProgressDrawMode;
import org.lifecompanion.model.api.selectionmode.ScanningMode;
import org.lifecompanion.ui.common.pane.specific.cell.FireEventInputListCell;
import org.lifecompanion.ui.common.pane.specific.cell.ProgressDrawModeSimpleListCell;
import org.lifecompanion.ui.common.pane.specific.cell.ScanningModeSimpleListCell;
import org.lifecompanion.util.javafx.FXControlUtils;
import org.lifecompanion.model.impl.selectionmode.SelectionModeEnum;
import org.lifecompanion.ui.app.generalconfiguration.GeneralConfigurationStepViewI;
import org.lifecompanion.ui.common.pane.generic.BaseConfigurationViewBorderPane;
import org.lifecompanion.ui.common.control.specific.KeyCodeSelectorControl;
import org.lifecompanion.ui.common.control.generic.MouseButtonSelectorControl;
import org.lifecompanion.ui.common.control.generic.colorpicker.LCColorPicker;
import org.lifecompanion.framework.commons.translation.Translation;
import org.lifecompanion.framework.commons.ui.LCViewInitHelper;

import java.util.Arrays;
import java.util.List;

public class SelectionModeSuppParamView extends BaseConfigurationViewBorderPane<SelectionModeUserI> implements LCViewInitHelper {

    /**
     * Change draw progress mode
     */
    private ComboBox<ProgressDrawMode> comboboxDrawProgressMode;

    /**
     * Change scanning mode
     */
    private ComboBox<ScanningMode> comboBoxScanningMode;

    /**
     * Enable/disable auto start
     */
    private ToggleSwitch toggleStartScanningOnClic;

    private ToggleSwitch toggleBackgroundReductionEnabled;


    /**
     * Color pickers
     */
    private LCColorPicker colorPickerSelection, colorPickerActivation, colorPickerProgressColor, colorPickerVirtualCursorColor;

    /**
     * Slider for the selection view size
     */
    private Slider sliderSelectionViewSize;

    /**
     * Slider for the selection progress bar size
     */
    private Slider sliderProgressBarSize;

    private Slider sliderBackgroundReductionLevel;

    private Slider sliderVirtualCursorSize;

    /**
     * Spinner for scan pause, and pause on first
     */
    private Spinner<Double> spinnerScanPause, spinnerFirstPause;

    /**
     * Spinner for max in same part
     */
    private Spinner<Integer> spinnerMaxScan;

    /**
     * To enable/disable properties
     */
    private ToggleSwitch toggleEnableProgressDrawing, toggleSkipEmptyCells, toggleManifyKeyOver, toggleEnableActivationWithSelection, toggleEnableDirectSelectionOnMouseOnScanningSelectionMode, toggleHideMouseCursor, toggleShowVirtualCursor, toggleEnableAutoActivation;


    private List<Node> keyboardControlNodes;
    private List<Node> mouseNodes;
    private ComboBox<FireEventInput> comboBoxNextScanEventInput;
    private KeyCodeSelectorControl keySelectorControlKeyboardNextScanKeyCode;
    private MouseButtonSelectorControl mouseButtonSelectorControl;

    /**
     * Spinner for auto clic method
     */
    private Spinner<Double> spinnerAutoActivation, spinnerAutoOver;

    private Label labelSelectedMode;

    private Label labelSelectionModeSelectedDesc, titlePartActivation, labelTimeActivation, labelTimeOver, titlePartScanning, titlePartManualScanning, labelScanPause, labelScanFirstPause,
            labelScanMaxSame, titlePartStyle, labelColorSelection, labelColorActivation, labelSelectionViewSize, titlePartProgressStyle, labelProgressColor, labelDrawProgressMode,
            labelProgressBarSize, labelScanningMode, labelNextScanEventInput, labelKeyboardNextScanKey, titlePartAutoScanning, labelBackgroundReductionLevel, labelMouseButton, labelVirtualCursorSize, titlePartVirtualCursor, labelVirtualCursorColor;

    private final ObjectProperty<SelectionModeEnum> selectedMode;

    private GridPane gridPaneConfiguration;
    private boolean dirty;

    public SelectionModeSuppParamView() {
        this.selectedMode = new SimpleObjectProperty<>();
        initAll();
    }

    @Override
    public void bind(SelectionModeUserI model) {
        this.colorPickerActivation.setValue(model.getSelectionModeParameter().selectionActivationViewColorProperty().get());
        this.colorPickerSelection.setValue(model.getSelectionModeParameter().selectionViewColorProperty().get());
        this.spinnerScanPause.getValueFactory().setValue(model.getSelectionModeParameter().scanPauseProperty().get() / 1000.0);
        this.spinnerFirstPause.getValueFactory().setValue(model.getSelectionModeParameter().scanFirstPauseProperty().get() / 1000.0);
        this.toggleEnableProgressDrawing.setSelected(model.getSelectionModeParameter().drawProgressProperty().get());
        this.colorPickerProgressColor.setValue(model.getSelectionModeParameter().progressViewColorProperty().get());
        this.spinnerMaxScan.getValueFactory().setValue(model.getSelectionModeParameter().maxScanBeforeStopProperty().get());
        this.toggleManifyKeyOver.setSelected(model.getSelectionModeParameter().manifyKeyOverProperty().get());
        this.toggleSkipEmptyCells.setSelected(model.getSelectionModeParameter().skipEmptyComponentProperty().get());
        this.comboboxDrawProgressMode.getSelectionModel().select(model.getSelectionModeParameter().progressDrawModeProperty().get());
        this.spinnerAutoActivation.getValueFactory().setValue(model.getSelectionModeParameter().autoActivationTimeProperty().get() / 1000.0);
        this.toggleEnableAutoActivation.setSelected(model.getSelectionModeParameter().enableAutoActivationProperty().get());
        this.spinnerAutoOver.getValueFactory().setValue(model.getSelectionModeParameter().autoOverTimeProperty().get() / 1000.0);
        this.toggleStartScanningOnClic.setSelected(model.getSelectionModeParameter().startScanningOnClicProperty().get());
        this.toggleEnableActivationWithSelection.setSelected(model.getSelectionModeParameter().enableActivationWithSelectionProperty().get());
        this.toggleEnableDirectSelectionOnMouseOnScanningSelectionMode.setSelected(model.getSelectionModeParameter().enableDirectSelectionOnMouseOnScanningSelectionModeProperty().get());
        this.sliderSelectionViewSize.setValue(model.getSelectionModeParameter().selectionViewSizeProperty().get());
        this.sliderProgressBarSize.setValue(model.getSelectionModeParameter().progressViewBarSizeProperty().get());
        this.comboBoxScanningMode.getSelectionModel().select(model.getSelectionModeParameter().scanningModeProperty().get());
        this.comboBoxNextScanEventInput.getSelectionModel().select(model.getSelectionModeParameter().nextScanEventInputProperty().get());
        this.keySelectorControlKeyboardNextScanKeyCode.valueProperty().set(model.getSelectionModeParameter().keyboardNextScanKeyProperty().get());
        this.toggleBackgroundReductionEnabled.setSelected(model.getSelectionModeParameter().backgroundReductionEnabledProperty().get());
        this.sliderBackgroundReductionLevel.setValue(model.getSelectionModeParameter().backgroundReductionLevelProperty().get());
        this.mouseButtonSelectorControl.setValue(model.getSelectionModeParameter().mouseButtonNextScanProperty().get());
        this.toggleHideMouseCursor.setSelected(model.getSelectionModeParameter().hideMouseCursorProperty().get());
        this.toggleShowVirtualCursor.setSelected(model.getSelectionModeParameter().showVirtualCursorProperty().get());
        this.colorPickerVirtualCursorColor.setValue(model.getSelectionModeParameter().virtualCursorColorProperty().get());
        this.sliderVirtualCursorSize.setValue(model.getSelectionModeParameter().virtualCursorSizeProperty().get());
        dirty = false;
    }

    @Override
    public void unbind(SelectionModeUserI model) {
    }

    public void saveChanges() {
        model.get().getSelectionModeParameter().selectionActivationViewColorProperty().set(colorPickerActivation.getValue());
        model.get().getSelectionModeParameter().selectionViewColorProperty().set(colorPickerSelection.getValue());
        model.get().getSelectionModeParameter().drawProgressProperty().set(toggleEnableProgressDrawing.isSelected());
        model.get().getSelectionModeParameter().scanPauseProperty().set((int) (spinnerScanPause.getValue() * 1000.0));
        model.get().getSelectionModeParameter().scanFirstPauseProperty().set((int) (spinnerFirstPause.getValue() * 1000.0));
        model.get().getSelectionModeParameter().autoActivationTimeProperty().set((int) (spinnerAutoActivation.getValue() * 1000.0));
        model.get().getSelectionModeParameter().enableAutoActivationProperty().set(toggleEnableAutoActivation.isSelected());
        model.get().getSelectionModeParameter().autoOverTimeProperty().set((int) (spinnerAutoOver.getValue() * 1000.0));
        model.get().getSelectionModeParameter().progressViewColorProperty().set(this.colorPickerProgressColor.getValue());
        model.get().getSelectionModeParameter().maxScanBeforeStopProperty().set(this.spinnerMaxScan.getValueFactory().getValue());
        model.get().getSelectionModeParameter().manifyKeyOverProperty().set(this.toggleManifyKeyOver.isSelected());
        model.get().getSelectionModeParameter().skipEmptyComponentProperty().set(this.toggleSkipEmptyCells.isSelected());
        model.get().getSelectionModeParameter().progressDrawModeProperty().set(this.comboboxDrawProgressMode.getSelectionModel().selectedItemProperty().get());
        model.get().getSelectionModeParameter().startScanningOnClicProperty().set(this.toggleStartScanningOnClic.isSelected());
        model.get().getSelectionModeParameter().enableActivationWithSelectionProperty().set(this.toggleEnableActivationWithSelection.isSelected());
        model.get().getSelectionModeParameter().selectionViewSizeProperty().set(this.sliderSelectionViewSize.getValue());
        model.get().getSelectionModeParameter().progressViewBarSizeProperty().set(this.sliderProgressBarSize.getValue());
        model.get().getSelectionModeParameter().scanningModeProperty().set(comboBoxScanningMode.getSelectionModel().selectedItemProperty().get());
        model.get().getSelectionModeParameter().nextScanEventInputProperty().set(comboBoxNextScanEventInput.getSelectionModel().selectedItemProperty().get());
        model.get().getSelectionModeParameter().keyboardNextScanKeyProperty().set(keySelectorControlKeyboardNextScanKeyCode.valueProperty().get());
        model.get().getSelectionModeParameter().backgroundReductionEnabledProperty().set(this.toggleBackgroundReductionEnabled.isSelected());
        model.get().getSelectionModeParameter().backgroundReductionLevelProperty().set(this.sliderBackgroundReductionLevel.getValue());
        model.get().getSelectionModeParameter().enableDirectSelectionOnMouseOnScanningSelectionModeProperty().set(toggleEnableDirectSelectionOnMouseOnScanningSelectionMode.isSelected());
        model.get().getSelectionModeParameter().mouseButtonNextScanProperty().set(mouseButtonSelectorControl.getValue());
        model.get().getSelectionModeParameter().hideMouseCursorProperty().set(toggleHideMouseCursor.isSelected());
        model.get().getSelectionModeParameter().showVirtualCursorProperty().set(toggleShowVirtualCursor.isSelected());
        model.get().getSelectionModeParameter().virtualCursorColorProperty().set(this.colorPickerVirtualCursorColor.getValue());
        model.get().getSelectionModeParameter().virtualCursorSizeProperty().set(this.sliderVirtualCursorSize.getValue());
    }

    public void setSelectedSelectionMode(SelectionModeEnum selectionMode) {
        this.selectedMode.set(selectionMode);
    }

    @Override
    public void initUI() {
        labelSelectedMode = new Label();
        labelSelectedMode.getStyleClass().add("text-weight-bold");
        labelSelectionModeSelectedDesc = new Label(Translation.getText("selection.mode.param.selected.selection.mode.label"));
        labelSelectionModeSelectedDesc.setMinWidth(GeneralConfigurationStepViewI.LEFT_COLUMN_MIN_WIDTH);
        labelSelectedMode.setMaxWidth(Double.MAX_VALUE);
        labelSelectedMode.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setHgrow(labelSelectedMode, Priority.ALWAYS);

        // Scanning mode
        comboBoxScanningMode = new ComboBox<>(FXCollections.observableArrayList(ScanningMode.values()));
        this.comboBoxScanningMode.setButtonCell(new ScanningModeSimpleListCell());
        this.comboBoxScanningMode.setCellFactory((lv) -> new ScanningModeSimpleListCell());
        comboBoxScanningMode.setMaxWidth(Double.MAX_VALUE);
        FXControlUtils.createAndAttachTooltip(comboBoxScanningMode, "tooltip.selection.mode.start.manual.next.scan");
        labelScanningMode = new Label(Translation.getText("selection.mode.scanning.mode.label"));

        // Manual scanning input part
        titlePartManualScanning = FXControlUtils.createTitleLabel("selection.mode.param.title.part.manual.scanning.part");

        this.comboBoxNextScanEventInput = new ComboBox<>(FXCollections.observableArrayList(FireEventInput.values()));
        this.comboBoxNextScanEventInput.setCellFactory((lv) -> new FireEventInputListCell());
        this.comboBoxNextScanEventInput.setButtonCell(new FireEventInputListCell());
        this.comboBoxNextScanEventInput.setMaxWidth(Double.MAX_VALUE);
        labelNextScanEventInput = new Label(Translation.getText("selection.mode.scanning.manual.input.type"));
        labelNextScanEventInput.disableProperty().bind(comboBoxNextScanEventInput.disabledProperty());
        FXControlUtils.createAndAttachTooltip(comboBoxNextScanEventInput, "tooltip.selection.mode.scanning.manual.input.type");

        // Direct mouse selection while scanning mode
        toggleEnableDirectSelectionOnMouseOnScanningSelectionMode = FXControlUtils.createToggleSwitch("selection.mode.enable.direct.selection.on.mouse.on.scanning.mode",
                "selection.mode.enable.direct.selection.on.mouse.on.scanning.mode.tooltip");

        // Keyboard input parameters
        this.keySelectorControlKeyboardNextScanKeyCode = new KeyCodeSelectorControl(null);
        labelKeyboardNextScanKey = new Label(Translation.getText("selection.mode.scanning.manual.input.keyboard.key"));
        labelKeyboardNextScanKey.disableProperty().bind(keySelectorControlKeyboardNextScanKeyCode.disabledProperty());
        this.keyboardControlNodes = Arrays.asList(labelKeyboardNextScanKey, keySelectorControlKeyboardNextScanKeyCode);

        mouseButtonSelectorControl = new MouseButtonSelectorControl();
        labelMouseButton = new Label(Translation.getText("selection.mode.scanning.manual.mouse.button.selector.label"));
        labelMouseButton.disableProperty().bind(mouseButtonSelectorControl.disabledProperty());
        mouseNodes = Arrays.asList(labelMouseButton, mouseButtonSelectorControl);

        // Auto activation
        titlePartActivation = FXControlUtils.createTitleLabel("selection.mode.param.title.part.activation.configuration");
        this.toggleEnableAutoActivation = FXControlUtils.createToggleSwitch("selection.mode.enable.auto.activation", "selection.mode.enable.auto.activation.tooltip");
        this.spinnerAutoActivation = FXControlUtils.createDoubleSpinner(0.0, 100.0, 2.0, 0.1, GeneralConfigurationStepViewI.FIELD_WIDTH);
        FXControlUtils.createAndAttachTooltip(spinnerAutoActivation, "tooltip.explain.auto.activation.time");
        labelTimeActivation = new Label(Translation.getText("selection.mode.auto.time.activation"));
        toggleEnableActivationWithSelection = FXControlUtils.createToggleSwitch("selection.mode.auto.enable.direct.activation",
                "tooltip.explain.selection.mode.auto.enable.direct.activation");
        GridPane.setHalignment(this.spinnerAutoActivation, HPos.RIGHT);
        GridPane.setHgrow(labelTimeActivation, Priority.ALWAYS);

        // Direct activation
        this.spinnerAutoOver = FXControlUtils.createDoubleSpinner(0.0, 100.0, 2.0, 0.1, GeneralConfigurationStepViewI.FIELD_WIDTH);
        labelTimeOver = new Label(Translation.getText("selection.mode.auto.time.over"));
        GridPane.setHgrow(labelTimeOver, Priority.ALWAYS);
        FXControlUtils.createAndAttachTooltip(spinnerAutoOver, "tooltip.explain.auto.over.time");
        GridPane.setHalignment(this.spinnerAutoOver, HPos.RIGHT);

        // Virtual cursor
        titlePartVirtualCursor = FXControlUtils.createTitleLabel("selection.mode.param.title.part.virtual.cursor");
        this.toggleShowVirtualCursor = FXControlUtils.createToggleSwitch("selection.mode.show.virtual.cursor", null);
        labelVirtualCursorSize = new Label(Translation.getText("selection.mode.virtual.cursor.size"));
        this.sliderVirtualCursorSize = FXControlUtils.createBaseSlider(4.0, 60.0, 5);
        this.sliderVirtualCursorSize.setMajorTickUnit(5);
        this.sliderVirtualCursorSize.setMinorTickCount(2);
        labelVirtualCursorColor = new Label(Translation.getText("selection.mode.virtual.cursor.color"));
        colorPickerVirtualCursorColor = new LCColorPicker();

        // Auto scanning
        titlePartAutoScanning = FXControlUtils.createTitleLabel("selection.mode.param.title.part.auto.scanning.configuration");
        labelScanPause = new Label(Translation.getText("selection.mode.param.scan.pause"));
        this.spinnerScanPause = FXControlUtils.createDoubleSpinner(0.1, 100.0, 2.0, 0.1, GeneralConfigurationStepViewI.FIELD_WIDTH);
        FXControlUtils.createAndAttachTooltip(spinnerScanPause, "tooltip.explain.selection.param.scan.pause");
        GridPane.setHalignment(spinnerScanPause, HPos.RIGHT);
        labelScanPause.disableProperty().bind(spinnerScanPause.disabledProperty());
        labelScanFirstPause = new Label(Translation.getText("selection.mode.param.scan.first.pause"));
        this.spinnerFirstPause = FXControlUtils.createDoubleSpinner(0.0, 100.0, 1.0, 0.1, GeneralConfigurationStepViewI.FIELD_WIDTH);
        GridPane.setHalignment(spinnerFirstPause, HPos.RIGHT);
        FXControlUtils.createAndAttachTooltip(spinnerFirstPause, "tooltip.explain.selection.param.scan.first.pause");
        labelScanFirstPause.disableProperty().bind(spinnerFirstPause.disabledProperty());
        this.toggleStartScanningOnClic = FXControlUtils.createToggleSwitch("selection.mode.start.scanning.on.clic",
                "tooltip.explain.selection.param.scan.start.on.clic");

        // Scanning part (auto + manual)
        titlePartScanning = FXControlUtils.createTitleLabel("selection.mode.param.title.part.scanning.configuration");
        labelScanMaxSame = new Label(Translation.getText("selection.mode.param.max.scan.same"));
        this.spinnerMaxScan = FXControlUtils.createIntSpinner(1, 20, 2, 1, GeneralConfigurationStepViewI.FIELD_WIDTH);
        GridPane.setHalignment(spinnerMaxScan, HPos.RIGHT);
        FXControlUtils.createAndAttachTooltip(spinnerMaxScan, "tooltip.explain.selection.param.scan.max.scan");

        // Style part
        titlePartStyle = FXControlUtils.createTitleLabel("selection.mode.param.title.general.style.configuration");
        this.colorPickerActivation = new LCColorPicker();
        colorPickerActivation.setMaxWidth(Double.MAX_VALUE);
        FXControlUtils.createAndAttachTooltip(colorPickerActivation, "tooltip.explain.selection.param.activation.color");
        this.colorPickerSelection = new LCColorPicker();
        colorPickerSelection.setMaxWidth(Double.MAX_VALUE);
        FXControlUtils.createAndAttachTooltip(colorPickerSelection, "tooltip.explain.selection.param.selection.color");
        this.toggleHideMouseCursor = FXControlUtils.createToggleSwitch("selection.mode.hide.mouse.cursor", "selection.mode.hide.mouse.cursor.tooltip");
        this.toggleManifyKeyOver = FXControlUtils.createToggleSwitch("selection.mode.manify.key.over", "tooltip.explain.selection.param.manify.key");
        this.toggleSkipEmptyCells = FXControlUtils.createToggleSwitch("selection.mode.enable.skip.empty",
                "tooltip.explain.selection.param.skip.empty");
        labelColorSelection = new Label(Translation.getText("selection.mode.param.selection.color"));
        labelColorActivation = new Label(Translation.getText("selection.mode.param.activation.color"));
        // Selection view size
        this.sliderSelectionViewSize = FXControlUtils.createBaseSlider(0.0, 40, 5);
        this.sliderSelectionViewSize.setMajorTickUnit(10);
        this.sliderSelectionViewSize.setMinorTickCount(3);
        FXControlUtils.createAndAttachTooltip(sliderSelectionViewSize, "tooltip.explain.selection.param.selection.view.size");
        labelSelectionViewSize = new Label(Translation.getText("selection.mode.param.selection.view.size"));

        // Scanning style part
        titlePartProgressStyle = FXControlUtils.createTitleLabel("selection.mode.param.title.progress.style.configuration");
        this.toggleEnableProgressDrawing = FXControlUtils.createToggleSwitch("selection.mode.param.draw.scanning.progress",
                "tooltip.explain.selection.param.enable.draw.progress");
        this.colorPickerProgressColor = new LCColorPicker();
        colorPickerProgressColor.setMaxWidth(Double.MAX_VALUE);
        FXControlUtils.createAndAttachTooltip(colorPickerProgressColor, "tooltip.explain.selection.param.progress.color");
        this.comboboxDrawProgressMode = new ComboBox<>(FXCollections.observableArrayList(ProgressDrawMode.values()));
        this.comboboxDrawProgressMode.setButtonCell(new ProgressDrawModeSimpleListCell());
        this.comboboxDrawProgressMode.setCellFactory((lv) -> new ProgressDrawModeSimpleListCell());
        comboboxDrawProgressMode.setMaxWidth(Double.MAX_VALUE);
        comboboxDrawProgressMode.setPrefWidth(150.0);
        FXControlUtils.createAndAttachTooltip(comboboxDrawProgressMode, "tooltip.explain.selection.param.progress.draw.mode");
        labelDrawProgressMode = new Label(Translation.getText("selection.mode.draw.progress.mode"));
        labelProgressColor = new Label(Translation.getText("selection.mode.param.progress.color"));
        labelProgressColor.disableProperty().bind(colorPickerProgressColor.disabledProperty());
        labelDrawProgressMode.disableProperty().bind(comboboxDrawProgressMode.disabledProperty());
        // Progress bar view size
        labelProgressBarSize = new Label(Translation.getText("selection.mode.param.progress.bar.size"));
        this.sliderProgressBarSize = FXControlUtils.createBaseSlider(0.0, 40, 5);
        this.sliderProgressBarSize.setMajorTickUnit(10);
        this.sliderProgressBarSize.setMinorTickCount(3);
        FXControlUtils.createAndAttachTooltip(sliderProgressBarSize, "tooltip.explain.selection.param.progress.bar.size");
        labelProgressBarSize.disableProperty().bind(sliderProgressBarSize.disabledProperty());

        toggleBackgroundReductionEnabled = FXControlUtils.createToggleSwitch("selection.mode.param.background.reduction.enabled",
                "tooltip.explain.selection.mode.param.background.reduction.enabled");

        labelBackgroundReductionLevel = new Label(Translation.getText("selection.mode.param.background.reduction.level"));
        this.sliderBackgroundReductionLevel = FXControlUtils.createBaseSlider(0.0, 1.0, 0.8);
        this.sliderBackgroundReductionLevel.setMajorTickUnit(0.1);
        this.sliderBackgroundReductionLevel.setMinorTickCount(1);
        FXControlUtils.createAndAttachTooltip(sliderProgressBarSize, "tooltip.explain.selection.mode.param.background.reduction.level");
        labelBackgroundReductionLevel.disableProperty().bind(sliderBackgroundReductionLevel.disabledProperty());

        // GridPane total
        gridPaneConfiguration = new GridPane();
        gridPaneConfiguration.setHgap(GeneralConfigurationStepViewI.GRID_H_GAP);
        gridPaneConfiguration.setVgap(GeneralConfigurationStepViewI.GRID_V_GAP);

        // Add to center
        gridPaneConfiguration.setPadding(new Insets(GeneralConfigurationStepViewI.PADDING));
        ScrollPane scrollPane = new ScrollPane(gridPaneConfiguration);
        scrollPane.setFitToWidth(true);
        this.setCenter(scrollPane);
    }

    @Override
    public void initListener() {
    }

    @Override
    public void initBinding() {
        selectedMode.addListener((obs, ov, nv) -> updateConfigurationGridPaneForCurrentMode(nv));

        // Mode selected
        labelSelectedMode.textProperty().bind(Bindings.createStringBinding(() -> selectedMode.get() != null ? selectedMode.get().getName() : "", selectedMode));

        sliderBackgroundReductionLevel.disableProperty().bind(toggleBackgroundReductionEnabled.selectedProperty().not());

        // Progress display : when auto scanning, or virtual cursor with auto activation or only auto clic mode
        BooleanBinding progressDisplayDisabled = this.comboBoxScanningMode.valueProperty().isEqualTo(ScanningMode.AUTO)
                .or(Bindings.createBooleanBinding(() -> selectedMode.get() != null && selectedMode.get().useVirtualCursorProperty().get(), selectedMode)
                        .and(toggleEnableAutoActivation.selectedProperty()))
                .or(Bindings.createBooleanBinding(() -> selectedMode.get() != null && selectedMode.get().useAutoClicProperty().get() && !selectedMode.get().useVirtualCursorProperty().get(),
                        selectedMode))
                .not();

        this.toggleEnableProgressDrawing.disableProperty().bind(progressDisplayDisabled);
        BooleanBinding progressDisabledBinding = this.toggleEnableProgressDrawing.selectedProperty().not().or(progressDisplayDisabled);
        this.colorPickerProgressColor.disableProperty().bind(progressDisabledBinding);
        this.comboboxDrawProgressMode.disableProperty().bind(progressDisabledBinding);
        this.sliderProgressBarSize.disableProperty().bind(
                Bindings.createBooleanBinding(() -> progressDisabledBinding.get() || this.comboboxDrawProgressMode.getSelectionModel().getSelectedItem() != ProgressDrawMode.PROGRESS_BAR
                        , this.comboboxDrawProgressMode.getSelectionModel().selectedItemProperty(), progressDisabledBinding));

        // Manual scanning activation
        this.keySelectorControlKeyboardNextScanKeyCode.disableProperty().bind(comboBoxScanningMode.valueProperty().isNotEqualTo(ScanningMode.MANUAL));
        this.mouseButtonSelectorControl.disableProperty().bind(comboBoxScanningMode.valueProperty().isNotEqualTo(ScanningMode.MANUAL));
        this.comboBoxNextScanEventInput.disableProperty().bind(comboBoxScanningMode.valueProperty().isNotEqualTo(ScanningMode.MANUAL));

        // Auto scanning activation
        this.spinnerFirstPause.disableProperty().bind(comboBoxScanningMode.valueProperty().isNotEqualTo(ScanningMode.AUTO));
        this.spinnerScanPause.disableProperty().bind(comboBoxScanningMode.valueProperty().isNotEqualTo(ScanningMode.AUTO));
        this.toggleStartScanningOnClic.disableProperty().bind(comboBoxScanningMode.valueProperty().isNotEqualTo(ScanningMode.AUTO));

        // Virtual cursor style, if showing
        this.colorPickerVirtualCursorColor.disableProperty().bind(toggleShowVirtualCursor.selectedProperty().not());
        this.labelVirtualCursorColor.disableProperty().bind(colorPickerVirtualCursorColor.disabledProperty());
        this.sliderVirtualCursorSize.disableProperty().bind(toggleShowVirtualCursor.selectedProperty().not());
        this.labelVirtualCursorSize.disableProperty().bind(sliderVirtualCursorSize.disabledProperty());

        // Keyboard key selection hidden if not selected
        for (Node n : this.keyboardControlNodes)
            n.visibleProperty().bind(comboBoxNextScanEventInput.valueProperty().isEqualTo(FireEventInput.KEYBOARD));
        // Mouse button selection hidden if not selected
        for (Node n : this.mouseNodes)
            n.visibleProperty().bind(comboBoxNextScanEventInput.valueProperty().isEqualTo(FireEventInput.MOUSE));

        InvalidationListener invalidationListener = e -> dirty = true;
        this.comboBoxNextScanEventInput.valueProperty().addListener(invalidationListener);
        this.comboboxDrawProgressMode.valueProperty().addListener(invalidationListener);
        this.comboBoxScanningMode.valueProperty().addListener(invalidationListener);
        this.spinnerAutoActivation.valueProperty().addListener(invalidationListener);
        this.toggleEnableAutoActivation.selectedProperty().addListener(invalidationListener);
        this.spinnerAutoOver.valueProperty().addListener(invalidationListener);
        this.spinnerFirstPause.valueProperty().addListener(invalidationListener);
        this.spinnerMaxScan.valueProperty().addListener(invalidationListener);
        this.spinnerScanPause.valueProperty().addListener(invalidationListener);
        this.sliderProgressBarSize.valueProperty().addListener(invalidationListener);
        this.sliderBackgroundReductionLevel.valueProperty().addListener(invalidationListener);
        this.sliderSelectionViewSize.valueProperty().addListener(invalidationListener);
        this.colorPickerSelection.valueProperty().addListener(invalidationListener);
        this.colorPickerActivation.valueProperty().addListener(invalidationListener);
        this.colorPickerProgressColor.valueProperty().addListener(invalidationListener);
        this.toggleBackgroundReductionEnabled.selectedProperty().addListener(invalidationListener);
        this.toggleEnableDirectSelectionOnMouseOnScanningSelectionMode.selectedProperty().addListener(invalidationListener);
        this.toggleHideMouseCursor.selectedProperty().addListener(invalidationListener);
        this.toggleManifyKeyOver.selectedProperty().addListener(invalidationListener);
        this.toggleSkipEmptyCells.selectedProperty().addListener(invalidationListener);
        this.toggleStartScanningOnClic.selectedProperty().addListener(invalidationListener);
        this.mouseButtonSelectorControl.valueProperty().addListener(invalidationListener);
        this.keySelectorControlKeyboardNextScanKeyCode.valueProperty().addListener(invalidationListener);
        this.toggleShowVirtualCursor.selectedProperty().addListener(invalidationListener);
        this.colorPickerVirtualCursorColor.valueProperty().addListener(invalidationListener);
        this.sliderVirtualCursorSize.valueProperty().addListener(invalidationListener);
    }


    /**
     * This clear the current field grid and add fields again.<br>
     * This is mandatory because {@link GridPane} doesn't ignore un managed children between rows/columns.
     *
     * @param selectionModeEnum the current selection mode
     */
    private void updateConfigurationGridPaneForCurrentMode(SelectionModeEnum selectionModeEnum) {
        gridPaneConfiguration.getChildren().clear();

        int gridRowIndex = 0;

        if (selectionModeEnum != null) {
            gridPaneConfiguration.add(labelSelectionModeSelectedDesc, 0, gridRowIndex);
            gridPaneConfiguration.add(labelSelectedMode, 1, gridRowIndex++);

            if (selectionModeEnum.useScanningProperty().get()) {
                gridPaneConfiguration.add(labelScanningMode, 0, gridRowIndex);
                gridPaneConfiguration.add(comboBoxScanningMode, 1, gridRowIndex++);

                gridPaneConfiguration.add(titlePartManualScanning, 0, gridRowIndex++, 2, 1);
                gridPaneConfiguration.add(labelNextScanEventInput, 0, gridRowIndex);
                gridPaneConfiguration.add(comboBoxNextScanEventInput, 1, gridRowIndex++);
                gridPaneConfiguration.add(labelKeyboardNextScanKey, 0, gridRowIndex);
                gridPaneConfiguration.add(labelMouseButton, 0, gridRowIndex);
                gridPaneConfiguration.add(keySelectorControlKeyboardNextScanKeyCode, 1, gridRowIndex);
                gridPaneConfiguration.add(mouseButtonSelectorControl, 1, gridRowIndex++);
            }

            // Activation configuration
            if (selectionModeEnum.useAutoClicProperty().get() || selectionModeEnum.usePointerProperty().get()) {
                gridPaneConfiguration.add(titlePartActivation, 0, gridRowIndex++, 2, 1);
            }
            if (selectionModeEnum.useVirtualCursorProperty().get()) {
                gridPaneConfiguration.add(toggleEnableAutoActivation, 0, gridRowIndex++, 2, 1);
            }
            if (selectionModeEnum.useAutoClicProperty().get()) {
                gridPaneConfiguration.add(labelTimeActivation, 0, gridRowIndex);
                gridPaneConfiguration.add(spinnerAutoActivation, 1, gridRowIndex++);
                gridPaneConfiguration.add(toggleEnableActivationWithSelection, 0, gridRowIndex++, 2, 1);
            }
            if (selectionModeEnum.usePointerProperty().get()) {
                gridPaneConfiguration.add(labelTimeOver, 0, gridRowIndex);
                gridPaneConfiguration.add(spinnerAutoOver, 1, gridRowIndex++);
            }

            // Scanning configuration
            if (selectionModeEnum.useScanningProperty().get()) {
                gridPaneConfiguration.add(titlePartAutoScanning, 0, gridRowIndex++, 2, 1);
                gridPaneConfiguration.add(labelScanPause, 0, gridRowIndex);
                gridPaneConfiguration.add(spinnerScanPause, 1, gridRowIndex++);
                gridPaneConfiguration.add(labelScanFirstPause, 0, gridRowIndex);
                gridPaneConfiguration.add(spinnerFirstPause, 1, gridRowIndex++);
                gridPaneConfiguration.add(toggleStartScanningOnClic, 0, gridRowIndex++, 2, 1);

                gridPaneConfiguration.add(titlePartScanning, 0, gridRowIndex++, 2, 1);
                gridPaneConfiguration.add(labelScanMaxSame, 0, gridRowIndex);
                gridPaneConfiguration.add(spinnerMaxScan, 1, gridRowIndex++);

                gridPaneConfiguration.add(toggleEnableDirectSelectionOnMouseOnScanningSelectionMode, 0, gridRowIndex++, 2, 1);
            }

            // Style configuration
            gridPaneConfiguration.add(titlePartStyle, 0, gridRowIndex++, 2, 1);
            gridPaneConfiguration.add(labelColorSelection, 0, gridRowIndex);
            gridPaneConfiguration.add(colorPickerSelection, 1, gridRowIndex++);
            gridPaneConfiguration.add(labelColorActivation, 0, gridRowIndex);
            gridPaneConfiguration.add(colorPickerActivation, 1, gridRowIndex++);
            gridPaneConfiguration.add(labelSelectionViewSize, 0, gridRowIndex);
            gridPaneConfiguration.add(sliderSelectionViewSize, 1, gridRowIndex++);
            gridPaneConfiguration.add(toggleHideMouseCursor, 0, gridRowIndex++, 2, 1);
            gridPaneConfiguration.add(toggleManifyKeyOver, 0, gridRowIndex++, 2, 1);
            gridPaneConfiguration.add(toggleBackgroundReductionEnabled, 0, gridRowIndex++, 2, 1);
            gridPaneConfiguration.add(labelBackgroundReductionLevel, 0, gridRowIndex, 1, 1);
            gridPaneConfiguration.add(sliderBackgroundReductionLevel, 1, gridRowIndex++, 1, 1);
            gridPaneConfiguration.add(toggleSkipEmptyCells, 0, gridRowIndex++, 2, 1);

            // Virtual cursor
            if (selectionModeEnum.useVirtualCursorProperty().get()) {
                gridPaneConfiguration.add(titlePartVirtualCursor, 0, gridRowIndex++, 2, 1);
                gridPaneConfiguration.add(toggleShowVirtualCursor, 0, gridRowIndex++, 2, 1);
                gridPaneConfiguration.add(labelVirtualCursorColor, 0, gridRowIndex);
                gridPaneConfiguration.add(colorPickerVirtualCursorColor, 1, gridRowIndex++);
                gridPaneConfiguration.add(labelVirtualCursorSize, 0, gridRowIndex, 1, 1);
                gridPaneConfiguration.add(sliderVirtualCursorSize, 1, gridRowIndex++, 1, 1);
            }

            // Progress
            if (selectionModeEnum.useProgressDrawProperty().get()) {
                gridPaneConfiguration.add(titlePartProgressStyle, 0, gridRowIndex++, 2, 1);
                gridPaneConfiguration.add(toggleEnableProgressDrawing, 0, gridRowIndex++, 2, 1);
                gridPaneConfiguration.add(labelProgressColor, 0, gridRowIndex);
                gridPaneConfiguration.add(colorPickerProgressColor, 1, gridRowIndex++);
                gridPaneConfiguration.add(labelDrawProgressMode, 0, gridRowIndex);
                gridPaneConfiguration.add(comboboxDrawProgressMode, 1, gridRowIndex++);
                gridPaneConfiguration.add(labelProgressBarSize, 0, gridRowIndex);
                gridPaneConfiguration.add(sliderProgressBarSize, 1, gridRowIndex++);
            }
        }
    }

    public ObjectProperty<SelectionModeUserI> modelProperty() {
        return model;
    }

    public void resetDirty() {
        dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }
}
