package com.generic_tools.tester;

/**
 * Created by oem on 4/5/17.
 */
@NameNotEmpty
public class ObjectWithName {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ObjectWithName{" +
                "name='" + name + '\'' +
                '}';
    }
}
