package com.suprogramuota_visata.vedlys.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.suprogramuota_visata.api.ApiSvClient
import com.suprogramuota_visata.api.domain.models.LoginRequest
import com.suprogramuota_visata.api.domain.models.RegisterRequest
import com.suprogramuota_visata.api.domain.util.ApiResult
import kotlinx.coroutines.launch

class AuthViewModel(private val apiClient: ApiSvClient) : BaseViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var lastSuccessMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String, onSuccess: (Boolean) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "El. paštas ir slaptažodis yra privalomi."
            return
        }
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = apiClient.authRepository.login(LoginRequest(email, password))) {
                is ApiResult.Success -> {
                    isLoading = false
                    val isAdmin = result.data.role == "ADMIN"
                    onSuccess(isAdmin)
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = "Prisijungti nepavyko: ${result.message}"
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Visi laukai yra privalomi."
            return
        }
        if (password.length < 6) {
            errorMessage = "Slaptažodis turi būti bent 6 simbolių ilgio."
            return
        }
        scope.launch {
            isLoading = true
            errorMessage = null
            when (val result = apiClient.authRepository.register(
                RegisterRequest(name, email, password, null)
            )) {
                is ApiResult.Success -> {
                    isLoading = false
                    lastSuccessMessage = "Registracija sėkminga. Galite prisijungti."
                    onSuccess()
                }
                is ApiResult.Error -> {
                    isLoading = false
                    errorMessage = "Registracija nepavyko: ${result.message}"
                }
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
