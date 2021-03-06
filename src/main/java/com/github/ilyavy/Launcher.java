package com.github.ilyavy;

import com.github.ilyavy.controller.App;

/**
 * Launcher is used as a workaround in order to not require javafx runtime installed on the target machine.
 * {@see https://github.com/javafxports/openjdk-jfx/issues/236}
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(new String[0]);
    }
}
