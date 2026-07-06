package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.AppViewModel
import com.example.ui.theme.crowmixShadow

// --- Static / Mock Data Models ---

data class CategoryItem(
    val name: String,
    val displayName: String,
    val emoji: String
)

data class ProductData(
    val id: String,
    val name: String,
    val category: String,
    val subcategory: String,
    val price: Int,
    val originalPrice: Int,
    val weight: String,
    val savings: String,
    val rating: Double,
    val reviewCount: Int,
    val deliveryTime: String,
    val emoji: String,
    val tintColorHex: Long
)

// --- Canonicalization helper ---

fun getCanonicalCategoryName(name: String): String {
    return when (name.trim().uppercase()) {
        "VEGETABLES & FRUITS" -> "Vegetables & Fruits"
        "DAIRY, BREAD & EGGS", "MILK, CURD & PANEER", "MILK CURD & PANEER" -> "Milk, Curd & Paneer"
        "OIL, GHEE & MASALA", "GHEE, BUTTER & OIL", "GHEE BUTTER & OIL" -> "Ghee, Butter & Oil"
        "ATTA, RICE & DAL", "RICE, ATTA & MORE", "RICE ATTA & MORE" -> "Rice, Atta & More"
        "BAKERY & BISCUITS" -> "Bakery & Biscuits"
        "DRY FRUITS & CEREALS" -> "Dry Fruits & Cereals"
        "CHICKEN, MEAT & FISH", "CHICKEN & MEAT" -> "Chicken & Meat"
        "CHIPS & NAMKEEN" -> "Chips & Namkeen"
        "TEA, COFFEE & MORE", "TEA COFFEE & MORE" -> "Tea, Coffee & More"
        "SWEETS & CHOCOLATES", "CHOCOLATES & SWEETS" -> "Chocolates & Sweets"
        "DRINKS & JUICES", "COOL DRINKS & JUICES" -> "Cool Drinks & Juices"
        "INSTANT FOOD", "NOODLES & PASTA" -> "Noodles & Pasta"
        "SAUCES & SPREADS" -> "Sauces & Spreads"
        "ICE CREAMS & MORE", "FROZEN FOOD" -> "Frozen Food"
        "CLEANERS & REPELLENTS", "CLEANING ESSENTIALS" -> "Cleaning Essentials"
        else -> name
    }
}

