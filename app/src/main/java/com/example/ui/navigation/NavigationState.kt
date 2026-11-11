package com.example.ui.navigation

import com.example.data.model.UserRole
import com.example.data.session.UserSession
import com.example.data.session.UserSessionManager
import com.example.ui.viewmodel.ScreenTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The specific Dashboard View type conditionally rendered based on the user's role upon login.
 */
enum class DashboardViewType {
    ADMIN_DASHBOARD,
    COMMITTEE_DASHBOARD,
    RESIDENT_DASHBOARD;

    companion object {
        fun fromRole(role: UserRole): DashboardViewType {
            return when (role) {
                UserRole.SUPER_ADMIN,
                UserRole.CHAIRMAN -> ADMIN_DASHBOARD
                UserRole.SECRETARY,
                UserRole.TREASURER,
                UserRole.COMMITTEE_MEMBER -> COMMITTEE_DASHBOARD
                UserRole.RESIDENT -> RESIDENT_DASHBOARD
            }
        }
    }
}

/**
 * State representing app navigation, active tab, active role dashboard route,
 * and permission enforcement.
 */
data class NavigationState(
    val currentTab: ScreenTab = ScreenTab.DASHBOARD,
    val activeDashboardView: DashboardViewType = DashboardViewType.ADMIN_DASHBOARD,
    val previousTab: ScreenTab? = null,
    val backStack: List<ScreenTab> = listOf(ScreenTab.DASHBOARD),
    val accessDeniedMessage: String? = null
)

/**
 * Central Navigation State Manager that synchronizes with UserSessionManager
 * and automatically switches the active Dashboard View upon user role login/change.
 */
class AppNavigationManager(
    private val sessionManager: UserSessionManager
) {
    private val _navigationState = MutableStateFlow(
        NavigationState(
            currentTab = ScreenTab.DASHBOARD,
            activeDashboardView = DashboardViewType.fromRole(sessionManager.session.value.currentUser.role)
        )
    )
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    /**
     * Updates navigation destination when a user logs in or switches role.
     * Automatically verifies tab accessibility and configures the appropriate Dashboard view.
     */
    fun onUserSessionChanged(session: UserSession) {
        val targetDashboardView = DashboardViewType.fromRole(session.currentUser.role)
        val currentTab = _navigationState.value.currentTab
        
        val validTab = if (session.accessibleTabs.contains(currentTab)) {
            currentTab
        } else {
            ScreenTab.DASHBOARD
        }

        _navigationState.value = _navigationState.value.copy(
            currentTab = validTab,
            activeDashboardView = targetDashboardView,
            accessDeniedMessage = null
        )
    }

    /**
     * Navigates to a specific screen tab after verifying role permissions.
     * Returns true if navigation succeeded, false if access was denied.
     */
    fun navigateTo(tab: ScreenTab): Boolean {
        val session = sessionManager.session.value
        if (session.accessibleTabs.contains(tab)) {
            val updatedStack = _navigationState.value.backStack.filter { it != tab } + tab
            _navigationState.value = _navigationState.value.copy(
                previousTab = _navigationState.value.currentTab,
                currentTab = tab,
                backStack = updatedStack,
                accessDeniedMessage = null
            )
            return true
        } else {
            _navigationState.value = _navigationState.value.copy(
                accessDeniedMessage = "Access Restricted: You need ${getRequiredRoleForTab(tab)} permissions to view ${tab.name.replace('_', ' ')}."
            )
            return false
        }
    }

    /**
     * Handles back navigation across the app navigation stack.
     */
    fun navigateBack(): Boolean {
        val stack = _navigationState.value.backStack
        if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            val previous = newStack.last()
            _navigationState.value = _navigationState.value.copy(
                currentTab = previous,
                backStack = newStack,
                accessDeniedMessage = null
            )
            return true
        } else if (_navigationState.value.currentTab != ScreenTab.DASHBOARD) {
            _navigationState.value = _navigationState.value.copy(
                currentTab = ScreenTab.DASHBOARD,
                backStack = listOf(ScreenTab.DASHBOARD),
                accessDeniedMessage = null
            )
            return true
        }
        return false
    }

    fun clearAccessDeniedMessage() {
        _navigationState.value = _navigationState.value.copy(accessDeniedMessage = null)
    }

    private fun getRequiredRoleForTab(tab: ScreenTab): String {
        return when (tab) {
            ScreenTab.FINANCIALS,
            ScreenTab.EXPENSES,
            ScreenTab.CORRESPONDENCE,
            ScreenTab.MEETINGS_TASKS,
            ScreenTab.AUDIT_SETTINGS -> "Committee or Admin"
            else -> "Authorized Member"
        }
    }
}
