package org.slf4j.impl;

import javafx.util.Pair;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;

public class StaticLoggerBinder implements Logger {

    private static ThreadLocal<List<Pair<Level, String>>> LOGS = new ThreadLocal<>();

    // For SLF4J
    public ILoggerFactory getLoggerFactory() {
        return name -> this;
    }

    // FOR SLF4J
    public static StaticLoggerBinder getSingleton() {
        return new StaticLoggerBinder();
    }

    public static void hook() {
        LOGS.set(new ArrayList<>());
    }

    public static int count() {
        return LOGS.get().size();
    }

    public static Level getLevel(int i) {
        return LOGS.get().get(i).getKey();
    }

    public static String getLog(int i) {
        return LOGS.get().get(i).getValue();
    }

    @Override
    public String getName() {
        return StaticLoggerBinder.ROOT_LOGGER_NAME;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String s) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(String s, Object o) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return true;
    }

    @Override
    public void trace(Marker marker, String s) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.TRACE, s));
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String s) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(String s, Object o) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public void debug(Marker marker, String s) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.DEBUG, s));
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String s) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(String s, Object o) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public void info(Marker marker, String s) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.INFO, s));
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String s) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(String s, Object o) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void warn(Marker marker, String s) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.WARN, s));
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String s) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(String s, Object o) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void error(Marker marker, String s) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        LOGS.get().add(new Pair<>(Level.ERROR, s));
    }
}
