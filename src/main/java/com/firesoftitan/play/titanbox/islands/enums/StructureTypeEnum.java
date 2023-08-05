package com.firesoftitan.play.titanbox.islands.enums;

public enum StructureTypeEnum {
    ANIMAL("animal", 30),
    BUILDING("building", 50),
    WOOD("wood", 20),
    SHORE("shore", 10),
    MINERAL("mineral", 25),
    INLAND("inland", 15);

    private final String name;
    private final int value;
    StructureTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
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
