package com.sapient.demo.creditcard.repository;

import com.sapient.demo.creditcard.command.CreditCardAggregate;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("command")
public class CreditCardCommandRepository {

	@Bean
	public Repository<CreditCardAggregate> creditCardRepository(EventStore eventStore, Cache cache) {
		return EventSourcingRepository.builder(CreditCardAggregate.class)
				.cache(cache).eventStore(eventStore)
				.build();
	}

	@Bean
    public Cache cache() {
	    return new WeakReferenceCache();
	}
}