fun getSubcategoriesForCategory(category: String): List<String> {
    val canonical = getCanonicalCategoryName(category)
    return when (canonical) {
        "Vegetables & Fruits" -> listOf("Leafy Greens", "Root Vegetables", "Cruciferous Vegetables", "Allium Vegetables", "Mushrooms", "Stem Vegetables", "Nightshade Vegetables", "Sea Vegetables", "Other Vegetables", "Citrus Fruits", "Tropical Fruits", "Berries", "Stone Fruits", "Pome Fruits", "Melons", "Dried Fruits", "Exotic Fruits")
        "Milk, Curd & Paneer" -> listOf("Milk Curd & Buttermilk", "Paneer & Cheese", "Butter & Cream", "Breads & Buns", "Brown & Multigrain Bread", "Eggs", "Flavoured Milk", "Milk Powders")
        "Ghee, Butter & Oil" -> listOf("Edible Oils", "Cold Pressed Oils", "Ghee & Vanaspati", "Whole Spices", "Powdered Spices", "Blended Masala", "Cooking Pastes", "Salt/Sugar/Jaggery", "Dates & Seeds", "Papad & Fryums")
        "Rice, Atta & More" -> listOf("Wheat Atta", "Multigrain & Specialty Atta", "Rice", "Premium & Basmati Rice", "Dal & Pulses", "Sprouts & Beans", "Poha & Sooji", "Ready Flour Mixes")
        "Bakery & Biscuits" -> listOf("Biscuits", "Cookies", "Cream Biscuits", "Cakes & Rusks", "Breads & Pav", "Bakery Snacks")
        "Dry Fruits & Cereals" -> listOf("Dry Fruits", "Roasted Dry Fruits", "Seeds & Mixes", "Trail Mixes", "Breakfast Cereals", "Muesli & Oats")
        "Chicken & Meat" -> listOf("Fresh Chicken", "Chicken Cuts", "Mutton", "Fish & Seafood", "Ready-to-Cook Non-Veg", "Frozen Non-Veg")
        "Chips & Namkeen" -> listOf("Chips", "Namkeen", "Bhujia & Mixtures", "Healthy Snacks")
        "Tea, Coffee & More" -> listOf("Tea", "Coffee", "Green & Herbal Tea", "Health Drinks", "Milk Additives")
        "Chocolates & Sweets" -> listOf("Chocolates", "Chocolate Bars", "Indian Sweets", "Mithai Boxes")
        "Cool Drinks & Juices" -> listOf("Fruit Juices", "Soft Drinks", "Energy Drinks", "Soda & Mixers")
        "Noodles & Pasta" -> listOf("Noodles", "Pasta & Macaroni", "Ready Meals", "Soups", "Breakfast Mixes")
        "Sauces & Spreads" -> listOf("Ketchup & Sauces", "Mayonnaise & Dips", "Sandwich Spreads", "Cooking Sauces")
        "Frozen Food" -> listOf("Ice Cream", "Ice Cream Cups", "Frozen Desserts", "Frozen Snacks")
        "Cleaning Essentials" -> listOf("Floor Cleaners", "Toilet Cleaners", "Dishwashing Products", "Laundry Care", "Mosquito Control", "Insect Repellents")
        
        // Secondary groups
        "Baby Care" -> listOf("Baby Food", "Diapers & Wipes", "Baby Bath", "Baby Skin Care")
        "Bath & Body" -> listOf("Soaps", "Body Wash", "Hand Wash", "Talcum Powder")
        "Hair" -> listOf("Shampoo", "Hair Oil", "Conditioner", "Hair Styling")
        "Skin & Face" -> listOf("Face Wash", "Moisturizers", "Sunscreen", "Lip Care")
        "Beauty & Cosmetics" -> listOf("Makeup", "Nail Polish", "Perfumes")
        "Feminine Hygiene" -> listOf("Pads", "Liners", "Intimate Wash")
        "Health & Pharma" -> listOf("Vitamins", "Pain Relief", "First Aid", "Masks")
        "Electronics" -> listOf("Batteries", "Bulbs", "Cables", "Chargers")
        "Home & Lifestyle" -> listOf("Kitchenware", "Containers", "Bed Sheets", "Towels")
        "Stationery & Games" -> listOf("Notebooks", "Pens & Pencils", "Card Games")
        "Spiritual Store" -> listOf("Agarbatti", "Diya Oil", "Puja Samagri")
        "Pet Store" -> listOf("Dog Food", "Cat Food", "Pet Toys")
        "Jewellery Store" -> listOf("Rings", "Necklaces", "Earrings")
        "Edible Store" -> listOf("Grains", "Spices", "Snacks")
        "Sports Store" -> listOf("Badminton", "Cricket", "Fitness")
        "Fashion Store" -> listOf("T-Shirts", "Socks", "Caps")
        "Toy Store" -> listOf("Action Figures", "Board Games", "Dolls")
        "Book Store" -> listOf("Fiction", "Educational", "Notebooks")
        "Kitchenware & Appliances" -> listOf("Utensils", "Blenders", "Kettles")
        "Instant Food" -> listOf("Noodles", "Ready Meals", "Soups")
        "Ice Creams & More" -> listOf("Tubs", "Cones", "Kulfi")
        else -> listOf("All Products")
    }
}

