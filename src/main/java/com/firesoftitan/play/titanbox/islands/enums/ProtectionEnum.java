package com.firesoftitan.play.titanbox.islands.enums;

public enum ProtectionEnum {
    USE("use"),
    BREAK("break"),
    BUILD("build"),
    IGNITE("ignite"),
    CREEPERS("creepers");

    private final String name;
    ProtectionEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
