package com.sapient.demo.creditcard.command;

import com.sapient.demo.creditcard.api.IssueCmd;
import com.sapient.demo.creditcard.api.IssuedEvt;
import com.sapient.demo.creditcard.api.PurchaseCmd;
import com.sapient.demo.creditcard.api.PurchasedEvt;
import lombok.Data;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.lang.invoke.MethodHandles;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
@Profile("command")
@Data
public class CreditCardAggregate {

    private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @AggregateIdentifier
    private String id;
    private Double limitValue;
    private Double remainingValue;

    public CreditCardAggregate() {
        log.debug("empty constructor invoked");
    }

    @CommandHandler
    public CreditCardAggregate(IssueCmd cmd) {
        if (cmd.getLimitValue() < 0) throw new IllegalArgumentException("remainingValue < 0");
        if(cmd.getId().length() >19) throw new IllegalArgumentException("credit card number > 19");
        apply(new IssuedEvt(cmd.getId(),cmd.getName(), cmd.getLimitValue()));
    }

    @CommandHandler
    public void handle(PurchaseCmd cmd) {
        if (cmd.getPurchaseValue() < 0) throw new IllegalArgumentException("purchase value < 0");
        if(cmd.getPurchaseValue() > remainingValue) throw new IllegalStateException("remainingValue > remaining value");
        apply(new PurchasedEvt(cmd.getId(), cmd.getPurchaseValue()));
    }

    @EventSourcingHandler
    public void on(IssuedEvt evt) {
        id = evt.getId();
        remainingValue = evt.getLimitValue();
    }

    @EventSourcingHandler
    public void on(PurchasedEvt evt) {
        remainingValue -= evt.getPurchaseValue();
    }

}
