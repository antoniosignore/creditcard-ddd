package com.sapient.demo.creditcard.rest;

import com.sapient.demo.creditcard.api.IssueCmd;
import com.sapient.demo.creditcard.api.PurchaseCmd;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.Future;

@RestController
@Profile("rest")
public class CreditCardCommandController {

    private final CommandGateway commandGateway;

    public CreditCardCommandController(@SuppressWarnings("SpringJavaAutowiringInspection") CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping("/creditcards")
    public Future<String> createCreditCard(@RequestBody @Valid CreditCardValueBean creditCard) {

        Assert.notNull(creditCard.getName(), "name is mandatory for credit card");
        Assert.notNull(creditCard.getId(), "Number is mandatory for credit card");
        Assert.notNull(creditCard.getCreditLimit(), "Credit limit is mandatory for credit card");

        return commandGateway.send(
                new IssueCmd(
                        creditCard.getId(),
                        creditCard.getName(),
                        creditCard.getCreditLimit()));
    }

    @PostMapping("/creditcards/{id}/purchase")
    public Future<Void> purchase(@PathVariable String id, @RequestBody @Valid Double purchasePrice) {
        return commandGateway.send(new PurchaseCmd(id, purchasePrice));
    }
}
