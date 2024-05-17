package ui.style

import com.jme3.material.RenderState
import com.simsilica.lemur.*;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.component.*;

def gradient = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/bordered-gradient.png",
                generateMips:false ),
        1, 1, 1, 126, 126,
        1f, false );

def bevel = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/bevel-quad.png",
                generateMips:false ),
        0.125f, 8, 8, 119, 119,
        1f, false );

def transparent = new QuadBackgroundComponent(color(0, 0, 0, 0))

def border = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/border.png",
                generateMips:false ),
        1, 1, 1, 6, 6,
        1f, false );

def border2 = TbtQuadBackgroundComponent.create(
        texture( name:"/com/simsilica/lemur/icons/border.png",
                generateMips:false ),
        1, 2, 2, 6, 6,
        1f, false );

def doubleGradient = new QuadBackgroundComponent( color(0.5, 0.75, 0.85, 0.5) );
doubleGradient.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png", generateMips:false )

selector( "dark" ) {
    fontSize = 14
}

selector( "label", "dark" ) {
    insets = new Insets3f( 2, 2, 0, 2 );
    color = color(0.75, 0.75, 0.75, 0.95)
}

selector( "container", "dark" ) {
    background = bevel.clone()
    background.setColor(color(0.5, 0.5, 0.5, 1.0))
}

selector( "slider", "dark" ) {
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 0.95))
}

def pressedCommand = new Command<Button>() {
    void execute( Button source ) {
        if( source.isPressed() ) {
            source.move(1, -1, 0);
        } else {
            source.move(-1, 1, 0);
        }
    }
};

def repeatCommand = new Command<Button>() {
    private long startTime;
    private long lastClick;

    void execute( Button source ) {
        // Only do the repeating click while the mouse is
        // over the button (and pressed of course)
        if( source.isPressed() && source.isHighlightOn() ) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            // After half a second pause, click 8 times a second
            if( elapsedTime > 500 ) {
                if( elapsedTime - lastClick > 125 ) {
                    source.click();

                    // Try to quantize the last click time to prevent drift
                    lastClick = ((elapsedTime - 500) / 125) * 125 + 500;
                }
            }
        } else {
            startTime = System.currentTimeMillis();
            lastClick = 0;
        }
    }
};

def stdButtonCommands = [
        (ButtonAction.Down):[pressedCommand],
        (ButtonAction.Up):[pressedCommand]
];

def sliderButtonCommands = [
        (ButtonAction.Hover):[repeatCommand]
];

selector( "title", "dark" ) {
    color = color(0.9, 0.9, 0.9, 0.85)

    highlightColor = color(1, 0.8, 1, 0.85)
    shadowColor = color(0, 0, 0, 0.75)
    shadowOffset = vec3(1, -1, -1);
    background = new QuadBackgroundComponent( color(0.75, 0.75, 0.75, 0.5) );
    background.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png",
            generateMips:false )
    insets = new Insets3f( 2, 2, 2, 2 );

    buttonCommands = stdButtonCommands;
}

selector( "button", "dark" ) {
    background = gradient.clone()
    color = color(0.9, 0.9, 0.9, 0.85f)
    background.setColor(color(0.75, 0.75, 0.75, 0.5))
    insets = new Insets3f( 2, 2, 2, 2 );

    buttonCommands = stdButtonCommands;
}

selector( "slider", "dark" ) {
    insets = new Insets3f( 1, 3, 1, 2 );
}

selector( "slider", "button", "dark" ) {
    background = doubleGradient.clone()
    background.setColor(color(0.75, 0.75, 0.75, 0.5))
    insets = new Insets3f( 0, 0, 0, 0 );
}

selector( "slider.thumb.button", "dark" ) {
    text = "[]"
    color = color(0.6, 0.8, 0.8, 0.85)
}

selector( "slider.left.button", "dark" ) {
    text = "-"
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    background.setMargin(5, 0);
    color = color(0.6, 0.8, 0.8, 0.85)

    buttonCommands = sliderButtonCommands;
}

