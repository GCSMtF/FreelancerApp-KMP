package com.xommore.freelancerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null,
    val passwordResetSent: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        _authState.value = AuthState(
            isLoggedIn = currentUser != null,
            user = currentUser
        )
    }

    // 회원가입
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            try {
                val result = auth.createUserWithEmailAndPassword(email, password)
                _authState.value = AuthState(
                    isLoading = false,
                    isLoggedIn = true,
                    user = result.user
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = getErrorMessage(e)
                )
            }
        }
    }

    // 로그인
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            try {
                val result = auth.signInWithEmailAndPassword(email, password)
                _authState.value = AuthState(
                    isLoading = false,
                    isLoggedIn = true,
                    user = result.user
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = getErrorMessage(e)
                )
            }
        }
    }

    // 로그아웃
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState(
                    isLoggedIn = false,
                    user = null
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    error = getErrorMessage(e)
                )
            }
        }
    }

    // 비밀번호 재설정 이메일 발송
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, passwordResetSent = false)

            try {
                auth.sendPasswordResetEmail(email)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    passwordResetSent = true
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = getErrorMessage(e)
                )
            }
        }
    }

    // 에러 클리어
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun refreshAuthState() {
        val currentUser = auth.currentUser
        _authState.value = AuthState(
            isLoggedIn = currentUser != null,
            user = currentUser
        )
    }

    // 비밀번호 재설정 상태 클리어
    fun clearPasswordResetSent() {
        _authState.value = _authState.value.copy(passwordResetSent = false)
    }

    // 에러 메시지 한글화
    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("email address is badly formatted") == true ->
                "이메일 형식이 올바르지 않습니다"
            e.message?.contains("password is invalid") == true ->
                "비밀번호가 올바르지 않습니다"
            e.message?.contains("no user record") == true ->
                "등록되지 않은 이메일입니다"
            e.message?.contains("email address is already in use") == true ->
                "이미 사용 중인 이메일입니다"
            e.message?.contains("Password should be at least 6 characters") == true ->
                "비밀번호는 6자 이상이어야 합니다"
            e.message?.contains("network error") == true ->
                "네트워크 연결을 확인해주세요"
            e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
                "이메일 또는 비밀번호가 올바르지 않습니다"
            else -> e.message ?: "오류가 발생했습니다"
        }
    }
}