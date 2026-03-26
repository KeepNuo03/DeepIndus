package com.induscore.mobile.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.GsonBuilder
import com.induscore.mobile.config.AppConfig
import com.induscore.mobile.data.SessionManager
import com.induscore.mobile.data.local.AppDatabase
import com.induscore.mobile.data.remote.AuthInterceptor
import com.induscore.mobile.data.remote.MobileApiService
import com.induscore.mobile.domain.AuthRepository
import com.induscore.mobile.domain.MobileRepository
import com.induscore.mobile.ml.LocalYoloEngine
import com.induscore.mobile.ml.NoOpLocalYoloEngine
import com.induscore.mobile.ml.TfliteLocalYoloEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceLocator {
    private lateinit var appContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val authInterceptor by lazy { AuthInterceptor() }

    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS upload_operation_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    actionType TEXT NOT NULL,
                    targetScope TEXT NOT NULL,
                    affectedCount INTEGER NOT NULL,
                    success INTEGER NOT NULL,
                    message TEXT,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "induscore_mobile.db"
        ).addMigrations(migration1To2).build()
    }

    val sessionManager: SessionManager by lazy {
        SessionManager(database.authSessionDao())
    }

    val mobileApiService: MobileApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()
            .create(MobileApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(mobileApiService, sessionManager)
    }

    val mobileRepository: MobileRepository by lazy {
        MobileRepository(
            mobileApiService = mobileApiService,
            reviewTaskCacheDao = database.reviewTaskCacheDao(),
            uploadQueueDao = database.uploadQueueDao(),
            uploadOperationLogDao = database.uploadOperationLogDao()
        )
    }

    val localYoloEngine: LocalYoloEngine by lazy {
        if (!AppConfig.LOCAL_YOLO_ENABLED) {
            NoOpLocalYoloEngine("本地 YOLO 未启用（LOCAL_YOLO_ENABLED=false）")
        } else {
            runCatching {
                TfliteLocalYoloEngine(
                    context = appContext,
                    modelAssetPath = AppConfig.LOCAL_YOLO_MODEL_ASSET,
                    labelsAssetPath = AppConfig.LOCAL_YOLO_LABELS_ASSET,
                    inputSize = AppConfig.LOCAL_YOLO_INPUT_SIZE,
                    confThreshold = AppConfig.LOCAL_YOLO_CONF_THRESHOLD,
                    iouThreshold = AppConfig.LOCAL_YOLO_IOU_THRESHOLD,
                    maxResults = AppConfig.LOCAL_YOLO_MAX_RESULTS
                )
            }.getOrElse { error ->
                NoOpLocalYoloEngine("本地 YOLO 初始化失败，已降级：${error.message}")
            }
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        // Keep auth header in sync after process restart.
        scope.launch {
            runCatching {
                val token = sessionManager.getToken()
                authInterceptor.updateToken(token)
            }
        }
    }

    fun updateAuthToken(token: String?) {
        authInterceptor.updateToken(token)
    }

    fun getAppContext(): Context = appContext
}
