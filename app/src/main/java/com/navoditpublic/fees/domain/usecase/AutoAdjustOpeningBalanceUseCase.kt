package com.navoditpublic.fees.domain.usecase

import com.navoditpublic.fees.domain.repository.FeeRepository
import com.navoditpublic.fees.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UseCase to automatically adjust opening balance when receipts/fees are added to the previous session.
 * 
 * When editing data in the previous session (the session that was migrated to create the current session),
 * this UseCase updates the opening balance in the current session to keep balances consistent.
 * 
 * This should be called after any balance-affecting operation (add/edit/cancel receipt) in a previous session.
 */
@Singleton
class AutoAdjustOpeningBalanceUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val feeRepository: FeeRepository
) {
    
    /**
     * Check if the given session requires auto-adjustment (is it the previous session?).
     * 
     * @param sessionId The session where the change was made
     * @return true if this session is the previous session and auto-adjustment is needed
     */
    suspend fun shouldAutoAdjust(sessionId: Long): Boolean {
        return settingsRepository.isPreviousSession(sessionId)
    }
    
    /**
     * Check if the given session is read-only (older than previous session).
     * 
     * @param sessionId The session to check
     * @return true if this session is read-only
     */
    suspend fun isReadOnly(sessionId: Long): Boolean {
        return settingsRepository.isReadOnlySession(sessionId)
    }
    
    /**
     * Get the session access level for UI purposes.
     * 
     * @param sessionId The session to check
     * @return SessionAccessLevel indicating what operations are allowed
     */
    suspend fun getSessionAccessLevel(sessionId: Long): SessionAccessLevel {
        val currentSession = settingsRepository.getCurrentSession()
        
        return when {
            currentSession == null -> SessionAccessLevel.FULL_ACCESS
            sessionId == currentSession.id -> SessionAccessLevel.FULL_ACCESS
            settingsRepository.isPreviousSession(sessionId) -> SessionAccessLevel.PREVIOUS_SESSION
            else -> SessionAccessLevel.READ_ONLY
        }
    }
    
    /**
     * Perform auto-adjustment of opening balance after a change in the previous session.
     * 
     * @param studentId The student whose balance was affected
     * @param sourceSessionId The session where the change was made (previous session)
     * @return Result with the new opening balance amount, or null if no adjustment needed
     */
    suspend fun adjustOpeningBalance(
        studentId: Long,
        sourceSessionId: Long
    ): Result<Double?> = runCatching {
        // Verify this is the previous session
        if (!settingsRepository.isPreviousSession(sourceSessionId)) {
            return@runCatching null // No adjustment needed for current or older sessions
        }
        
        // Get the current session (target)
        val currentSession = settingsRepository.getCurrentSession()
            ?: return@runCatching null // No current session
        
        // Update the opening balance
        feeRepository.updateOpeningBalanceFromClosingBalance(
            studentId = studentId,
            sourceSessionId = sourceSessionId,
            targetSessionId = currentSession.id
        ).getOrThrow()
    }
    
    /**
     * Get info for the warning dialog when user tries to edit previous session.
     * 
     * @param studentId The student being edited
     * @param sessionId The session being edited
     * @return Warning info including current and projected opening balance
     */
    suspend fun getAdjustmentWarningInfo(
        studentId: Long,
        sessionId: Long
    ): AdjustmentWarningInfo? {
        // Verify this is the previous session
        if (!settingsRepository.isPreviousSession(sessionId)) {
            return null
        }
        
        val currentSession = settingsRepository.getCurrentSession() ?: return null
        val previousSession = settingsRepository.getSessionById(sessionId) ?: return null
        
        // Get current opening balance in current session
        val currentOpeningBalance = feeRepository.getClosingBalanceForSession(
            studentId = studentId,
            sessionId = sessionId
        )
        
        return AdjustmentWarningInfo(
            previousSessionName = previousSession.sessionName,
            currentSessionName = currentSession.sessionName,
            currentOpeningBalance = currentOpeningBalance,
            message = "Changes to ${previousSession.sessionName} will automatically adjust the opening balance in ${currentSession.sessionName}."
        )
    }
}

/**
 * Session access level indicating what operations are allowed.
 */
enum class SessionAccessLevel {
    /** Current session - all operations allowed */
    FULL_ACCESS,
    
    /** Previous session - editable with auto-adjust warning */
    PREVIOUS_SESSION,
    
    /** Older session - read-only, no changes allowed */
    READ_ONLY
}

/**
 * Info for displaying warning when editing previous session.
 */
data class AdjustmentWarningInfo(
    val previousSessionName: String,
    val currentSessionName: String,
    val currentOpeningBalance: Double,
    val message: String
)
