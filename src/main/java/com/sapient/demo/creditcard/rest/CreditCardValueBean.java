package com.sapient.demo.creditcard.rest;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class CreditCardValueBean {

    String id;
    String name;
    Double creditLimit;

}
