package ffc.airsync.api.services.house

import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.mock
import org.junit.Test

class HouseServiceTest {

    val dao = mock<HouseDao>()

    @Test
    fun getHouses() {
        with(HouseService(dao)) {
            getHouses("1", "12/9 3")
            getHouses("1", "12/9   3")
            getHouses("1", "2039")
        }

        verify(dao).findAll("1", "12/9", null, villageName = "3")
        verify(dao).findAll("1", "12/9", null, villageName = "3")
        verify(dao).findAll("1", "2039", null, villageName = null)
    }
}
