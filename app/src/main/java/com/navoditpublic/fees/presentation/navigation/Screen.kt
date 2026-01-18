package com.navoditpublic.fees.presentation.navigation

/**
 * Sealed class representing all screens in the app
 * 
 * Navigation Graph Structure (Per-Tab Back Stacks):
 * - Each bottom nav tab has its own nested graph
 * - Child screens belong to their parent tab's graph
 * - Back navigation works within each tab independently
 */
sealed class Screen(val route: String) {
    // Navigation Graph Routes (for nested graphs)
    data object HomeGraph : Screen("home_graph")
    data object StudentsGraph : Screen("students_graph")
    data object CollectGraph : Screen("collect_graph")
    data object LedgerGraph : Screen("ledger_graph")
    data object ReportsGraph : Screen("reports_graph")
    
    // Main screens (bottom nav - start destinations of each graph)
    data object Dashboard : Screen("dashboard")
    data object Students : Screen("students")
    data object FeeCollection : Screen("fee_collection")
    data object Ledger : Screen("ledger")
    data object Reports : Screen("reports")
    
    // Ledger screens
    data object LedgerClass : Screen("ledger/{className}") {
        fun createRoute(className: String) = "ledger/$className"
    }
    
    // Student screens
    data object StudentDetail : Screen("student/{studentId}") {
        fun createRoute(studentId: Long) = "student/$studentId"
    }
    data object AddEditStudent : Screen("student/edit?studentId={studentId}") {
        fun createRoute(studentId: Long? = null) = 
            if (studentId != null) "student/edit?studentId=$studentId" else "student/edit"
    }
    data object StudentLedger : Screen("student/{studentId}/ledger") {
        fun createRoute(studentId: Long) = "student/$studentId/ledger"
    }
    data object StudentReceipts : Screen("student/{studentId}/receipts") {
        fun createRoute(studentId: Long) = "student/$studentId/receipts"
    }
    data object StudentListByClass : Screen("students/{className}/{section}") {
        fun createRoute(className: String, section: String) = "students/$className/$section"
    }
    
    // Fee Collection screens
    data object CollectFee : Screen("collect_fee?studentId={studentId}") {
        fun createRoute(studentId: Long? = null) = 
            if (studentId != null) "collect_fee?studentId=$studentId" else "collect_fee"
    }
    data object ReceiptDetail : Screen("receipt/{receiptId}") {
        fun createRoute(receiptId: Long) = "receipt/$receiptId"
    }
    
    // Transport Quick Management
    // action: "manage" for enrolled students (shows Change/Stop/History), "enroll" for students without transport
    data object TransportQuick : Screen("transport_quick?studentId={studentId}&action={action}") {
        fun createRoute(studentId: Long? = null, action: String? = null): String {
            // Always include both parameters to match the route pattern
            // Use defaults that match NavGraph argument defaults (-1L for studentId, "" for action)
            val studentIdParam = studentId ?: -1L
            val actionParam = action ?: ""
            return "transport_quick?studentId=$studentIdParam&action=$actionParam"
        }
    }
    
    // Settings screens
    data object Settings : Screen("settings")
    data object SchoolProfile : Screen("settings/school_profile")
    data object AcademicSessions : Screen("settings/academic_sessions")
    data object FeeStructure : Screen("settings/fee_structure")
    data object TransportRoutes : Screen("settings/transport_routes")
    data object ClassesAndSections : Screen("settings/classes_sections")
    data object AuditLog : Screen("settings/audit_log")
    data object HowToUse : Screen("settings/how_to_use")
    data object About : Screen("settings/about")
    
    // Report screens - Collection
    data object DailyCollectionReport : Screen("reports/daily_collection?date={date}") {
        fun createRoute(date: Long? = null) = 
            if (date != null) "reports/daily_collection?date=$date" else "reports/daily_collection"
    }
    data object MonthlyCollectionReport : Screen("reports/monthly_collection")
    data object ReceiptRegister : Screen("reports/receipt_register")
    data object CustomCollectionReport : Screen("reports/custom_collection")
    
    // Report screens - Dues
    data object DefaultersReport : Screen("reports/defaulters")
    data object ClassWiseDuesReport : Screen("reports/class_wise_dues")
    data object TransportDuesReport : Screen("reports/transport_dues")
    data object CustomDuesReport : Screen("reports/custom_dues")
    data object SavedDuesViews : Screen("reports/saved_views")
    
    // Legacy - redirect to new
    data object ClassWiseReport : Screen("reports/class_wise_dues")
    data object CustomReport : Screen("reports/custom_collection")
}


