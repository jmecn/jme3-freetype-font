package io.github.jmecn.font;

public final class PlatformUtil {

    private static final String OS = System.getProperty("os.name");
    private static final boolean ANDROID = "Dalvik".equals(System.getProperty("java.vm.name"));
    private static final boolean WINDOWS = OS.startsWith("Windows");
    private static final boolean MAC = OS.startsWith("Mac");
    private static final boolean LINUX = OS.startsWith("Linux") && !ANDROID;
    private static final boolean IOS = OS.startsWith("iOS");

    /**
     * Returns true if the operating system is a form of Windows.
     */
    public static boolean isWindows(){
        return WINDOWS;
    }

    /**
     * Returns true if the operating system is a form of Mac OS.
     */
    public static boolean isMac(){
        return MAC;
    }

    /**
     * Returns true if the operating system is a form of Linux.
     */
    public static boolean isLinux(){
        return LINUX;
    }

    /**
     * Returns true if the operating system is iOS
     */
    public static boolean isIOS(){
        return IOS;
    }

    public static boolean isAndroid() {
       return ANDROID;
    }
}