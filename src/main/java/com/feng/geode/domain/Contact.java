package com.feng.geode.domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

public class Contact implements Serializable {
    @Getter
    private String name;
    @Getter
    private String[] phoneNumbers;

    public Contact(String name, String[] phoneNumbers) {
        this.name = name;
        this.phoneNumbers = phoneNumbers;
    }

    @Override
    public String toString() {
        return "(name=" + name + ", phones=" + Arrays.toString(phoneNumbers) + ")";
    }
}
