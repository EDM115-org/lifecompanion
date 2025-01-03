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

package org.lifecompanion.ui.notification;

import javafx.animation.KeyValue;
import javafx.beans.value.WritableValue;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.lifecompanion.controller.lifecycle.AppMode;
import org.lifecompanion.controller.lifecycle.AppModeController;
import org.lifecompanion.controller.useapi.GlobalRuntimeConfigurationController;
import org.lifecompanion.model.api.lifecycle.LCStateListener;
import org.lifecompanion.model.impl.useapi.GlobalRuntimeConfiguration;
import org.lifecompanion.util.javafx.FXThreadUtils;
import org.lifecompanion.controller.editaction.AsyncExecutorController;
import org.lifecompanion.model.impl.notification.LCNotification;
import org.lifecompanion.util.javafx.StageUtils;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public enum LCNotificationController implements LCStateListener {
    INSTANCE;

    private static final double SPACE_BETWEEN_NOTIFICATIONS = 10.0;

    private final CopyOnWriteArrayList<DisplayedNotificationContainer> displayedNotifications;
    private final LinkedList<Runnable> notificationToShow;
    private final AtomicBoolean handlingQueueItem;

    LCNotificationController() {
        notificationToShow = new LinkedList<>();
        displayedNotifications = new CopyOnWriteArrayList<>();
        handlingQueueItem = new AtomicBoolean();
    }

    // HANDLING NOTIFICATIONS
    //========================================================================
    private void handleNextNotificationShowRequest() {
        if (!handlingQueueItem.get() && !notificationToShow.isEmpty()) {
            notificationToShow.pop().run();
        }
    }

    static class DisplayedNotificationContainer {
        private final WritableValue<Double> stageYWritable;
        private final Stage notificationStage;

        DisplayedNotificationContainer(Stage notificationStage) {
            this.notificationStage = notificationStage;
            this.stageYWritable = new WritableValue<>() {
                @Override
                public Double getValue() {
                    return notificationStage.getY();
                }

                @Override
                public void setValue(Double value) {
                    notificationStage.setY(value);
                }
            };
        }

        KeyValue getShiftUpKeyValue() {
            return new KeyValue(stageYWritable, stageYWritable.getValue() - notificationStage.getHeight() - SPACE_BETWEEN_NOTIFICATIONS);
        }
    }
    //========================================================================

    // NOTIFICATION UI
    //========================================================================
    public void showNotification(LCNotification notification) {
        if (GlobalRuntimeConfigurationController.INSTANCE.isPresent(GlobalRuntimeConfiguration.DISABLE_ERROR_NOTIFICATION) && AppModeController.INSTANCE.isUseMode() && notification.getType() == LCNotification.LCNotificationType.ERROR) {
            return;
        }
        FXThreadUtils.runOnFXThread(() -> {
            notificationToShow.add(() -> {
                handlingQueueItem.set(true);

                NotificationStage notificationStage = new NotificationStage(new NotificationScene(notification));
                DisplayedNotificationContainer displayedNotificationContainer = new DisplayedNotificationContainer(notificationStage);
                notificationStage.setOnHidden(e -> displayedNotifications.remove(displayedNotificationContainer));

                notificationStage.setOnShown(e -> {
                    Window topWindow = StageUtils.getOnTopWindowExcludingNotification();
                    if (topWindow != null) topWindow.requestFocus();
                });

                notificationStage.show(displayedNotifications, () -> {
                    handlingQueueItem.set(false);
                    displayedNotifications.add(displayedNotificationContainer);
                    handleNextNotificationShowRequest();
                });
            });
            handleNextNotificationShowRequest();
        });
    }
    //========================================================================

    // STATE LISTENER
    //========================================================================
    @Override
    public void lcStart() {
        AsyncExecutorController.INSTANCE.addTaskAddedForExecutionListener((task, blocking, hideFromNotification) -> {
            if (!hideFromNotification)
                showNotification(LCNotification.createTask(task, blocking));
        });
    }

    @Override
    public void lcExit() {
    }
    //========================================================================
}
