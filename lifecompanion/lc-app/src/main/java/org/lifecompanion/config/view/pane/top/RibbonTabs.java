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

package org.lifecompanion.config.view.pane.top;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.controlsfx.glyphfont.FontAwesome;
import org.fxmisc.easybind.EasyBind;
import org.lifecompanion.base.data.control.refacto.AppModeController;
import org.lifecompanion.config.data.config.LCGlyphFont;
import org.lifecompanion.config.view.pane.tabs.RibbonTabAction;
import org.lifecompanion.config.view.pane.tabs.RibbonTabHome;
import org.lifecompanion.config.view.pane.tabs.RibbonTabSelected;
import org.lifecompanion.config.view.pane.tabs.RibbonTabStyle;
import org.lifecompanion.config.view.pane.tabs.api.AbstractTabContent;
import org.lifecompanion.config.view.scene.ConfigurationScene;
import org.lifecompanion.framework.commons.ui.LCViewInitHelper;

/**
 * Class that contains all the ribbons in different tabs and the menu button.
 *
 * @author Mathieu THEBAUD <math.thebaud@gmail.com>
 */
public class RibbonTabs extends StackPane implements LCViewInitHelper {
    private Button buttonMenu;
    private TabPane tabPane;
    //private Tab tabSelectedPart;

    public RibbonTabs() {
        this.initAll();
    }

    @Override
    public void initUI() {
        //Create buttons
        this.buttonMenu = new Button();
        this.buttonMenu.setBackground(null);
        this.buttonMenu.setGraphic(LCGlyphFont.FONT_AWESOME.create(FontAwesome.Glyph.BARS).sizeFactor(2).color(Color.WHITE));
        this.buttonMenu.setShape(new Circle(32.0));
        this.buttonMenu.setStyle("-fx-background-color:-fx-main-primary;");
        StackPane.setAlignment(this.buttonMenu, Pos.TOP_LEFT);
        StackPane.setMargin(this.buttonMenu, new Insets(3.0));
        this.buttonMenu.setCache(true);
        //Create tab pane
        this.tabPane = new TabPane();
        this.tabPane.getStyleClass().add("ribbon-tabs");
        this.tabPane.tabClosingPolicyProperty().set(TabClosingPolicy.UNAVAILABLE);
        //Add tabs
        this.addTab(new RibbonTabHome());
        this.addTab(new RibbonTabSelected());
        this.addTab(new RibbonTabStyle());
        this.addTab(new RibbonTabAction());

        //Add all
        this.getChildren().addAll(this.tabPane, this.buttonMenu);
    }

    private Tab addTab(final AbstractTabContent tabContent) {
        Tab tab = new Tab();
        tab.setContent(tabContent);
        tab.textProperty().bind(EasyBind.map(tabContent.tabTitleProperty(), String::toUpperCase));
        tab.disableProperty().bind(tabContent.disableTabProperty().or(AppModeController.INSTANCE.getEditModeContext().configurationProperty().isNull()));
        this.tabPane.getTabs().add(tab);
        return tab;
    }

    public Button getMenuButton() {
        return this.buttonMenu;
    }

    @Override
    public void initListener() {
        //Button behavior
        this.buttonMenu.setOnAction(ea -> {
            ConfigurationScene scene = (ConfigurationScene) AppModeController.INSTANCE.getEditModeContext().getStage().getScene();
            scene.switchMenu();
        });
    }
}