fun getProductsForSubcategory(category: String, subcategory: String): List<ProductData> {
    val canonicalCat = getCanonicalCategoryName(category)
    
    // Explicit specific products for "Vegetables & Fruits" -> "Leafy Greens"
    if (canonicalCat == "Vegetables & Fruits" && subcategory == "Leafy Greens") {
        return listOf(
            ProductData("v_coriander", "coriander leaves", category, subcategory, 20, 40, "250g", "₹20 OFF", 4.5, 312, "11 mins", "🌿", 0xFFF1F8E9),
            ProductData("v_spinach", "spinach", category, subcategory, 20, 40, "250g", "₹20 OFF", 4.4, 267, "11 mins", "🥬", 0xFFE8F5E9),
            ProductData("v_mint", "mint leaves", category, subcategory, 15, 30, "100g", "₹15 OFF", 4.6, 145, "10 mins", "🌱", 0xFFE8F8F5)
        )
    }
    
    // Explicit specific products for "Vegetables & Fruits" -> "Root Vegetables"
    if (canonicalCat == "Vegetables & Fruits" && subcategory == "Root Vegetables") {
        return listOf(
            ProductData("v_potato", "potato", category, subcategory, 30, 50, "1kg", "₹20 OFF", 4.7, 890, "12 mins", "🥔", 0xFFFFF3E0),
            ProductData("v_onion", "onion", category, subcategory, 40, 60, "1kg", "₹20 OFF", 4.8, 1205, "12 mins", "🧅", 0xFFF3E5F5),
            ProductData("v_carrot", "carrot", category, subcategory, 35, 50, "500g", "₹15 OFF", 4.5, 420, "11 mins", "🥕", 0xFFFFEBEE)
        )
    }

    // Dynamic generator to populate authentic looking items for any other selected subcategory
    val baseName = subcategory.trim().removeSuffix("s").removeSuffix("es").lowercase()
    val emoji = when {
        subcategory.contains("Milk", true) || subcategory.contains("Curd", true) -> "🥛"
        subcategory.contains("Butter", true) -> "🧈"
        subcategory.contains("Ghee", true) || subcategory.contains("Oil", true) -> "🛢️"
        subcategory.contains("Rice", true) || subcategory.contains("Atta", true) -> "🌾"
        subcategory.contains("Bread", true) || subcategory.contains("Bun", true) -> "🍞"
        subcategory.contains("Cake", true) -> "🍰"
        subcategory.contains("Biscuit", true) || subcategory.contains("Cookie", true) -> "🍪"
        subcategory.contains("Chicken", true) -> "🍗"
        subcategory.contains("Meat", true) || subcategory.contains("Mutton", true) -> "🥩"
        subcategory.contains("Fish", true) || subcategory.contains("Seafood", true) -> "🐟"
        subcategory.contains("Chip", true) || subcategory.contains("Namkeen", true) -> "🍿"
        subcategory.contains("Tea", true) || subcategory.contains("Coffee", true) -> "☕"
        subcategory.contains("Chocolate", true) -> "🍫"
        subcategory.contains("Sweet", true) -> "🍬"
        subcategory.contains("Juice", true) || subcategory.contains("Drink", true) -> "🍹"
        subcategory.contains("Noodle", true) || subcategory.contains("Pasta", true) -> "🍜"
        subcategory.contains("Sauce", true) || subcategory.contains("Spread", true) -> "🥫"
        subcategory.contains("Ice Cream", true) || subcategory.contains("Frozen", true) -> "🍦"
        subcategory.contains("Cleaner", true) || subcategory.contains("Laundry", true) -> "🧹"
        subcategory.contains("Fruit", true) -> "🍎"
        subcategory.contains("Vegetable", true) -> "🥦"
        subcategory.contains("Egg", true) -> "🥚"
        else -> "📦"
    }

    val colors = listOf(0xFFE8F5E9, 0xFFFFF3E0, 0xFFE3F2FD, 0xFFF3E5F5, 0xFFFFEBEE, 0xFFFFFDE7)
    val tint = colors[subcategory.hashCode().coerceAtLeast(0) % colors.size]

    return listOf(
        ProductData(
            id = "${category.hashCode()}_${subcategory.hashCode()}_1",
            name = "fresh $baseName pack",
            category = category,
            subcategory = subcategory,
            price = 45,
            originalPrice = 65,
            weight = "500g",
            savings = "₹20 OFF",
            rating = 4.6,
            reviewCount = 124,
            deliveryTime = "11 mins",
            emoji = emoji,
            tintColorHex = tint
        ),
        ProductData(
            id = "${category.hashCode()}_${subcategory.hashCode()}_2",
            name = "premium $baseName box",
            category = category,
            subcategory = subcategory,
            price = 90,
            originalPrice = 120,
            weight = "1kg",
            savings = "₹30 OFF",
            rating = 4.8,
            reviewCount = 312,
            deliveryTime = "12 mins",
            emoji = emoji,
            tintColorHex = tint
        )
    )
}

// --- Navigation Wrapper ---

@Composable
fun GroceryFlowContainer(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "category_grid",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("category_grid") {
            CategoryGridScreen(
                onBack = onBack,
                onCategoryClick = { selectedCategory ->
                    navController.navigate("subcategory_listing/${selectedCategory}")
                }
            )
        }
        composable(
            route = "subcategory_listing/{categoryName}",
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            SubcategoryListingScreen(
                categoryName = categoryName,
                viewModel = viewModel,
                onBack = { navController.navigateUp() }
            )
        }
    }
}

