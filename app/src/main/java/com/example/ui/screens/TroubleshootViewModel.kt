package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetTroubleshootingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TroubleshootUiState {
    object Idle : TroubleshootUiState
    object Loading : TroubleshootUiState
    data class Success(val response: String) : TroubleshootUiState
    data class Error(val message: String) : TroubleshootUiState
}

class TroubleshootViewModel(
    private val getTroubleshootingUseCase: GetTroubleshootingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TroubleshootUiState>(TroubleshootUiState.Idle)
    val uiState: StateFlow<TroubleshootUiState> = _uiState.asStateFlow()

    private val _complaintText = MutableStateFlow("")
    val complaintText: StateFlow<String> = _complaintText.asStateFlow()

    fun onComplaintChange(newText: String) {
        _complaintText.value = newText
    }

    fun getTroubleshooting() {
        val complaint = _complaintText.value
        if (complaint.isBlank()) return

        viewModelScope.launch {
            _uiState.value = TroubleshootUiState.Loading
            getTroubleshootingUseCase(complaint)
                .onSuccess { response ->
                    _uiState.value = TroubleshootUiState.Success(response)
                }
                .onFailure { error ->
                    _uiState.value = TroubleshootUiState.Error(
                        error.localizedMessage ?: "Terjadi kesalahan yang tidak diketahui"
                    )
                }
        }
    }

    fun clearState() {
        _complaintText.value = ""
        _uiState.value = TroubleshootUiState.Idle
    }

    class Factory(
        private val getTroubleshootingUseCase: GetTroubleshootingUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TroubleshootViewModel::class.java)) {
                return TroubleshootViewModel(getTroubleshootingUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
