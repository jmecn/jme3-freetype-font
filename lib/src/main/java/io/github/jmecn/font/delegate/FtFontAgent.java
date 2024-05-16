package io.github.jmecn.font.delegate;

import com.jme3.font.BitmapText;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.dynamic.loading.ClassReloadingStrategy.fromInstalledAgent;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class FtFontAgent {
    public static void init() {
        // The dark magic to override private method BitmapText#assemble()
        ByteBuddyAgent.install();

        new ByteBuddy().redefine(BitmapText.class)
                .method(ElementMatchers.named("assemble"))
                .intercept(MethodDelegation.to(BitmapTextDelegate.class))
                .make()
                .load(BitmapText.class.getClassLoader(), fromInstalledAgent());
    }
}