selector( "slider.right.button", "dark" ) {
    text = "+"
    background = doubleGradient.clone()
    background.setColor(color(0.5, 0.75, 0.75, 0.5))
    background.setMargin(4, 0);
    color = color(0.6, 0.8, 0.8, 0.85)

    buttonCommands = sliderButtonCommands;
}

selector( "slider.up.button", "dark" ) {
    buttonCommands = sliderButtonCommands;
}

selector( "slider.down.button", "dark" ) {
    buttonCommands = sliderButtonCommands;
}

selector( "optionPanel", "dark" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "optionPanel.container", "dark" ) {

    background = gradient.clone()
    background.color = color(0.25, 0.4, 0.6, 0.25)
    background.setMargin(10, 10)
    insets = new Insets3f( 2, 2, 2, 2 )
}

selector( "list.container", "dark" ) {
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "list.item", "dark" ) {
    color = color(0.75, 0.75, 0.75, 0.85)
    background = transparent.clone();
}

selector( "list.selector", "dark" ) {
    background = gradient.clone();
    background.color = color(0.6, 0.6, 0.6, 0.5)
    background.material.material.additionalRenderState.blendMode = RenderState.BlendMode.AlphaAdditive;
}

selector( "colorChooser.value", "dark" ) {
    border = gradient.clone()
    border.setColor(color(0.5, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "colorChooser.colors", "dark" ) {
    border = gradient.clone()
    border.setColor(color(0.5, 0.5, 0.5, 0.5))
    insets = new Insets3f( 2, 2, 2, 2, 2, 2 );
}

selector( "selector.container", "dark" ) {
    color = color(0.8, 0.9, 1, 0.85f)
    background = gradient.clone()
    background.setColor(color(0, 0.6, 0.6, 0.5))
}

selector( "selector.item", "dark" ) {
    color = color(0.8, 0.8, 0.9, 0.9)
    background = transparent;
    insets = new Insets3f(1, 1, 1, 1, 1, 1);
}

selector( "selector.popup", "dark" ) {
    background = gradient.clone()
    background.setColor(color(0, 0.75, 0.75, 0.75))
}

selector( "selector.down.button", "dark" ) {
    insets = new Insets3f(0, 0, 0, 0, 0, 0);
}

selector( "spinner.value", "dark" ) {
    color = color(0.8, 0.8, 0.9, 0.9)
    background = gradient.clone();
    background.color = color(0.2, 0.2, 0.2, 0.75);
}

selector( "spinner.buttons.container", "dark" ) {
    background = transparent;
    insets = new Insets3f(0, 0, 0, 0);
}

selector( "spinner", "button", "dark" ) {
    background = gradient.clone()
    // A negative margin works here when the font can support it.
    // It helps eat up the extra whitespace above/below the '+'/'-' signs.
    background.setMargin(2, -3.5);
    background.setColor(color(0.75, 0.75, 0.75, 0.5))
    insets = new Insets3f(0, 0, 0, 0);
    textHAlignment = HAlignment.Center;
}

selector( "dropdown", "selection", "dark") {
    background = border2.clone();
}

selector( "checkbox", "dark" ) {
    def on = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-on.png", 1f,
            0, 0, 1f, false );
    on.setColor(color(0.9, 0.9, 0.9, 0.9))
    on.setMargin(5, 0);
    def off = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-off.png", 1f,
            0, 0, 1f, false );
    off.setColor(color(0.8, 0.8, 0.8, 0.8))
    off.setMargin(5, 0);

    onView = on;
    offView = off;

    color = color(0.8, 0.9, 1, 0.85f)
}

selector( "rollup", "dark" ) {
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 0.5))
}

selector( "tabbedPanel", "dark" ) {
    activationColor = color(0.8, 0.9, 1, 0.85f)
}

selector( "tabbedPanel.container", "dark" ) {
    background = null
}

selector( "tab.button", "dark" ) {
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 0.5))
    color = color(0.4, 0.45, 0.5, 0.85f)
    insets = new Insets3f( 4, 2, 0, 2 );

    buttonCommands = stdButtonCommands;
}