// --- SCREEN 1: Category Grid (Grocery Home) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGridScreen(
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    // Layout sections with respective visual items
    val sections = listOf(
        "GROCERY & KITCHEN" to listOf(
            CategoryItem("Vegetables & Fruits", "Vegetables & Fruits", "🥦"),
            CategoryItem("Milk Curd & Paneer", "Dairy, Bread & Eggs", "🥛"),
            CategoryItem("Ghee Butter & Oil", "Oil, Ghee & Masala", "🧈"),
            CategoryItem("Rice Atta & More", "Atta, Rice & Dal", "🌾"),
            CategoryItem("Bakery & Biscuits", "Bakery & Biscuits", "🍞"),
            CategoryItem("Dry Fruits & Cereals", "Dry Fruits & Cereals", "🥜"),
            CategoryItem("Chicken & Meat", "Chicken, Meat & Fish", "🍗"),
            CategoryItem("Kitchenware & Appliances", "Kitchenware & Appliances", "🍳")
        ),
        "SNACKS & DRINKS" to listOf(
            CategoryItem("Chips & Namkeen", "Chips & Namkeen", "🍿"),
            CategoryItem("Tea Coffee & More", "Tea, Coffee & More", "☕"),
            CategoryItem("Chocolates & Sweets", "Sweets & Chocolates", "🍫"),
            CategoryItem("Cool Drinks & Juices", "Drinks & Juices", "🍹"),
            CategoryItem("Instant Food", "Instant Food", "🍜"),
            CategoryItem("Sauces & Spreads", "Sauces & Spreads", "🥫"),
            CategoryItem("Frozen Food", "Ice Creams & More", "🍦")
        ),
        "BEAUTY & SELF CARE" to listOf(
            CategoryItem("Baby Care", "Baby Care", "👶"),
            CategoryItem("Bath & Body", "Bath & Body", "🧼"),
            CategoryItem("Hair", "Hair", "💈"),
            CategoryItem("Skin & Face", "Skin & Face", "🧴"),
            CategoryItem("Beauty & Cosmetics", "Beauty & Cosmetics", "💄"),
            CategoryItem("Feminine Hygiene", "Feminine Hygiene", "🌸"),
            CategoryItem("Health & Pharma", "Health & Pharma", "💊")
        ),
        "HOUSEHOLD ESSENTIALS" to listOf(
            CategoryItem("Electronics", "Electronics", "🔌"),
            CategoryItem("Home & Lifestyle", "Home & Lifestyle", "🏠"),
            CategoryItem("Cleaning Essentials", "Cleaners & Repellents", "🧹"),
            CategoryItem("Stationery & Games", "Stationery & Games", "🎮")
        ),
        "FOR LOCAL SHOPS" to listOf(
            CategoryItem("Spiritual Store", "Spiritual Store", "🪔"),
            CategoryItem("Pet Store", "Pet Store", "🐶"),
            CategoryItem("Jewellery Store", "Jewellery Store", "💎"),
            CategoryItem("Edible Store", "Edible Store", "🏪"),
            CategoryItem("Sports Store", "Sports Store", "⚽"),
            CategoryItem("Fashion Store", "Fashion Store", "👕"),
            CategoryItem("Toy Store", "Toy Store", "🧸"),
            CategoryItem("Book Store", "Book Store", "📚")
        )
    )

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSections = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            sections
        } else {
            sections.map { (sectionTitle, items) ->
                sectionTitle to items.filter { item ->
                    item.displayName.contains(searchQuery, ignoreCase = true) ||
                    item.name.contains(searchQuery, ignoreCase = true)
                }
            }.filter { it.second.isNotEmpty() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A3BBF), Color(0xFF1A56DB))
                    )
                ),
                title = {
                    if (isSearchActive) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .testTag("grocery_search_input"),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search categories...",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 18.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Grocery",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("grocery_grid_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFFF8FAFC) // Light off-white background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (filteredSections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No categories found for \"$searchQuery\"",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                filteredSections.forEach { (sectionTitle, items) ->
                    item {
                        CategorySection(
                            title = sectionTitle,
                            items = items,
                            onCategoryClick = onCategoryClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection(
    title: String,
    items: List<CategoryItem>,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        // Group items into chunks of 4 for a perfect 4-column grid layout
        val chunks = items.chunked(4)
        chunks.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 4) {
                    if (i < rowItems.size) {
                        val item = rowItems[i]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onCategoryClick(item.name) }
                                .padding(4.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .crowmixShadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9)), // Soft mint green background
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.emoji, fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.displayName.uppercase(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = Color.Black
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: Subcategory Product Listing ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryListingScreen(
    categoryName: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val subcategories = remember(categoryName) { getSubcategoriesForCategory(categoryName) }
    var selectedSubcategory by remember { mutableStateOf(subcategories.firstOrNull() ?: "") }

    // Filters & Sort states
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var showSortBottomSheet by remember { mutableStateOf(false) }

    var selectedPriceRanges by remember { mutableStateOf(setOf<String>()) }
    var selectedProperties by remember { mutableStateOf(setOf<String>()) }
    var selectedSort by remember { mutableStateOf("Relevance (default)") }

    // Temporary states for filters
    var tempSelectedPriceRanges by remember { mutableStateOf(setOf<String>()) }
    var tempSelectedProperties by remember { mutableStateOf(setOf<String>()) }
    var filterSearchQuery by remember { mutableStateOf("") }

    // Get list of base products depending on property filter
    val baseProducts = remember(categoryName, selectedSubcategory, selectedProperties) {
        if (selectedProperties.isEmpty()) {
            getProductsForSubcategory(categoryName, selectedSubcategory)
        } else {
            selectedProperties.flatMap { subcat ->
                getProductsForSubcategory(categoryName, subcat)
            }.distinctBy { it.id }
        }
    }

    // Filter products by selected price ranges
    val priceFilteredProducts = remember(baseProducts, selectedPriceRanges) {
        if (selectedPriceRanges.isEmpty()) {
            baseProducts
        } else {
            baseProducts.filter { product ->
                selectedPriceRanges.any { range ->
                    when (range) {
                        "Under ₹50" -> product.price < 50
                        "₹50 - ₹100" -> product.price in 50..100
                        "₹100 - ₹200" -> product.price in 100..200
                        "₹200 - ₹500" -> product.price in 200..500
                        "Above ₹500" -> product.price > 500
                        else -> true
                    }
                }
            }
        }
    }

    // Sort products by selected sort option
    val products = remember(priceFilteredProducts, selectedSort) {
        when (selectedSort) {
            "Price (low to high)" -> priceFilteredProducts.sortedBy { it.price }
            "Price (high to low)" -> priceFilteredProducts.sortedByDescending { it.price }
            "Discount (high to low)" -> priceFilteredProducts.sortedByDescending { it.originalPrice - it.price }
            else -> priceFilteredProducts // Relevance (default)
        }
    }

    // Favorite tracker local state
    var favorites by remember { mutableStateOf(setOf<String>()) }
    val context = LocalContext.current

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val searchedProducts = remember(products, searchQuery) {
        if (searchQuery.isEmpty()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.subcategory.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1A3BBF), Color(0xFF1A56DB))
                        )
                    ),
                    title = {
                        if (isSearchActive) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp)
                                    .testTag("grocery_subcategory_search_input"),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search products...",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 18.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = categoryName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("subcategory_back")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color.White
                                )
                            }
                        } else {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color(0xFFF8FAFC)
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // LEFT SIDE: Subcategory vertical scrollable rail
                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(subcategories) { subcat ->
                            val isSelected = subcat == selectedSubcategory
                            val firstLetter = subcat.take(1).uppercase()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSubcategory = subcat }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(
                                                    color = Color(0xFFE0F2FE),
                                                    shape = RoundedCornerShape(20.dp)
                                                ).border(
                                                    width = 2.dp,
                                                    color = Color(0xFF1800AD),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                            } else {
                                                Modifier.background(
                                                    color = Color(0xFFF1F5F9),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = firstLetter,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color(0xFF1800AD) else Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = subcat.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF1800AD) else Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                // RIGHT SIDE: Main product listing content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                ) {
                    // Dropdown and active filter row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Filters Button
                        OutlinedButton(
                            onClick = {
                                tempSelectedPriceRanges = selectedPriceRanges
                                tempSelectedProperties = selectedProperties
                                filterSearchQuery = ""
                                showFilterBottomSheet = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            border = BorderStroke(1.dp, Color.LightGray),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val filterCount = selectedPriceRanges.size + selectedProperties.size
                            Text(
                                text = if (filterCount > 0) "Filters ($filterCount)" else "Filters",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(1.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Sort Button
                        OutlinedButton(
                            onClick = { showSortBottomSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            border = BorderStroke(1.dp, Color.LightGray),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (selectedSort == "Relevance (default)") "Sort" else when (selectedSort) {
                                    "Price (low to high)" -> "₹ ↑"
                                    "Price (high to low)" -> "₹ ↓"
                                    "Discount (high to low)" -> "% ↓"
                                    else -> "Sort"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(1.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Subcategory horizontal chips (scrollable list on the right side)
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(subcategories) { subcat ->
                                val isSelected = subcat == selectedSubcategory
                                val count = remember(subcat) { getProductsForSubcategory(categoryName, subcat).size }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) Color(0xFF1A56DB) else Color(0xFFF1F5F9)
                                        )
                                        .clickable { selectedSubcategory = subcat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = subcat.lowercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF64748B)
                                        )
                                        if (count > 0) {
                                            Text(
                                                text = "($count)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2-Column Product Grid
                    if (searchedProducts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (searchQuery.isNotEmpty()) {
                                    Text("No products found matching", fontSize = 14.sp, color = Color.Gray)
                                    Text("\"$searchQuery\"", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("No products available matching", fontSize = 14.sp, color = Color.Gray)
                                    Text("the applied filters.", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(searchedProducts) { product ->
                                ProductCard(
                                    product = product,
                                    onAddClick = {
                                        viewModel.registerExternalProduct(
                                            id = product.id,
                                            name = product.name,
                                            category = product.category,
                                            price = product.price.toDouble(),
                                            originalPrice = product.originalPrice.toDouble(),
                                            qtyUnit = product.weight,
                                            emoji = product.emoji,
                                            tintColorHex = product.tintColorHex
                                        )
                                        viewModel.addToCart(product.id)
                                        Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                                    },
                                    isFavorite = favorites.contains(product.id),
                                    onFavoriteToggle = {
                                        favorites = if (favorites.contains(product.id)) {
                                            favorites - product.id
                                        } else {
                                            favorites + product.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overlay Sheets
        if (showFilterBottomSheet) {
            FilterBottomSheet(
                categoryName = categoryName,
                subcategories = subcategories,
                selectedPriceRanges = tempSelectedPriceRanges,
                selectedProperties = tempSelectedProperties,
                filterSearchQuery = filterSearchQuery,
                onSearchQueryChange = { filterSearchQuery = it },
                onDismiss = { showFilterBottomSheet = false },
                onApply = { priceRanges, properties ->
                    selectedPriceRanges = priceRanges
                    selectedProperties = properties
                    showFilterBottomSheet = false
                },
                onClear = {
                    tempSelectedPriceRanges = emptySet()
                    tempSelectedProperties = emptySet()
                    selectedPriceRanges = emptySet()
                    selectedProperties = emptySet()
                    filterSearchQuery = ""
                    showFilterBottomSheet = false
                }
            )
        }

        if (showSortBottomSheet) {
            SortBottomSheet(
                selectedSort = selectedSort,
                onDismiss = { showSortBottomSheet = false },
                onSelectSort = { sort ->
                    selectedSort = sort
                    showSortBottomSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    categoryName: String,
    subcategories: List<String>,
    selectedPriceRanges: Set<String>,
    selectedProperties: Set<String>,
    filterSearchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: (Set<String>, Set<String>) -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {}
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Filters",
                            tint = Color.Black
                        )
                    }
                }

                OutlinedTextField(
                    value = filterSearchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    placeholder = { Text("Search across filters...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon", tint = Color.Gray)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A56DB),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                var activeTab by remember { mutableStateOf("Price") }
                
                Row(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .width(130.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFF8FAFC))
                    ) {
                        val isPriceSelected = activeTab == "Price"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeTab = "Price" }
                                .background(if (isPriceSelected) Color.White else Color.Transparent)
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(if (isPriceSelected) Color(0xFF1A56DB) else Color.Transparent)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Price",
                                fontWeight = if (isPriceSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPriceSelected) Color(0xFF1A56DB) else Color(0xFF64748B),
                                fontSize = 15.sp
                            )
                        }

                        val isPropertiesSelected = activeTab == "Properties"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeTab = "Properties" }
                                .background(if (isPropertiesSelected) Color.White else Color.Transparent)
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(if (isPropertiesSelected) Color(0xFF1A56DB) else Color.Transparent)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Properties",
                                fontWeight = if (isPropertiesSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isPropertiesSelected) Color(0xFF1A56DB) else Color(0xFF64748B),
                                fontSize = 15.sp
                            )
                        }
                    }

                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color(0xFFE2E8F0)))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        var tempPriceRanges by remember(selectedPriceRanges) { mutableStateOf(selectedPriceRanges) }
                        var tempProperties by remember(selectedProperties) { mutableStateOf(selectedProperties) }

                        if (activeTab == "Price") {
                            val priceOptions = listOf("Under ₹50", "₹50 - ₹100", "₹100 - ₹200", "₹200 - ₹500", "Above ₹500")
                            val filteredPriceOptions = if (filterSearchQuery.isEmpty()) {
                                priceOptions
                            } else {
                                priceOptions.filter { it.contains(filterSearchQuery, ignoreCase = true) }
                            }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredPriceOptions) { option ->
                                    val isSelected = tempPriceRanges.contains(option)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                tempPriceRanges = if (isSelected) {
                                                    tempPriceRanges - option
                                                } else {
                                                    tempPriceRanges + option
                                                }
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF334155)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (isSelected) Color(0xFF1A56DB) else Color(0xFFCBD5E1),
                                                    shape = CircleShape
                                                )
                                                .padding(3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFF1A56DB), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val filteredSubcategories = if (filterSearchQuery.isEmpty()) {
                                subcategories
                            } else {
                                subcategories.filter { it.contains(filterSearchQuery, ignoreCase = true) }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredSubcategories) { subcat ->
                                    val isSelected = tempProperties.contains(subcat)
                                    val count = remember(subcat) { getProductsForSubcategory(categoryName, subcat).size }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                tempProperties = if (isSelected) {
                                                    tempProperties - subcat
                                                } else {
                                                    tempProperties + subcat
                                                }
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = subcat.lowercase(),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF334155)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "($count)",
                                                fontSize = 13.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (isSelected) Color(0xFF1A56DB) else Color(0xFFCBD5E1),
                                                    shape = CircleShape
                                                )
                                                .padding(3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFF1A56DB), CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Actions inside sheet spanning right panel
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    tempPriceRanges = emptySet()
                                    tempProperties = emptySet()
                                    onClear()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF1A56DB)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A56DB))
                            ) {
                                Text("Clear Filter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Button(
                                onClick = { onApply(tempPriceRanges, tempProperties) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A56DB))
                            ) {
                                Text("Apply", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortBottomSheet(
    selectedSort: String,
    onDismiss: () -> Unit,
    onSelectSort: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {}
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Sort",
                            tint = Color.Black
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                val sortOptions = listOf(
                    "Relevance (default)",
                    "Price (low to high)",
                    "Price (high to low)",
                    "Discount (high to low)"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    sortOptions.forEach { option ->
                        val isSelected = option == selectedSort
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSort(option) }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF1A56DB) else Color(0xFF334155)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) Color(0xFF1A56DB) else Color(0xFFCBD5E1),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1A56DB), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductData,
    onAddClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top: product image floats with NO background box/card behind it
            // (matches Blinkit/Zepto style — image only, no colored container)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Emoji/illustration sits directly on the screen background —
                // no .background(), no .clip(), no shadow behind it
                Text(product.emoji, fontSize = 48.sp)

                // Favorite heart icon, top-right corner of the image area
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // ADD button, bottom-right corner, slightly overlapping the image
                Button(
                    onClick = onAddClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = 12.dp)
                        .height(26.dp)
                        .testTag("add_product_${product.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2E7D32)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "ADD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product name
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.Black
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Weight/quantity
            Text(
                text = product.weight,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Rating + review count (only if data exists)
            if (product.rating > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${product.rating}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "(${product.reviewCount})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Delivery time badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🕐", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = product.deliveryTime,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Price row: current price (bold) + strikethrough MRP
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "₹${product.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                )
                if (product.originalPrice > product.price) {
                    Text(
                        text = "₹${product.originalPrice}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DottedDivider() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
        drawLine(
            color = Color.LightGray.copy(alpha = 0.6f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}
