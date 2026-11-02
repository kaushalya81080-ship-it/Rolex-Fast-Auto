package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.RolexDatabase
import com.example.data.repository.RolexRepository
import com.example.models.LicenseStatus
import com.example.models.LicenseType
import com.example.simulator.CarromSimulatorEngine
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: RolexDatabase
    private lateinit var repository: RolexRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RolexDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RolexRepository(database.rolexDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context matches Rolex Fast Auto`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rolex Fast Auto", appName)
    }

    @Test
    fun `autoPlay key format starts with RFAX-PLAY`() = runBlocking {
        val key = repository.generateAutoPlayKey(durationDays = 7, customerNote = "Test Player")
        assertTrue(key.startsWith("RFAX-PLAY-"))
    }

    @Test
    fun `admin key format starts with RFAX-ADM and costs 500`() = runBlocking {
        val key = repository.generateAdminLicenseKey(setupCost = 500, durationDays = 365)
        assertTrue(key.startsWith("RFAX-ADM-"))
    }

    @Test
    fun `autoplay key cannot activate admin panel`() = runBlocking {
        val playKey = repository.generateAutoPlayKey(durationDays = 7)
        val result = repository.activateKey(
            keyInput = playKey,
            customerId = "test_user",
            customerName = "Tester",
            deviceId = "test_device"
        )
        assertTrue(result.isSuccess)
        val (type, _) = result.getOrThrow()
        // Must activate as AUTOPLAY, not ADMIN_PANEL!
        assertEquals(LicenseType.AUTOPLAY, type)
    }

    @Test
    fun `admin key activates as admin panel license`() = runBlocking {
        val admKey = repository.generateAdminLicenseKey(setupCost = 500)
        val result = repository.activateKey(
            keyInput = admKey,
            customerId = "admin_user",
            customerName = "AdminTester",
            deviceId = "test_device"
        )
        assertTrue(result.isSuccess)
        val (type, _) = result.getOrThrow()
        assertEquals(LicenseType.ADMIN_PANEL, type)
    }

    @Test
    fun `payment submission and verification generates issued key`() = runBlocking {
        val tx = repository.submitPayment(
            customerId = "cust_123",
            customerName = "CarromVIP",
            planId = "plan_7d",
            planName = "7 Days Weekly VIP",
            amountInr = 500,
            paymentMethod = "UPI",
            utrNumber = "998877665544"
        )
        assertNotNull(tx.id)

        val verifyResult = repository.verifyPayment(tx.id, approve = true, adminActor = "MasterAdmin")
        assertTrue(verifyResult.isSuccess)

        val updatedTx = database.rolexDao().getPaymentById(tx.id)
        assertNotNull(updatedTx)
        assertEquals("VERIFIED", updatedTx!!.status)
        assertNotNull(updatedTx.issuedLicenseKey)
        assertTrue(updatedTx.issuedLicenseKey!!.startsWith("RFAX-PLAY-"))
    }

    @Test
    fun `carrom simulator calculates optimal aim angle`() {
        val engine = CarromSimulatorEngine()
        val aimResult = engine.calculateAutoAimPath()
        assertNotNull(aimResult)
        assertTrue(aimResult!!.pocketConfidencePct > 90)
    }
}