def menuHoverCommand = new Command<Button>() {
    void execute( Button source ) {
        if( source.isHighlightOn() ) {
            if (source.isEnabled()) {
                ((TbtQuadBackgroundComponent) source.getBackground()).setColor(color(0.25, 0.5, 0.5, 1.0))
            }
        } else {
            ((TbtQuadBackgroundComponent)source.getBackground()).setColor(color(0.25, 0.5, 0.5, 0.0))
        }
    }
}

def stdMenuCommands = [
        (Button.ButtonAction.HighlightOn) :[menuHoverCommand],
        (Button.ButtonAction.HighlightOff):[menuHoverCommand]
]

selector( "menu-bar", "dark") {
    background = gradient.clone()
    background.setMargin(5, 5)
    background.setColor(color(0.5, 0.5, 0.5, 1.0))
}

// Menu and MenuItem (Button)
selector( "menu-item", "dark") {
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 0.0))
    background.setMargin(10, 5)
    color = color(0.8, 0.9, 1, 0.85f)
    buttonCommands = stdMenuCommands
}

// CheckboxMenuItem (CheckBox)
selector( "menu-checkbox", "dark" ) {

    def on = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-on.png", 1f, 0, 0, 1f, false )
    on.setColor(color(0.9, 0.9, 0.9, 0.9))
    on.setMargin(5, 0)

    def off = new IconComponent( "/com/simsilica/lemur/icons/Glass-check-off.png", 1f, 0, 0, 1f, false )
    off.setColor(color(0.8, 0.8, 0.8, 0.8))
    off.setMargin(5, 0)

    onView = on
    offView = off

    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 0.0))
    background.setMargin(10, 5)

    buttonCommands = stdMenuCommands
}

// menu children container
selector( "menu-children", "dark" ) {
    background = gradient.clone()
    background.setMargin(5, 5)
    background.setColor(color(0.5, 0.5, 0.5, 1.0))
}


/////////// window
selector( "window-title-bar", "dark" ) {
    // background = new QuadBackgroundComponent(ColorUtils.fromHex("#365d8f"))
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 1.0))
    //background.setMargin(2, 2)
}

selector( "window-title-label", "dark" ) {
    background = null
    textVAlignment = VAlignment.Center
    color = color(0.8, 0.8, 0.8, 1.0)
    insets = new Insets3f( 5, 5, 5, 15 )
}

selector( "window-content-outer", "dark") {
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 1.0))
    background.setMargin(5,5)
}

selector( "window-content-inner", "dark" ) {
    background = gradient.clone()
    background.setColor(color(0.5, 0.5, 0.5, 0.25))
    background.setMargin(5,5)
}

selector( "window-button-minimize", "dark" ) {
    background = null
    icon = new IconComponent("ui/images/button.png")
    icon.setColor(color(1.0, 0.7412, 0.298, 1.0))
    insets = new Insets3f(5,5,5,5)
    fontSize = 0
}

selector( "window-button-maximize", "dark" ) {
    background = null
    icon = new IconComponent("ui/images/button.png")
    icon.setColor(color(0.0, 0.7921, 0.3411, 1.0))
    insets = new Insets3f(5,0,5,5)
    fontSize = 0
}

selector( "window-button-close", "dark" ) {
    background = null
    icon = new IconComponent("ui/images/button.png")
    icon.setColor(color(1.0, 0.3882, 0.3568, 1.0))
    insets = new Insets3f(5,0,5,5)
    fontSize = 0
}

selector( "dialog-button", "dark") {
    background = gradient.clone()
    color = color(0.8, 0.9, 1, 0.85f)
    background.setColor(color(0, 0.75, 0.75, 0.5))
    background.setMargin(15, 5)

    highlightColor = color(1, 1, 0, 1)// Yellow
    focusColor = color(0, 1, 0, 1)// Green

    insets = new Insets3f( 2, 2, 2, 2 );

    buttonCommands = stdButtonCommands;
}

selector( "value", "label", "dark" ) {
    insets = new Insets3f( 1, 2, 0, 2 );
    textHAlignment = HAlignment.Right;
    background = border.clone();
    background.color = color(0.75, 0.75, 0.75, 0.25)
    color = color(0.8, 0.8, 0.8, 0.85)
}
