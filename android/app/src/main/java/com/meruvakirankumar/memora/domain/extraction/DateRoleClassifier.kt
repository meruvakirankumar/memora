package com.meruvakirankumar.memora.domain.extraction

import com.meruvakirankumar.memora.domain.model.EventType

/** Maps a line's label keyword to a date role and (where known) a specific event type. */
internal object DateRoleClassifier {

    fun classify(upperLine: String): Pair<DateRole, EventType?> = when {
        MANUFACTURE.containsMatchIn(upperLine) -> DateRole.MANUFACTURE to null
        USE_BY.containsMatchIn(upperLine) -> DateRole.EXPIRY to EventType.USE_BY
        BEST_BEFORE.containsMatchIn(upperLine) -> DateRole.EXPIRY to EventType.BEST_BEFORE
        EXPIRY.containsMatchIn(upperLine) -> DateRole.EXPIRY to EventType.EXPIRY
        BILL_DUE.containsMatchIn(upperLine) -> DateRole.BILL_DUE to EventType.BILL_DUE
        RETURN.containsMatchIn(upperLine) -> DateRole.RETURN_DEADLINE to EventType.RETURN_DEADLINE
        WARRANTY.containsMatchIn(upperLine) -> DateRole.WARRANTY to EventType.WARRANTY_END
        SUBSCRIPTION.containsMatchIn(upperLine) -> DateRole.SUBSCRIPTION to EventType.SUBSCRIPTION_END
        DOCUMENT.containsMatchIn(upperLine) -> DateRole.DOCUMENT to EventType.DOCUMENT_EXPIRY
        else -> DateRole.UNLABELED to null
    }

    private val MANUFACTURE = Regex("\\b(MFG|MANUFACTUR\\w*|PACKED|PKD|PROD(?:UCED)?|MADE ON)\\b")
    private val USE_BY = Regex("USE BY")
    private val BEST_BEFORE = Regex("BEST BEFORE|BEST BY|\\bBB\\b|BBE")
    private val EXPIRY = Regex("\\bEXP\\w*|VALID (?:UNTIL|THRU|TILL)")
    private val BILL_DUE = Regex("PAY BY|AMOUNT DUE|DUE DATE|\\bDUE\\b|\\bBILL\\b|INVOICE")
    private val RETURN = Regex("RETURN BY|\\bRETURN\\b|EXCHANGE BY")
    private val WARRANTY = Regex("WARRANTY|GUARANTEE")
    private val SUBSCRIPTION = Regex("SUBSCRIPTION|RENEW\\w*|NEXT BILLING")
    private val DOCUMENT = Regex("PASSPORT|LICEN[SC]E")
}
