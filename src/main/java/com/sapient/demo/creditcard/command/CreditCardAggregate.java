package com.sapient.demo.creditcard.command;

import com.sapient.demo.creditcard.api.*;
import com.sapient.demo.creditcard.api.*;
import com.sapient.demo.creditcard.api.*;
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
        log.debug("handling {}", cmd);

        if(cmd.getLimitValue() <= 0) throw new IllegalArgumentException("remainingValue <= 0");
        if(cmd.getId().length() >19) throw new IllegalArgumentException("credit card number > 19");

        apply(new IssuedEvt(cmd.getId(),cmd.getName(), cmd.getLimitValue()));
    }

    @CommandHandler
    public void handle(PurchaseCmd cmd) {
        log.debug("handling {}", cmd);
        if(cmd.getPurchaseValue() <= 0) throw new IllegalArgumentException("remainingValue <= 0");
        if(cmd.getPurchaseValue() > remainingValue) throw new IllegalStateException("remainingValue > remaining value");
        apply(new PurchasedEvt(id, cmd.getPurchaseValue()));
    }

    @CommandHandler
    public void handle(CancelCmd cmd) {
        log.debug("handling {}", cmd);
        apply(new CancelEvt(id));
    }

    @EventSourcingHandler
    public void on(IssuedEvt evt) {
        log.debug("applying {}", evt);
        id = evt.getId();
        remainingValue = evt.getLimitValue();
        log.debug("new remaining value: {}", remainingValue);
    }

    @EventSourcingHandler
    public void on(PurchasedEvt evt) {
        log.debug("applying {}", evt);
        remainingValue -= evt.getPurchaseValue();
        log.debug("new remaining value: {}", remainingValue);
    }

    @EventSourcingHandler
    public void on(CancelEvt evt) {
        log.debug("applying {}", evt);
        remainingValue = 0D;
        log.debug("new remaining value: {}", remainingValue);
    }

    public static boolean luhnCheck(String card) {
        if (card == null)
            return false;
        char checkDigit = card.charAt(card.length() - 1);
        String digit = calculateCheckDigit(card.substring(0, card.length() - 1));
        return checkDigit == digit.charAt(0);
    }

    public static String calculateCheckDigit(String card) {
        if (card == null)
            return null;
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2)	{
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }
}
