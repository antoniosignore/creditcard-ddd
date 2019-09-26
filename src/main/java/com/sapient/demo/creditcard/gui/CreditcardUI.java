package com.sapient.demo.creditcard.gui;

import com.sapient.demo.creditcard.api.*;
import com.vaadin.annotations.Push;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SpringUI
@Push
@Profile("gui")
public class CreditcardUI extends UI {

    private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    private CardSummaryDataProvider cardSummaryDataProvider;
    private ScheduledFuture<?> updaterThread;

    public CreditcardUI(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout commandBar = new HorizontalLayout();
        commandBar.setWidth("100%");
        commandBar.addComponents(issuePanel(),  purchasePanel());

        Grid summary = summaryGrid();

        HorizontalLayout statusBar = new HorizontalLayout();
        Label statusLabel = new Label("Status");
        statusBar.setDefaultComponentAlignment(Alignment.MIDDLE_RIGHT);
        statusBar.addComponent(statusLabel);
        statusBar.setWidth("100%");

        VerticalLayout layout = new VerticalLayout();
        layout.addComponents(commandBar, summary, statusBar);
        layout.setExpandRatio(summary, 1f);
        layout.setSizeFull();

        setContent(layout);

        UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Throwable cause = event.getThrowable();
                log.error("an error occured", cause);
                while(cause.getCause() != null) cause = cause.getCause();
                Notification.show("Error", cause.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        });

        setPollInterval(1000);
        int offset = Page.getCurrent().getWebBrowser().getTimezoneOffset();
        // offset is in milliseconds
             ZoneOffset instantOffset = ZoneOffset.ofTotalSeconds(offset/1000);
        StatusUpdater statusUpdater = new StatusUpdater(statusLabel, instantOffset);
        updaterThread = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(statusUpdater, 1000,
                                                                     5000, TimeUnit.MILLISECONDS);
        setPollInterval(1000);
        getSession().getSession().setMaxInactiveInterval(30);
        addDetachListener((DetachListener) detachEvent -> {
             log.warn("Closing UI");
             updaterThread.cancel(true);

        });

    }

    private Panel issuePanel() {
        TextField id = new TextField("Credit Card Number");
        TextField name = new TextField("Name");
        TextField creditLimit = new TextField("Credit Limit");
        Button submit = new Button("Submit");

        submit.addClickListener(evt -> {
            commandGateway.sendAndWait(new IssueCmd(id.getValue(), name.getValue(), Integer.parseInt(creditLimit.getValue())));
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener(e -> cardSummaryDataProvider.refreshAll());
        });

        FormLayout form = new FormLayout();
        form.addComponents(id, name, creditLimit, submit);
        form.setMargin(true);

        Panel panel = new Panel("Issue single card");
        panel.setContent(form);
        return panel;
    }

    private Panel purchasePanel() {
        TextField id = new TextField("Credit Card Number");
        TextField name = new TextField("Name");
        TextField creditLimitValue = new TextField("Purchase value");
        Button submit = new Button("Submit");

        submit.addClickListener(evt -> {
            commandGateway.sendAndWait(new PurchaseCmd(id.getValue(), Integer.parseInt(creditLimitValue.getValue())));
            Notification.show("Success", Notification.Type.HUMANIZED_MESSAGE)
                    .addCloseListener(e -> cardSummaryDataProvider.refreshAll());
        });

        FormLayout form = new FormLayout();
        form.addComponents(id, name, creditLimitValue, submit);
        form.setMargin(true);

        Panel panel = new Panel("Purchase with card");
        panel.setContent(form);
        return panel;
    }

    private Grid summaryGrid() {
        cardSummaryDataProvider = new CardSummaryDataProvider(queryGateway);
        Grid<CardSummary> grid = new Grid<>();
        grid.addColumn(CardSummary::getId).setCaption("Credit Card Number");
        grid.addColumn(CardSummary::getName).setCaption("Name");
        grid.addColumn(CardSummary::getLimitValue).setCaption("Limit value");
        grid.addColumn(CardSummary::getRemainingValue).setCaption("Remaining credit");
        grid.setSizeFull();
        grid.setDataProvider(cardSummaryDataProvider);
        return grid;
    }

    public class StatusUpdater implements Runnable {
        private final Label statusLabel;
         private final ZoneOffset instantOffset;

         public StatusUpdater(Label statusLabel, ZoneOffset instantOffset) {
             this.statusLabel = statusLabel;
             this.instantOffset = instantOffset;
         }

         @Override
         public void run() {
             CountCardSummariesQuery query = new CountCardSummariesQuery();
             queryGateway.query(
                     query, CountCardSummariesResponse.class).whenComplete((r, exception) -> {
                 if( exception == null) statusLabel.setValue(Instant.ofEpochMilli(r.getLastEvent()).atOffset(instantOffset).toString());
             });

         }

    }
}
