package com.generic_tools.tester;

import com.generic_tools.validations.RuntimeValidator;

/**
 * Created by oem on 4/5/17.
 */
public class ValidationTester {

    public static void main(String[] args) {
        System.out.println("Start Validator testing");

        RuntimeValidator runtimeValidator = new RuntimeValidator();
        ObjectWithName objectWithName = new ObjectWithName();
        objectWithName.setName("Any Name");
        System.out.println(runtimeValidator.validate(objectWithName).getMessage());

        objectWithName = new ObjectWithName();
        System.out.println(runtimeValidator.validate(objectWithName).getMessage());

        System.out.println("Finish Validator test");
    }
}
