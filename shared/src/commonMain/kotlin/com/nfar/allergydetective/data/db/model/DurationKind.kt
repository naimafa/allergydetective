package com.nfar.allergydetective.data.db.model

/**
 * How long a complaint lasted, optional (DB version 9, feedback 20.11.). Stored by name, so new values need no
 * migration. User 25.11.2026: 3–6 h and 6–12 h; H3_TO_12 stays for entries saved before.
 */
enum class DurationKind { UNDER_1H, H1_TO_3, H3_TO_12, OVER_12H, H3_TO_6, H6_TO_12 }
