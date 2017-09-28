package com.backend.data.api.domain

import com.fasterxml.jackson.annotation.JsonInclude


@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductDTO constructor(
        var id: String?,
        var name: String?,
        var description: String? = null,
        var type: ProductTypeDTO? = null,
        var price: Price? = Price(0, 0, BAHT)
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductTypeDTO constructor(
        var id: String?,
        var name: String?,
        var description: String? = null
)