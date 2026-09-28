package com.chandeh.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandeh.app.data.PriceItem
import com.chandeh.app.data.PriceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PricesUiState(
    val items: List<PriceItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/** فاصله‌ی به‌روزرسانی خودکار داخل برنامه (میلی‌ثانیه) */
private const val AUTO_REFRESH_MS = 60_000L

class PricesViewModel(
    private val repo: PriceRepository = PriceRepository()
) : ViewModel() {

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

    /** به‌روزرسانی خودکار و بی‌صدا — بدون نمایش لودینگ */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(AUTO_REFRESH_MS)
                fetchAndPublish()
            }
        }
    }

    private suspend fun fetchAndPublish() {
        repo.fetchPrices()
            .onSuccess { items ->
                _state.value = PricesUiState(items = items, isLoading = false)
            }
            .onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "خطا در دریافت قیمت‌ها"
                )
            }
    }
}
