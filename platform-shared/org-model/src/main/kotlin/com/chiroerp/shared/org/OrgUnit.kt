package com.chiroerp.shared.org

/**
 * Organizational unit value object.
 * Represents any node in the org hierarchy (company, division, department, cost center, etc.).
 * 
 * Related ADRs: ADR-006, ADR-045
 */
data class OrgUnit(
    val id: String,                      // Unique identifier (e.g., "CC-100", "DIV-WEST")
    val code: String,                    // Business code (e.g., "100", "WEST")
    val name: String,                    // Display name (e.g., "Marketing Department")
    val type: OrgUnitType,               // Type of org unit
    val parentId: String?,               // Parent org unit ID (null if root)
    val level: Int,                      // Depth in hierarchy (0 = root)
    val isActive: Boolean = true,        // Active status
    val attributes: Map<String, String> = emptyMap()  // Flexible attributes
) {
    /**
     * Check if this org unit is a descendant of another.
     * 
     * @param ancestorId Potential ancestor org unit ID
     * @param hierarchyService Service to query hierarchy
     * @return true if this unit is under ancestorId
     */
    suspend fun isDescendantOf(ancestorId: String, hierarchyService: OrgHierarchyService): Boolean {
        val pathResult = hierarchyService.getPath(this.id)
        return pathResult.getOrNull()?.any { it.id == ancestorId } ?: false
    }
}

/**
 * Types of organizational units.
 */
enum class OrgUnitType {
    /**
     * Legal entity (company code in SAP terminology).
     * Top-level organizational unit for financial reporting.
     */
    COMPANY,
    
    /**
     * Business division (product line, geography, etc.).
     */
    DIVISION,
    
    /**
     * Department within a division.
     */
    DEPARTMENT,
    
    /**
     * Cost center for expense tracking.
     */
    COST_CENTER,
    
    /**
     * Profit center for P&L reporting.
     */
    PROFIT_CENTER,
    
    /**
     * Sales organization.
     */
    SALES_ORG,
    
    /**
     * Plant/warehouse location.
     */
    PLANT,
    
    /**
     * Custom organizational unit type.
     */
    CUSTOM
}
