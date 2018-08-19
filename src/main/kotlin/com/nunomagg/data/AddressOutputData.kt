package com.nunomagg.data

import java.math.BigDecimal

data class AddressOutputData(
        val value: BigDecimal?,
        val tx_hash: String,
        val output_idx: Number?
)