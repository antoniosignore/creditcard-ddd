package io.axoniq.labs.game.commandmodel;


import com.sapient.demo.creditcard.api.IssueCmd;
import com.sapient.demo.creditcard.api.IssuedEvt;
import com.sapient.demo.creditcard.api.PurchaseCmd;
import com.sapient.demo.creditcard.command.CreditCardAggregate;
import org.axonframework.eventsourcing.IncompatibleAggregateException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CreditCardAggregateTest {
    private AggregateTestFixture<CreditCardAggregate> testFixture;

    @Before
    public void setUp() {
        testFixture = new AggregateTestFixture<>(CreditCardAggregate.class);
    }

    @Test
    public void test_create_credit_card_with_0_limit() {
        testFixture.givenNoPriorActivity()
                .when(new IssueCmd("123", "me", 0))
                .expectEvents(new IssuedEvt("123", "me", 0));
    }

    @Test
    public void test_create_credit_card_with_negative_limit() {
        testFixture.givenNoPriorActivity()
                .when(new IssueCmd("123", "me", -1))
                .expectNoEvents().expectException(IllegalArgumentException.class);
    }

    @Test
    public void test_create_credit_card_with_100_limit() {
        testFixture.givenNoPriorActivity()
                .when(new IssueCmd("123", "me", 100))
                .expectEvents(new IssuedEvt("123", "me", 100));
    }

    @Test
    public void test_create_credit_card_with_number_longer_than_19() {
        testFixture.givenNoPriorActivity()
                .when(new IssueCmd("12345678901234567890", "me", 100))
                .expectNoEvents().expectException(IllegalArgumentException.class);
    }

    // TODO add embedded db server to run this test
    @Ignore
    @Test
    public void test_purchase_negative_value() {
        final PurchaseCmd purchase = new PurchaseCmd("123", -10);

        testFixture
                .given(new IssueCmd("123", "me", 100))
                .when(purchase)
                .expectNoEvents().expectException(IllegalArgumentException.class);
    }

    // TODO add embedded db server to run this test
    @Ignore
    @Test
    public void test_purchase_with_credit_card_with_100_limit() {

        final PurchaseCmd purchase = new PurchaseCmd("123", 10);

        testFixture
                .given(new IssueCmd("123", "me", 100))
                .when(purchase)
                .expectNoEvents().expectException(IncompatibleAggregateException.class);
    }

}
