package com.chandeh.app.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chandeh.app.data.Prefs
import com.chandeh.app.data.PriceItem
import com.chandeh.app.data.PriceRepository
import com.chandeh.app.util.toFaDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PricesUiState(
    val items: List<PriceItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** ساعت آخرین دریافت موفق توسط خود برنامه (به‌روزرسانی هر ۶۰ ثانیه) */
    val lastFetchAt: String? = null,
    /** true یعنی حداقل یک قیمت از کش (ذخیره‌شده) آمده، نه زنده */
    val hasStale: Boolean = false
)

/** فاصله‌ی به‌روزرسانی خودکار داخل برنامه (میلی‌ثانیه) */
private const val AUTO_REFRESH_MS = 60_000L

class PricesViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs =
        app.getSharedPreferences(Prefs.NAME, Context.MODE_PRIVATE)

    /** هر بار رپوی تازه تا کلید BRSِ جدید تنظیمات هم اعمال شود */
    private fun newRepo() = PriceRepository(
        brsApiKey = prefs.getString(Prefs.KEY_BRS_API, null),
        prefs = prefs
    )

    private val _state = MutableStateFlow(PricesUiState())
    val state: StateFlow<PricesUiState> = _state

    init {
        refresh()
        startAutoRefresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            fetchAndPublish()
        }
    }

    /** به‌روزرسانی خودکار و بی‌صدا — بدون نمایش لودینگ؛ هیچ‌وقت نمی‌میرد */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(AUTO_REFRESH_MS)
                try {
                    fetchAndPublish()
                } catch (e: Exception) {
                    // خطای غیرمنتظره حلقه را نکشد؛ ۶۰ ثانیه بعد دوباره تلاش می‌شود
                }
            }
        }
    }

    private suspend fun fetchAndPublish() {
        newRepo().fetchPrices()
            .onSuccess { items ->
                _state.value = PricesUiState(
                    items = items,
                    isLoading = false,
                    lastFetchAt = nowFaTime(),
                    hasStale = items.any { it.isStale }
                )
            }
            .onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "خطا در دریافت قیمت‌ها"
                )
            }
    }

    private fun nowFaTime(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
        return sdf.format(java.util.Date()).toFaDigits()
    }
}
