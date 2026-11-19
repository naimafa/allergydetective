package com.nfar.allergydetective.data.db.model

/** Does a complaint on a sick day belong to the illness? Null = not answered (DB version 7, self-test 19.11.). */
enum class IllnessRelationKind { RELATED, UNRELATED, UNKNOWN }
