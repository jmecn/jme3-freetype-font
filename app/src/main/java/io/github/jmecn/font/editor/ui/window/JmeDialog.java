package io.github.jmecn.font.editor.ui.window;

import com.simsilica.lemur.*;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.ElementId;
import lombok.NonNull;

public class JmeDialog implements Dialog {

    private final Container dialogContainer;
    private WindowManager windowManager;

    public JmeDialog(String title, String description, DialogButton... buttons) {
        this(title, new Label(description), buttons);
    }

    public JmeDialog(String title, Panel content, DialogButton... buttons) {

        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("You must specify a dialog title.");
        }

        if (content == null) {
            throw new RuntimeException("You must specify dialog content.");
        }

        dialogContainer = new Container("null");

        Container titleContainer = dialogContainer.addChild(new Container(new SpringGridLayout(), new ElementId(JmeWindow.ELEMENT_ID_TITLE_BAR)));
        titleContainer.addChild(new Label(title, new ElementId(JmeWindow.ELEMENT_ID_TITLE_LABEL)));

        Container contentParent = dialogContainer.addChild(new Container(
                new SpringGridLayout(Axis.Y, Axis.X, FillMode.First, FillMode.First),
                new ElementId(JmeWindow.ELEMENT_ID_WINDOW_CONTENT_OUTER)));

        Container contentContainer = contentParent.addChild(new Container(
                new SpringGridLayout(),
                new ElementId(JmeWindow.ELEMENT_ID_WINDOW_CONTENT_INNER)
        ));

        contentContainer.addChild(content);

        Container buttonsContainer = contentContainer.addChild(
                new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.First, FillMode.First), "null"));

        buttonsContainer.setInsets(new Insets3f(15,0,0,0));
        buttonsContainer.addChild(new Label(""), 0, 0);

        if (buttons.length == 0) {
            // throw new RuntimeException("You must specify at least one dialog button.");
            Button button = buttonsContainer.addChild(new Button(DialogButton.Ok.name(), new ElementId("dialog-button")), 0, 1);
            button.addClickCommands(source -> closeDialog());
        }
        else {
            for (int i = 0; i < buttons.length; i++) {

                DialogButton type = buttons[i];
                Button button = buttonsContainer.addChild(new Button(type.name(), new ElementId("dialog-button")), 0, i + 1);
                button.addCommands(Button.ButtonAction.Click, source -> buttonPressed(type));
            }
        }

    }

    @Override
    public void setWindowManager(@NonNull WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public @NonNull Panel getDialogPanel() {
        return dialogContainer;
    }

    @Override
    public void closeDialog() {
        windowManager.closeDialog(this);
    }

    /**
     * An event that is fired when a button is pressed.
     * @param buttonType the type of button pressed to cause the event to fire.
     */
    @Override
    public void buttonPressed(DialogButton buttonType) {

    }

}
