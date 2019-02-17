import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowContentUiType;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

public class FootPrintToolWindow implements ToolWindow {
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void activate(@Nullable Runnable runnable) {

    }

    @Override
    public void activate(@Nullable Runnable runnable, boolean autoFocusContents) {

    }

    @Override
    public void activate(@Nullable Runnable runnable, boolean autoFocusContents, boolean forced) {

    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void show(@Nullable Runnable runnable) {

    }

    @Override
    public void hide(@Nullable Runnable runnable) {

    }

    @Override
    public ToolWindowAnchor getAnchor() {
        return null;
    }

    @Override
    public void setAnchor(@NotNull ToolWindowAnchor anchor, @Nullable Runnable runnable) {

    }

    @Override
    public boolean isSplitMode() {
        return false;
    }

    @Override
    public void setSplitMode(boolean split, @Nullable Runnable runnable) {

    }

    @Override
    public boolean isAutoHide() {
        return false;
    }

    @Override
    public void setAutoHide(boolean state) {

    }

    @Override
    public ToolWindowType getType() {
        return null;
    }

    @Override
    public void setType(@NotNull ToolWindowType type, @Nullable Runnable runnable) {

    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void setIcon(Icon icon) {

    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setTitle(String title) {

    }

    @NotNull
    @Override
    public String getStripeTitle() {
        return null;
    }

    @Override
    public void setStripeTitle(@NotNull String title) {

    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void setAvailable(boolean available, @Nullable Runnable runnable) {

    }

    @Override
    public void setContentUiType(@NotNull ToolWindowContentUiType type, @Nullable Runnable runnable) {

    }

    @Override
    public void setDefaultContentUiType(@NotNull ToolWindowContentUiType type) {

    }

    @NotNull
    @Override
    public ToolWindowContentUiType getContentUiType() {
        return null;
    }

    @Override
    public void installWatcher(ContentManager contentManager) {

    }

    @Override
    public JComponent getComponent() {
        return null;
    }

    @Override
    public ContentManager getContentManager() {
        return null;
    }

    @Override
    public void setDefaultState(@Nullable ToolWindowAnchor anchor, @Nullable ToolWindowType type, @Nullable Rectangle floatingBounds) {

    }

    @Override
    public void setToHideOnEmptyContent(boolean hideOnEmpty) {

    }

    @Override
    public boolean isToHideOnEmptyContent() {
        return false;
    }

    @Override
    public void setShowStripeButton(boolean show) {

    }

    @Override
    public boolean isShowStripeButton() {
        return false;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void showContentPopup(InputEvent inputEvent) {

    }

    @NotNull
    @Override
    public ActionCallback getReady(@NotNull Object requestor) {
        return null;
    }
}
