package com.nfar.allergydetective.data.db.model

/**
 * Raw or cooked, only stored for ingredients where cooking changes the triggers (DB version 5). User 25.11.2026: or
 * GLUTEN_FREE for the gluten-free version of a product; stored by name, so no migration.
 */
enum class PreparationKind { RAW, COOKED, GLUTEN_FREE }
