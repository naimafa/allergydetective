package com.nfar.allergydetective.data.db.model

/** Confirmation state of a day (TRD 1.3). Only NO_SYMPTOMS and COMPLETE days are analysed. */
enum class DayStatus { OPEN, NO_SYMPTOMS, COMPLETE }
