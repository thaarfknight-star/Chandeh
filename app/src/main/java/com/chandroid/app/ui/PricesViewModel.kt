package com.chandroid.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandroid.app.data.PriceItem
import com.chandroid.app.data.PriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PricesUiState(
    val items: List<PriceItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class PricesViewModel(
    private val repo: PriceRepository = PriceRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(PricesUiState())
    val state: StateFlow<PricesUiState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
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
}
