package ffc.airsync.api.services

import ffc.entity.Entity

/**
 * ให้ Dao ที่ต้องการความสามารถในการ Sync สืบทอด ในตอนนี้ใช้คู่กับ
 * @see MongoSyncDao
 */
interface SyncDao<T : Entity> {
    /**
     * ใส่ข้อมูลพร้อมกับแป๊ะ Label block เอาไว้ด้วย
     * @param block หมายเลข block
     */
    fun insertBlock(orgId: String, block: Int, item: List<T>): List<T>

    /**
     * ดึงข้อมูลตามหมายเลข block
     */
    fun getBlock(orgId: String, block: Int): List<T>

    /**
     * การ confirm ใช้เมื่อ มีการใส่ข้อมูลเสร็จสิ้น
     * จะเป็นการถอด Label block ออกจาก Object
     * จะไม่สามารถ getBlock ด้วยหมายเลข block ที่ถอดออกได้
     * จะมีสถานะเป็น Object ปกติ
     */
    fun confirmBlock(orgId: String, block: Int)

    /**
     * จะเป็นการลบ Object ที่มี Label block ตามที่ระบุ
     * ใช้ในกรณีที่เกิดความผิดพลาดในการใส่ข้อมูลใน block
     */
    fun unConfirmBlock(orgId: String, block: Int)
}
