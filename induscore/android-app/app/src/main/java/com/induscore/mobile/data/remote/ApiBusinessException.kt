package com.induscore.mobile.data.remote

class ApiBusinessException(
    val code: Int,
    override val message: String
) : RuntimeException(message)
