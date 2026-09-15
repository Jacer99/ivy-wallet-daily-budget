package com.ivy.legacy.datamodel

import com.ivy.base.legacy.Transaction
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.model.AllocationMode

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    accountId = accountId,
    type = type,
    amount = amount.toDouble(),
    toAccountId = toAccountId,
    toAmount = toAmount.toDouble(),
    title = title,
    description = description,
    dateTime = dateTime,
    categoryId = categoryId,
    dueDate = dueDate,
    recurringRuleId = recurringRuleId,
    paidForDateTime = paidFor,
    attachmentUrl = attachmentUrl,
    loanId = loanId,
    loanRecordId = loanRecordId,
    allocationMode = allocationMode.parseAllocationMode(),
    id = id,
    isSynced = isSynced,
    isDeleted = isDeleted
)

/**
 * Safely parses the legacy String allocationMode into the domain enum.
 * Falls back to TODAY if the value is unrecognized (e.g. corrupt DB row).
 */
private fun String.parseAllocationMode(): AllocationMode =
    AllocationMode.entries.find { it.name == this } ?: AllocationMode.TODAY