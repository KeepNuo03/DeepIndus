package com.induscore.mobile.ui.state

import com.induscore.mobile.data.remote.ApiBusinessException
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(defaultMessage: String): String {
    return when (this) {
        is ApiBusinessException -> when (code) {
            401 -> "登录已过期，请重新登录"
            403 -> "当前账号无权限访问该功能"
            404 -> "目标资源不存在，请刷新后重试"
            in 500..599 -> "服务器异常，请稍后重试"
            else -> message.ifBlank { defaultMessage }
        }
        is HttpException -> when (code()) {
            401 -> "登录已过期，请重新登录"
            403 -> "当前账号无权限访问该功能"
            404 -> "请求地址不存在，请检查版本或稍后重试"
            in 500..599 -> "服务器异常，请稍后重试"
            else -> message()
        }
        is IOException -> "网络连接异常，请检查网络后重试"
        else -> message ?: defaultMessage
    }
}
