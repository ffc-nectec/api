package ffc.airsync.api.services

interface SyncDao<T> {
    fun createBlock(orgId: String, block: Int, item: List<T>): List<T>
    fun confirmBlock(orgId: String, block: Int)
    fun unConfirmBlock(orgId: String, block: Int)
    fun getBlock(orgId: String, block: Int): List<T>
}
