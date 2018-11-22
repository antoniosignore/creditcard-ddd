package com.sapient.demo.creditcard.gui;

import com.sapient.demo.creditcard.api.CountCardSummariesQuery;
import com.sapient.demo.creditcard.api.CountCardSummariesResponse;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("gui")
public class CcGuiConfiguration {

	@EventListener(ApplicationReadyEvent.class)
	public void helloHub(ApplicationReadyEvent event) {
		QueryGateway queryGateway = event.getApplicationContext().getBean(QueryGateway.class);
		queryGateway.query(new CountCardSummariesQuery(),
				ResponseTypes.instanceOf(CountCardSummariesResponse.class));
	}

}
