package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCartOperationsAndTotals() {
        val viewModel = AppViewModel()

        // 1. Initially, cart should be empty
        assertEquals(0, viewModel.getCartCount())
        assertEquals(0.0, viewModel.getCartSubtotal(), 0.01)

        // 2. Add some items
        viewModel.addToCart("v1") // Desi Onion, ₹34
        viewModel.addToCart("v1") // Desi Onion, ₹34
        viewModel.addToCart("v3") // Tomato Local, ₹21

        assertEquals(3, viewModel.getCartCount())
        // Subtotal: (34 * 2) + 21 = ₹89
        assertEquals(89.0, viewModel.getCartSubtotal(), 0.01)

        // 3. Remove an item
        viewModel.removeFromCart("v1")
        assertEquals(2, viewModel.getCartCount())
        assertEquals(55.0, viewModel.getCartSubtotal(), 0.01) // 34 + 21 = ₹55

        // 4. Test Grand Total Calculation
        // Grand total = subtotal + handlingFee (₹4) + deliveryFee (₹25 if subtotal <= ₹150)
        // 55.0 + 4.0 + 25.0 = 84.0
        assertEquals(84.0, viewModel.getGrandTotal(), 0.01)

        // 5. Test Tip Addition
        viewModel.setDeliveryTip(25.0)
        assertEquals(25.0, viewModel.deliveryTip.value, 0.01)
        // 84.0 + 25.0 = 109.0
        assertEquals(109.0, viewModel.getGrandTotal(), 0.01)
    }

    @Test
    fun testCouponApplicationRules() {
        val viewModel = AppViewModel()

        // Minimum order for ZEPTOMANIA is ₹199
        viewModel.addToCart("v1") // ₹34
        val resultFail = viewModel.applyCoupon("ZEPTOMANIA")
        assertTrue(resultFail.startsWith("ERROR"))
        assertNull(viewModel.appliedCoupon.value)

        // Add more items to exceed ₹199 (add 5 onions = 34 * 5 = ₹170) -> Total = 204
        viewModel.addToCart("v1")
        viewModel.addToCart("v1")
        viewModel.addToCart("v1")
        viewModel.addToCart("v1")
        viewModel.addToCart("v1")

        val resultSuccess = viewModel.applyCoupon("ZEPTOMANIA")
        assertTrue(resultSuccess.startsWith("SUCCESS"))
        assertNotNull(viewModel.appliedCoupon.value)
        assertEquals("ZEPTOMANIA", viewModel.appliedCoupon.value?.code)
        assertEquals(50.0, viewModel.appliedCoupon.value?.discountValue ?: 0.0, 0.01)

        // Remove coupon
        viewModel.removeCoupon()
        assertNull(viewModel.appliedCoupon.value)
    }
}
