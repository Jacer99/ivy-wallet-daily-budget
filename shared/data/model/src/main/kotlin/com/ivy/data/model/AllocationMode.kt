package com.ivy.data.model

/**
 * How an expense is attributed to the daily safe-to-spend allowance.
 *
 * This affects budget guidance only. It does NOT affect actual
 * accounting — the account balance is debited by the full amount
 * regardless of which mode is selected.
 */
enum class AllocationMode {
    /** Full amount on the transaction date. Default for new expenses. */
    TODAY,
    /** Split evenly across transaction date → Sunday (clipped to period end). */
    WEEK,
    /** Split evenly across transaction date → last day of month (clipped to period end). */
    MONTH
}