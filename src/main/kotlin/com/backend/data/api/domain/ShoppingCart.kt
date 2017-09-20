package com.backend.data.api.domain

import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by jakkritsittiwerapong on 9/11/2017 AD.
 */

@Document(collection="Action")
data class Action @PersistenceConstructor constructor(val id: String, val name: String);

data class OrderEntry @PersistenceConstructor constructor(val id: String, val quantity: Integer)