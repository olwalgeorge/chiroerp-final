package com.chiroerp.shared.org

/**
 * Interface for organizational hierarchy queries.
 * 
 * Phase 0: Hardcoded org structure (single company, few cost centers)
 * Phase 1: Database-driven with tree queries (parent-child relationships)
 * Phase 2: AI-powered org analytics and recommendation
 * 
 * Related ADRs: ADR-006, ADR-045
 */
interface OrgHierarchyService {
    /**
     * Get organizational unit by ID.
     * 
     * @param orgUnitId Unique identifier
     * @return OrgUnit or null if not found
     */
    suspend fun getOrgUnit(orgUnitId: String): Result<OrgUnit?>
    
    /**
     * Get parent organizational unit.
     * 
     * @param orgUnitId Child org unit ID
     * @return Parent OrgUnit or null if root
     */
    suspend fun getParent(orgUnitId: String): Result<OrgUnit?>
    
    /**
     * Get all children of an organizational unit.
     * 
     * @param orgUnitId Parent org unit ID
     * @param recursive If true, get entire subtree
     * @return List of child OrgUnits
     */
    suspend fun getChildren(orgUnitId: String, recursive: Boolean = false): Result<List<OrgUnit>>
    
    /**
     * Get path from root to given org unit.
     * 
     * @param orgUnitId Target org unit ID
     * @return List of OrgUnits from root to target
     */
    suspend fun getPath(orgUnitId: String): Result<List<OrgUnit>>
    
    /**
     * Check if user has access to data from org unit.
     * 
     * @param userId User identifier
     * @param orgUnitId Org unit to check
     * @return true if user has access (direct or inherited)
     */
    suspend fun hasAccess(userId: String, orgUnitId: String): Result<Boolean>
    
    /**
     * Get all org units accessible by user (for data filtering).
     * 
     * @param userId User identifier
     * @return List of OrgUnit IDs user can access
     */
    suspend fun getAccessibleOrgUnits(userId: String): Result<Set<String>>
}
