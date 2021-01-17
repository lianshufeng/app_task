package top.dzurl.apptask.core.type;

import lombok.Getter;

public enum PlatformType {

    Android("Android"),
    Ios("Ios")
    ;

    @Getter
    private String name;

    PlatformType(String name) {
        this.name = name;
    }
}
