package com.sapient.demo.creditcard.api

import org.axonframework.modelling.command.TargetAggregateIdentifier

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery

data class IssueCmd(@TargetAggregateIdentifier val id: String, val name: String, val limitValue: Double)
data class IssuedEvt(val id: String, val name: String, val limitValue: Double)

data class PurchaseCmd(@TargetAggregateIdentifier val id: String, val purchaseValue: Double)
data class PurchasedEvt(val id: String, val purchaseValue: Double)

data class CancelCmd(@TargetAggregateIdentifier val id: String)
data class CancelEvt(val id: String)

@Entity
@NamedQueries(
        NamedQuery(name = "CardSummary.fetch",
                query = "SELECT c FROM CardSummary c WHERE c.id LIKE CONCAT(:idStartsWith, '%') ORDER BY c.id"),
        NamedQuery(name = "CardSummary.count",
                query = "SELECT COUNT(c) FROM CardSummary c WHERE c.id LIKE CONCAT(:idStartsWith, '%')"))

data class CardSummary(@Id var id: String, var limitValue: Double, var remainingValue: Double, val name: String) {
    constructor() : this("", 0.0, 0.0, "")
}

data class CardSummaryFilter(val idStartsWith: String = "")

class CountCardSummariesQuery(val filter: CardSummaryFilter = CardSummaryFilter()) {
    override fun toString() : String = "CountCardSummariesQuery" }

data class CountCardSummariesResponse(val count: Int, val lastEvent: Long)

data class FetchCardSummariesQuery(val offset: Int, val limit: Int, val filter: CardSummaryFilter)

class CountChangedUpdate()