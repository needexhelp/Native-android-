package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Data Models for Quick Commerce ---
data class ProductItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val originalPrice: Double? = null,
    val qtyUnit: String,
    val labelChar: String, // Emoji representation for illustration
    val tintColorHex: Long, // Color tint for item illustration card
    val isBestSeller: Boolean = false
) {
    val discountPercent: Int?
        get() = if (originalPrice != null && originalPrice > price) {
            (((originalPrice - price) / originalPrice) * 100).toInt()
        } else {
            null
        }
}

data class CategoryItem(
    val id: String,
    val name: String,
    val labelChar: String,
    val colorHex: Long
)

data class Coupon(
    val code: String,
    val discountDescription: String,
    val discountValue: Double,
    val minOrder: Double
)

enum class CommerceMode {
    BLINKIT, ZEPTO
}

enum class DeliveryStep {
    ORDER_CONFIRMED, STORE_PREPARATION, PARTNER_ASSIGNED, ON_THE_WAY, NEARBY, DELIVERED
}

enum class AppTab { HOME, ORDERS, SERVICES, NOTIFY, NEWS }

enum class ActiveScreen { 
  MAIN, FOOD, MEDICINE, TAXI, AMBULANCE, HOSPITAL, PRINT, BUS, PROFILE, DOXA_AI, GROCERY
}

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val unread: Boolean,
    val category: String
)

data class FoodItem(
    val id: String,
    val name: String,
    val price: Double,
    val originalPrice: Double,
    val rating: String,
    val cuisines: List<String>,
    val isVeg: Boolean,
    val deliveryTime: String,
    val emoji: String = "🍛"
) {
    val discountPercent: Int
        get() = (((originalPrice - price) / originalPrice) * 100).toInt()
}

class AppViewModel : ViewModel() {

