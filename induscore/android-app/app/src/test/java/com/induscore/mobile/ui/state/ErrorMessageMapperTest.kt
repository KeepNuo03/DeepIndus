package com.induscore.mobile.ui.state

import com.induscore.mobile.data.remote.ApiBusinessException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErrorMessageMapperTest {

    @Test
    fun `api business exception should map to friendly messages`() {
        assertEquals("登录已过期，请重新登录", ApiBusinessException(401, "x").toUserMessage("默认错误"))
        assertEquals("当前账号无权限访问该功能", ApiBusinessException(403, "x").toUserMessage("默认错误"))
        assertEquals("目标资源不存在，请刷新后重试", ApiBusinessException(404, "x").toUserMessage("默认错误"))
        assertEquals("服务器异常，请稍后重试", ApiBusinessException(500, "x").toUserMessage("默认错误"))
    }

    @Test
    fun `http and io exceptions should map correctly`() {
        val body = "{}".toResponseBody("application/json".toMediaType())
        val response = Response.error<Any>(404, body)
        val httpException = HttpException(response)
        assertEquals("请求地址不存在，请检查版本或稍后重试", httpException.toUserMessage("默认错误"))
        assertEquals("网络连接异常，请检查网络后重试", IOException("timeout").toUserMessage("默认错误"))
    }
}
