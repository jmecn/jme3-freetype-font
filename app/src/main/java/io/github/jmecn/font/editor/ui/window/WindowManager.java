package io.github.jmecn.font.editor.ui.window;

import com.jme3.app.Application;
import lombok.NonNull;

public interface WindowManager {

    Application getApplication();

    @NonNull
    Window add(@NonNull Window window);
    boolean remove(@NonNull Window window);

    Window getByTitle(@NonNull String title);
    Window getById(@NonNull String id);

    int getWindowCount();

    void bringToFront(@NonNull Window window);
    void sendToBack(@NonNull Window window);

    void showDialog(@NonNull String title, @NonNull String text);
    void showDialog(@NonNull Dialog dialog);
    void closeDialog(@NonNull Dialog dialog);
}
