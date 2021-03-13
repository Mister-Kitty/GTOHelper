package com.gtohelper.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Logger {

    public enum Channel {
        SOLVER,
        HUD,
        HELPER
    }

    private Map<Channel, Consumer<String>> consumerMap = new HashMap<>();

    private static final Logger instance = new Logger();

    public static Logger getInstance() {
        return instance;
    }

    public static void log(Exception e) {
        log(e.getMessage());
    }

    public static void log(String message){
        log(Channel.HELPER, message);
    }

    public static void log(Channel channel, String message){
        Consumer<String> listener = getInstance().consumerMap.get(channel);
        if(listener != null)
            listener.accept(message);
    }

    public static void addChannelListener(Channel channel, Consumer<String> listener) {
        getInstance().consumerMap.put(channel, listener);
    }
}
