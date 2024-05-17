package io.github.jmecn.font.editor.ui.menubar;

import com.simsilica.lemur.Panel;

public interface MenuElement {

    Panel getPanel();

    String getText();

    void setText(String text);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    MenuElement getParent();

    void setParent(MenuElement element);

    LemurMenuBar getMenuBar();

    void setMenuBar(LemurMenuBar menuBar);

}