    // --- Bottom Tab and Screen Navigation ---
    private val _activeTab = MutableStateFlow(AppTab.HOME)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.MAIN)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    private val navigationStack = mutableListOf<ActiveScreen>(ActiveScreen.MAIN)

    fun setActiveTab(tab: AppTab) {
        _activeTab.value = tab
    }

    fun navigateTo(screen: ActiveScreen) {
        if (screen == ActiveScreen.MAIN) {
            navigationStack.clear()
            navigationStack.add(ActiveScreen.MAIN)
        } else {
            if (navigationStack.isEmpty() || navigationStack.last() != screen) {
                navigationStack.add(screen)
            }
        }
        _activeScreen.value = screen
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            val previousScreen = navigationStack.lastOrNull() ?: ActiveScreen.MAIN
            _activeScreen.value = previousScreen
        } else {
            _activeScreen.value = ActiveScreen.MAIN
        }
    }

    // --- Food Cart Management ---
    private val _foodCartItems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val foodCartItems: StateFlow<Map<String, Int>> = _foodCartItems.asStateFlow()

    fun addFoodToCart(id: String) {
        val current = _foodCartItems.value.toMutableMap()
        current[id] = (current[id] ?: 0) + 1
        _foodCartItems.value = current
    }

    fun removeFoodFromCart(id: String) {
        val current = _foodCartItems.value.toMutableMap()
        val count = current[id] ?: 0
        if (count <= 1) {
            current.remove(id)
        } else {
            current[id] = count - 1
        }
        _foodCartItems.value = current
    }

    // --- Shared Product Sheet State ---
    private val _selectedProductForDetail = MutableStateFlow<ProductItem?>(null)
    val selectedProductForDetail: StateFlow<ProductItem?> = _selectedProductForDetail.asStateFlow()

    private val _selectedFoodForDetail = MutableStateFlow<FoodItem?>(null)
    val selectedFoodForDetail: StateFlow<FoodItem?> = _selectedFoodForDetail.asStateFlow()

    fun showProductDetail(product: ProductItem) {
        _selectedProductForDetail.value = product
        _selectedFoodForDetail.value = null
    }

    fun showFoodDetail(food: FoodItem) {
        _selectedFoodForDetail.value = food
        _selectedProductForDetail.value = null
    }

    fun dismissDetailsSheet() {
        _selectedProductForDetail.value = null
        _selectedFoodForDetail.value = null
    }

    // --- Notifications State ---
    private val _notifications = MutableStateFlow(listOf(
        NotificationItem("n1", "⚡ Order #CRW-9824 Delivered!", "Your order from Noida Sector 62 dispatch store was successfully delivered in 9 mins 42 secs.", "Today, 10:50 AM", true, "delivery"),
        NotificationItem("n2", "🏷️ Triple Save Activated! Code: INSTANT50", "Apply INSTANT50 code inside your shopping cart to slash ₹50 FLAT on standard baskets.", "Today, 8:00 AM", true, "promo"),
        NotificationItem("n3", "🥑 Farm-Fresh Organic Avocados are Back", "Picked at sunrise, transferred in climate-regulated vans. Fresh and ready at Noida 62 hub.", "Yesterday", true, "inventory"),
        NotificationItem("n4", "⚙️ High Demand / Wet Roads Notice", "Heavy rains around Electronic City Metro zone. Courier matching queues could stretch by 5 minutes.", "2 days ago", false, "system")
    ))
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(3)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun markAllRead() { 
        _notifications.value = _notifications.value.map { it.copy(unread = false) }
        _unreadCount.value = 0
    }

    // --- Commerce Mode (Blinkit vs Zepto) ---
    private val _currentMode = MutableStateFlow(CommerceMode.ZEPTO)
    val currentMode: StateFlow<CommerceMode> = _currentMode.asStateFlow()

    // --- Search Query and Search Mode ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchModeActive = MutableStateFlow(false)
    val isSearchModeActive: StateFlow<String> = MutableStateFlow("").asStateFlow() // Keep simple representation or standard boolean
    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    // --- Selected Category ---
    private val _selectedCategory = MutableStateFlow<String>("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // --- Cart Management (Item ID -> Quantity) ---
    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cartItems: StateFlow<Map<String, Int>> = _cartItems.asStateFlow()

    private val _showCart = MutableStateFlow(false)
    val showCart: StateFlow<Boolean> = _showCart.asStateFlow()

    fun setShowCart(show: Boolean) {
        _showCart.value = show
    }

    // --- Selected Delivery Tip ---
    private val _deliveryTip = MutableStateFlow<Double>(0.0)
    val deliveryTip: StateFlow<Double> = _deliveryTip.asStateFlow()

    // --- Selected Delivery Address ---
    private val _deliveryAddress = MutableStateFlow("Yavatmal, Yavatmal District, Maharashtra, 445001, India")
    val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

    // --- Selected Coupon Code ---
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    // --- Order & Map Tracking Simulation State ---
    private val _isOrderPlaced = MutableStateFlow(false)
    val isOrderPlaced: StateFlow<Boolean> = _isOrderPlaced.asStateFlow()

    private val _trackingProgress = MutableStateFlow(0.0f) // 0.0 to 1.0 representing delivery rider position
    val trackingProgress: StateFlow<Float> = _trackingProgress.asStateFlow()

    private val _deliveryStep = MutableStateFlow(DeliveryStep.ORDER_CONFIRMED)
    val deliveryStep: StateFlow<DeliveryStep> = _deliveryStep.asStateFlow()

    private val _deliveryEtaSeconds = MutableStateFlow(600) // Changed to 10 minutes (600s) default
    val deliveryEtaSeconds: StateFlow<Int> = _deliveryEtaSeconds.asStateFlow()

    private val _cancelTimeRemaining = MutableStateFlow(60) // 1 minute order cancellation timer
    val cancelTimeRemaining: StateFlow<Int> = _cancelTimeRemaining.asStateFlow()

    private var cancelJob: Job? = null

    fun manuallySetDeliveryStep(step: DeliveryStep) {
        _deliveryStep.value = step
        // Bind map tracking progress beautifully to the manual steps!
        _trackingProgress.value = when (step) {
            DeliveryStep.ORDER_CONFIRMED -> 0.0f
            DeliveryStep.STORE_PREPARATION -> 0.2f
            DeliveryStep.PARTNER_ASSIGNED -> 0.4f
            DeliveryStep.ON_THE_WAY -> 0.65f
            DeliveryStep.NEARBY -> 0.85f
            DeliveryStep.DELIVERED -> 1.0f
        }
        if (step == DeliveryStep.DELIVERED) {
            _deliveryEtaSeconds.value = 0
        } else {
            // Adjust eta remaining time based on progress
            _deliveryEtaSeconds.value = when (step) {
                DeliveryStep.ORDER_CONFIRMED -> 600
                DeliveryStep.STORE_PREPARATION -> 480
                DeliveryStep.PARTNER_ASSIGNED -> 360
                DeliveryStep.ON_THE_WAY -> 240
                DeliveryStep.NEARBY -> 90
                DeliveryStep.DELIVERED -> 0
            }
        }
    }

    // --- Permission simulation states ---
    private val _isGpsDetected = MutableStateFlow(false)
    val isGpsDetected: StateFlow<Boolean> = _isGpsDetected.asStateFlow()

    private val _isCameraMicApproved = MutableStateFlow(false)
    val isCameraMicApproved: StateFlow<Boolean> = _isCameraMicApproved.asStateFlow()

    private val _isBackgroundEngineOn = MutableStateFlow(false)
    val isBackgroundEngineOn: StateFlow<Boolean> = _isBackgroundEngineOn.asStateFlow()

    fun setGpsDetected(detected: Boolean) {
        _isGpsDetected.value = detected
        if (detected) {
            _deliveryAddress.value = "Gate 3, Marine Drive Promenade, Mumbai (Auto-detected GPS 📍)"
        } else {
            _deliveryAddress.value = "Home (B-104, Mahim West, Mumbai Central)"
        }
    }

    fun setCameraMicApproved(approved: Boolean) {
        _isCameraMicApproved.value = approved
    }

    fun setBackgroundEngineOn(on: Boolean) {
        _isBackgroundEngineOn.value = on
    }

    private var trackingJob: Job? = null

    // --- Static Product Data ---
    val categories = listOf(
        CategoryItem("all", "All Items", "✨", 0xFFFFEBEB),
        CategoryItem("veg", "Grocery", "🛒", 0xFFE3F9E5),
        CategoryItem("snacks", "Food Order", "🍔", 0xFFFFF3D1),
        CategoryItem("dairy", "Taxi/Auto", "🚕", 0xFFE3F2FD),
        CategoryItem("instant", "Medicine", "💊", 0xFFFCE4EC),
        CategoryItem("personal", "Hospital", "🏥", 0xFFEDE7F6),
        CategoryItem("medicine", "Ambulance", "🚑", 0xFFE0F2FE)
    )

    val products = mutableListOf<ProductItem>(
        // Fruits & Veggies
        ProductItem("v1", "Desi Onion", "veg", 34.0, 48.0, "1 kg", "🧅", 0xFFFFF1EB, true),
        ProductItem("v2", "Fresh Potato", "veg", 28.0, 35.0, "1 kg", "🥔", 0xFFFCF4E0),
        ProductItem("v3", "Tomato Local", "veg", 21.0, 39.0, "500 g", "🍅", 0xFFFFF2F2, true),
        ProductItem("v4", "Hybrid Banana", "veg", 39.0, 50.0, "6 units", "🍌", 0xFFFFFDE7),
        ProductItem("v5", "Organic Spinach", "veg", 18.0, 24.0, "250 g", "🥬", 0xFFEAF9EB),
        ProductItem("v6", "Button Mushrooms", "veg", 49.0, 60.0, "1 pack", "🍄", 0xFFF5F5F5),

        // Snacks & Drinks
        ProductItem("s1", "Classic Salted Lay's", "snacks", 20.0, 20.0, "50 g", "🥔", 0xFFFFFDE7),
        ProductItem("s2", "Cadbury Dairy Milk Silk", "snacks", 80.0, 90.0, "60 g", "🍫", 0xFFF3E5F5, true),
        ProductItem("s3", "Coca Cola Soft Drink", "snacks", 40.0, 45.0, "330 ml", "🥤", 0xFFFFEBEE),
        ProductItem("s4", "Oreo Chocolate Cookies", "snacks", 30.0, 35.0, "120 g", "🍪", 0xFFECEFF1),
        ProductItem("s5", "Kurkure Masala Munch", "snacks", 20.0, 20.0, "80 g", "🌶️", 0xFFFFF3E0),

        // Dairy & Bread
        ProductItem("d1", "Amul Taaza Milk", "dairy", 66.0, 68.0, "1 L", "🥛", 0xFFEBF5FB, true),
        ProductItem("d2", "Amul Butter Salted", "dairy", 56.0, 58.0, "100 g", "🧈", 0xFFFFFDE7),
        ProductItem("d3", "Britannia Brown Bread", "dairy", 45.0, 50.0, "400 g", "🍞", 0xFFF5EBE6),
        ProductItem("d4", "Fresh Paneer Block", "dairy", 85.0, 95.0, "200 g", "🧀", 0xFFFFFDF0),

        // Instant Foods
        ProductItem("i1", "Samyang Hot Buldak Noodles", "instant", 125.0, 135.0, "140 g", "🌶️", 0xFFFFEBE3),
        ProductItem("i2", "Maggi 2-Minute Noodles", "instant", 14.0, 14.0, "70 g", "🍜", 0xFFFFFDE7, true),
        ProductItem("i3", "Chings Hakka Noodles", "instant", 40.0, 48.0, "150 g", "🍝", 0xFFFFF9E6),

        // Medicine & Wellness (Real items fitting Medicines Near You format perfectly)
        ProductItem("m1", "Crocin Pain Relief", "medicine", 30.0, 35.0, "15 tablets", "💊", 0xFFFFF2F2, true),
        ProductItem("m2", "Vicks Vaporub Gel", "medicine", 95.0, 105.0, "50 g", "🧴", 0xFFE3F2FD),
        ProductItem("m3", "Volini Pain Spray", "medicine", 135.0, 150.0, "75 g", "🧪", 0xFFFDF2E9),
        ProductItem("m4", "Revital Daily H Caps", "medicine", 280.0, 310.0, "30 caps", "💊", 0xFFECFDF5),

        // Personal Care
        ProductItem("p1", "Dettol Liquid Handwash", "personal", 99.0, 110.0, "200 ml", "🧴", 0xFFEAF9EC),
        ProductItem("p2", "Colgate MaxFresh toothpaste", "personal", 80.0, 95.0, "150 g", "🦷", 0xFFE3F2FD),
        ProductItem("p3", "Dove Cream Beauty Bar", "personal", 62.0, 70.0, "100 g", "🧼", 0xFFF5F5F5)
    )

    val validCoupons = listOf(
        Coupon("INSTANT5", "₹5 flat off on orders above ₹30", 5.0, 30.0),
        Coupon("INSTANT50", "₹50 flat off on orders above ₹199", 50.0, 199.0),
        Coupon("FREESHIP", "₹25 discount on orders above ₹150", 25.0, 150.0)
    )

    val foodProducts = listOf(
        FoodItem("f1", "Paneer Butter Masala + 2 Roti", 249.0, 320.0, "4.7", listOf("North Indian", "Chinese"), true, "12 mins", "🍛"),
        FoodItem("f2", "Margherita Pizza (7 inch)", 199.0, 280.0, "4.5", listOf("Italian", "Fast Food"), true, "18 mins", "🍕"),
        FoodItem("f3", "Dal Makhani + Jeera Rice", 189.0, 240.0, "4.8", listOf("North Indian", "Punjabi"), true, "14 mins", "🍲"),
        FoodItem("f4", "Crispy Veg Burger Combo", 149.0, 199.0, "4.4", listOf("Fast Food", "Burgers"), true, "10 mins", "🍔"),
        FoodItem("f5", "Veg Hakka Noodles & Manchurian", 169.0, 220.0, "4.6", listOf("Chinese", "Fast Food"), true, "16 mins", "🍜"),
        FoodItem("f6", "Chocolate Brownie Sundae", 129.0, 170.0, "4.9", listOf("Desserts", "Ice Cream"), true, "8 mins", "🍨")
    )

    fun toggleMode() {
        // Mode is locked to ZEPTO
    }

    fun setQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _searchActive.value = active
    }

    fun selectCategory(catId: String) {
        _selectedCategory.value = catId
    }

    // --- Register products from screens that keep their own local catalog
    // (e.g. GroceryScreen.kt's category/subcategory listings) so that
    // getCartSubtotal() / getCartCount() / CartDrawerSheet can find them
    // by id, exactly like the built-in home dashboard products.
    fun registerExternalProduct(
        id: String,
        name: String,
        category: String,
        price: Double,
        originalPrice: Double?,
        qtyUnit: String,
        emoji: String,
        tintColorHex: Long
    ) {
        if (products.none { it.id == id }) {
            products.add(
                ProductItem(
                    id = id,
                    name = name,
                    category = category,
                    price = price,
                    originalPrice = originalPrice,
                    qtyUnit = qtyUnit,
                    labelChar = emoji,
                    tintColorHex = tintColorHex
                )
            )
        }
    }

    // --- Cart Actions ---
    fun addToCart(productId: String) {
        val current = _cartItems.value.toMutableMap()
        current[productId] = (current[productId] ?: 0) + 1
        _cartItems.value = current
    }

    fun removeFromCart(productId: String) {
        val current = _cartItems.value.toMutableMap()
        val count = current[productId] ?: 0
        if (count <= 1) {
            current.remove(productId)
        } else {
            current[productId] = count - 1
        }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _appliedCoupon.value = null
        _deliveryTip.value = 0.0
    }

    fun applyCoupon(code: String): String {
        val match = validCoupons.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }
        return if (match != null) {
            val total = getCartSubtotal()
            if (total >= match.minOrder) {
                _appliedCoupon.value = match
                "SUCCESS: Coupon '${match.code}' applied! ₹${match.discountValue} saved."
            } else {
                "ERROR: Subtotal is ₹${total}. You need ₹${match.minOrder - total} more to apply this coupon."
            }
        } else {
            "ERROR: Invalid coupon code. Try 'INSTANT5', 'INSTANT50' or 'FREESHIP'!"
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun setDeliveryTip(tip: Double) {
        _deliveryTip.value = tip
    }

    fun setDeliveryAddress(address: String) {
        if (address.isNotBlank()) {
            _deliveryAddress.value = address
        }
    }

    // --- Calculation Getters ---
    fun getCartSubtotal(): Double {
        var subtotal = 0.0
        _cartItems.value.forEach { (prodId, qty) ->
            val product = products.firstOrNull { it.id == prodId }
            if (product != null) {
                subtotal += product.price * qty
            }
        }
        return subtotal
    }

    fun getGrandTotal(): Double {
        val sub = getCartSubtotal()
        if (sub == 0.0) return 0.0
        val couponDiscount = _appliedCoupon.value?.discountValue ?: 0.0
        val deliveryFee = if (sub > 150.0) 0.0 else 25.0
        val handlingFee = 4.0
        val tip = _deliveryTip.value
        return (sub - couponDiscount + deliveryFee + handlingFee + tip).coerceAtLeast(0.0)
    }

    fun getCartCount(): Int {
        return _cartItems.value.values.sum()
    }

    // --- Place Order & Start Map Simulation ---
    fun placeOrder() {
        if (_cartItems.value.isEmpty()) return
        _isOrderPlaced.value = true
        _trackingProgress.value = 0.0f
        _deliveryStep.value = DeliveryStep.ORDER_CONFIRMED
        _deliveryEtaSeconds.value = 600 // 10 minutes default
        _cancelTimeRemaining.value = 60 // 1 minute ticker

        cancelJob?.cancel()
        cancelJob = viewModelScope.launch {
            while (_cancelTimeRemaining.value > 0) {
                delay(1000)
                _cancelTimeRemaining.value -= 1
            }
        }

        // Start simulated realtime ticker for delivery partner on the map
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (_isOrderPlaced.value && _deliveryEtaSeconds.value > 0) {
                delay(1000)
                if (_deliveryEtaSeconds.value > 0 && _deliveryStep.value != DeliveryStep.DELIVERED) {
                    _deliveryEtaSeconds.value -= 1
                }
            }
        }
    }

    fun resetOrder() {
        trackingJob?.cancel()
        cancelJob?.cancel()
        _isOrderPlaced.value = false
        _trackingProgress.value = 0.0f
        _deliveryStep.value = DeliveryStep.ORDER_CONFIRMED
        _deliveryEtaSeconds.value = 600
        _cancelTimeRemaining.value = 60
        clearCart()
    }
}
