package com.feng.geode.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;

@AllArgsConstructor
public class EmployeeData implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private String firstName;
    @Getter
    private String lastName;
    @Getter
    private int employeeNumber;
    @Getter
    private String email;
    @Getter
    private int salary;
    @Getter
    private int hoursPerWeek;
    @Getter
    private Collection<Contact> contacts;

    @Override
    public String toString() {
        return "EmployeeData [firstName=" + firstName + ", lastName=" + lastName + ", employeeNumber="
               + employeeNumber + ", email= " + email + ", salary=" + salary + ", hoursPerWeek=" + hoursPerWeek
               + ", contacts=" + contacts + "]";
    }
}
