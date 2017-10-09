package com.backend.data.api.handler

import com.backend.data.api.domain.BinaryFile
import com.backend.data.api.repository.BinaryDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

@Component
class BinaryDataHandler(@Autowired val binaryDataRepo: BinaryDataRepository) {

    fun getBytesData(req: ServerRequest): Mono<ServerResponse> {
        val notFound = ServerResponse.notFound().build()
        val id = req.pathVariable("id");
        return binaryDataRepo.findById(id).flatMap { binData ->
            val binFile = BinaryFile(binData)
            ServerResponse.ok().body(Mono.just(binFile.getFileInBytes()))
        }.switchIfEmpty(notFound)
    }
}