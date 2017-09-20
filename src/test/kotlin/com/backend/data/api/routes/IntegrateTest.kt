package com.backend.data.api.routes

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.AfterClass
import org.junit.BeforeClass

abstract class IntegrateTest {

    companion object {

        lateinit var Gson: Gson

        @BeforeClass
        @JvmStatic fun setUp(){
            Gson = GsonBuilder().setPrettyPrinting().create()
        }

        @AfterClass
        @JvmStatic fun tearDown() {
            print("tear down!!!")
        }
    }
}