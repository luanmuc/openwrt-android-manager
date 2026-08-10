package com.luanmuc.openwrtmanager.util

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

/**
 * 生物识别工具类
 * 提供指纹/面容识别相关的工具函数
 */
object BiometricUtils {
    
    /**
     * 检查是否支持生物识别
     */
    fun isBiometricSupported(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否有已注册的生物识别
     */
    fun hasEnrolledBiometrics(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否支持强生物识别（指纹/面容）
     */
    fun isStrongBiometricSupported(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否支持弱生物识别（如面部识别）
     */
    fun isWeakBiometricSupported(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查是否支持设备凭据（PIN/图案/密码）
     */
    fun isDeviceCredentialSupported(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取生物识别类型
     */
    fun getBiometricType(context: Context): BiometricType {
        return when {
            isStrongBiometricSupported(context) -> BiometricType.STRONG
            isWeakBiometricSupported(context) -> BiometricType.WEAK
            isDeviceCredentialSupported(context) -> BiometricType.DEVICE_CREDENTIAL
            else -> BiometricType.NONE
        }
    }
    
    /**
     * 生物识别类型枚举
     */
    enum class BiometricType {
        NONE,               // 不支持
        STRONG,             // 强生物识别（指纹/面容）
        WEAK,               // 弱生物识别（面部识别等）
        DEVICE_CREDENTIAL   // 设备凭据
    }
    
    /**
     * 获取生物识别状态描述
     */
    fun getBiometricStatus(context: Context): String {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            
            when (result) {
                BiometricManager.BIOMETRIC_SUCCESS -> "生物识别可用"
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "设备没有生物识别硬件"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "生物识别硬件当前不可用"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "没有注册的生物识别"
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "需要安全更新"
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "不支持的生物识别"
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "状态未知"
                else -> "未知状态: $result"
            }
        } catch (e: Exception) {
            "错误: ${e.message}"
        }
    }
    
    /**
     * 显示生物识别提示
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "生物识别验证",
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "取消",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .build()
            
            val biometricPrompt = BiometricPrompt(
                activity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errorCode, errString)
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onFailed()
                    }
                }
            )
            
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(-1, e.message ?: "未知错误")
        }
    }
    
    /**
     * 显示带设备凭据的生物识别提示
     */
    fun showBiometricPromptWithCredential(
        activity: FragmentActivity,
        title: String = "验证身份",
        subtitle: String = "",
        description: String = "",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        try {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
            
            val biometricPrompt = BiometricPrompt(
                activity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errorCode, errString)
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onFailed()
                    }
                }
            )
            
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(-1, e.message ?: "未知错误")
        }
    }
    
    /**
     * 检查是否应该使用生物识别
     * 综合考虑设备支持和用户设置
     */
    fun shouldUseBiometric(context: Context, userEnabled: Boolean): Boolean {
        return userEnabled && isBiometricSupported(context) && hasEnrolledBiometrics(context)
    }
    
    /**
     * 获取推荐的验证方式
     */
    fun getRecommendedAuthMethod(context: Context): AuthMethod {
        return when {
            isStrongBiometricSupported(context) -> AuthMethod.BIOMETRIC_STRONG
            isWeakBiometricSupported(context) -> AuthMethod.BIOMETRIC_WEAK
            isDeviceCredentialSupported(context) -> AuthMethod.DEVICE_CREDENTIAL
            else -> AuthMethod.NONE
        }
    }
    
    /**
     * 验证方式枚举
     */
    enum class AuthMethod {
        NONE,               // 无
        BIOMETRIC_STRONG,   // 强生物识别
        BIOMETRIC_WEAK,     // 弱生物识别
        DEVICE_CREDENTIAL,  // 设备凭据
        PASSWORD            // 密码
    }
    
    /**
     * 获取Android版本支持的生物识别特性
     */
    fun getBiometricFeatures(): List<String> {
        val features = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            features.add("BiometricPrompt API")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            features.add("生物识别强度分类")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            features.add("设备凭据回退")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            features.add("更安全的生物识别")
        }
        
        return features
    }
}