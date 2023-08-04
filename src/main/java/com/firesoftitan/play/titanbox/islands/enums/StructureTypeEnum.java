package com.firesoftitan.play.titanbox.islands.enums;

public enum StructureTypeEnum {
    ANIMAL("animal"),
    BUILDING("building"),
    WOOD("wood"),
    SHORE("shore"),
    MINERAL("mineral"),
    INLAND("inland");

    private final String name;
    StructureTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public static StructureTypeEnum getType(String name)
    {
        for(StructureTypeEnum value: StructureTypeEnum.values())
        {
            if (value.getName().equalsIgnoreCase(name.toLowerCase()))
            {
                return value;
            }
        }
        return null;
    }

}
