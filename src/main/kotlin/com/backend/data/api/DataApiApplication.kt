package com.backend.data.api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories



@SpringBootApplication
@EnableMongoRepositories
class DataApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(DataApiApplication::class.java, *args)
}
