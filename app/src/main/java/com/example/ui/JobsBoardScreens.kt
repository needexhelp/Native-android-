package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.CrowmixElevation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppViewModel

data class ConnectJobPost(
    val id: String,
    val title: String,
    val companyName: String,
    val posterAvatar: String, // Emoji
    val posterType: String, // "PERSONAL" or "BUSINESS"
    val city: String,
    val locality: String,
    val salaryType: String, // "Fixed", "Range", "Negotiable"
    val salaryMin: Double? = null,
    val salaryMax: Double? = null,
    val salaryFixed: Double? = null,
    val jobType: String, // "Full-time", "Part-time", "Freelance", "Internship", "Daily Wage"
    val postedTime: String,
    val description: String,
    val requirements: String,
    val contactPreference: String, // "Applicants message me in-app" or "Show phone number"
    val contactNumber: String,
    val isClosed: Boolean = false,
    val posterName: String
)

data class ConnectJobApplication(
    val id: String,
    val jobId: String,
    val applicantName: String,
    val applicantAvatar: String,
    val applicantPhone: String,
    val coverNote: String,
    val resumeName: String?,
    val status: String, // "Applied", "Viewed", "Shortlisted", "Rejected"
    val appliedDate: String
)

object ConnectJobsManager {
    val jobPosts = mutableStateListOf<ConnectJobPost>()
    val jobApplications = mutableStateListOf<ConnectJobApplication>()

    init {
        // Pre-populate some highly realistic job posts
        jobPosts.addAll(listOf(
            ConnectJobPost(
                id = "job_1",
                title = "Gourmet Pizza Chef / Baker",
                companyName = "Bake House",
                posterAvatar = "🍕",
                posterType = "BUSINESS",
                city = "Noida",
                locality = "Sector 62",
                salaryType = "Range",
                salaryMin = 18000.0,
                salaryMax = 25000.0,
                jobType = "Full-time",
                postedTime = "2 hours ago",
                description = "We are looking for an experienced Pizza Baker to prepare high-quality sourdough pizzas. Responsibilities include preparing pizza dought, slicing toppings, and baking pizzas in our high-temperature deck oven.",
                requirements = "- 1+ years experience baking pizzas\n- Knowledge of sourdough fermentation\n- Clean hygiene practices\n- Active food safety training certificate is a plus",
                contactPreference = "Applicants message me in-app",
                contactNumber = "+91 98765 43210",
                posterName = "Bake House"
            ),
            ConnectJobPost(
                id = "job_2",
                title = "Home Tutor for Class 10 Math & Science",
                companyName = "Abhi Singh",
                posterAvatar = "🤠",
                posterType = "PERSONAL",
                city = "Pune",
                locality = "Kalyani Nagar",
                salaryType = "Fixed",
                salaryFixed = 6000.0,
                jobType = "Freelance",
                postedTime = "1 day ago",
                description = "Hiring a highly competent local tutor to teach CBSE Class 10 Mathematics and Science. Classes will be held 3 times a week (Monday, Wednesday, Friday), 1.5 hours per session in the evening.",
                requirements = "- Strong background in Math & Physics/Chemistry\n- Previous tutoring experience with Class 10 CBSE curriculum\n- Professional, patient, and results-oriented",
                contactPreference = "Applicants message me in-app",
                contactNumber = "+91 99887 76655",
                posterName = "Abhi Singh"
            ),
            ConnectJobPost(
                id = "job_3",
                title = "Blinkit/Zepto Quick Delivery Rider",
                companyName = "Crowmix Logistics",
                posterAvatar = "🛵",
                posterType = "Noida", // Set to Noida for easy initial match
                city = "Noida",
                locality = "Sector 62",
                salaryType = "Fixed",
                salaryFixed = 15000.0,
                jobType = "Daily Wage",
                postedTime = "2 days ago",
                description = "Earn daily payouts plus attractive per-delivery and weekend bonuses. We provide flexible shift hours and an onboarding kit including uniform and delivery bag.",
                requirements = "- Must own an Android phone and a two-wheeler/bicycle\n- Valid driving license and Aadhaar card\n- Well-mannered and prompt coordinates",
                contactPreference = "Show phone number",
                contactNumber = "+91 95400 44552",
                posterName = "Crowmix Logistics"
            ),
            ConnectJobPost(
                id = "job_4",
                title = "Apprentice Salon Hairdresser",
                companyName = "Cut & Glow Salon",
                posterAvatar = "💇",
                posterType = "BUSINESS",
                city = "New Delhi",
                locality = "Connaught Place",
                salaryType = "Negotiable",
                jobType = "Part-time",
                postedTime = "3 days ago",
                description = "Seeking a junior or apprentice hairdresser to assist our senior hair specialists. Great opportunity to learn hair coloring, chemical treatments, and advanced styling techniques under direct mentorship.",
                requirements = "- Basic knowledge of hair cleaning, drying, and primary trims\n- Eager to learn, polite demeanor\n- Punctual and client-friendly",
                contactPreference = "Applicants message me in-app",
                contactNumber = "+91 88223 34455",
                posterName = "Cut & Glow"
            )
        ))

        // Add 2 initial applications
        jobApplications.addAll(listOf(
            ConnectJobApplication(
                id = "app_1",
                jobId = "job_1",
                applicantName = "Rahul Prasad",
                applicantAvatar = "🤠",
                applicantPhone = "+91 99887 76655",
                coverNote = "Hi, I have 2 years of experience working as an assistant baker at Pizza Square. I am very familiar with kneading artisan sourdough dough and maintain a clean workstation. Enthusiastic to join!",
                resumeName = "Rahul_Pizza_Resume.pdf",
                status = "Applied",
                appliedDate = "Today, 11:20 AM"
            ),
            ConnectJobApplication(
                id = "app_2",
                jobId = "job_1",
                applicantName = "Priya Sharma",
                applicantAvatar = "👩‍💻",
                applicantPhone = "+91 98112 23344",
                coverNote = "Hello, I am a freelance food designer and baking hobbyist. I have run home kitchens and styled bakeries. Open to full-time kitchen management!",
                resumeName = "Priya_Design_Baking.pdf",
                status = "Shortlisted",
                appliedDate = "Yesterday, 3:15 PM"
            )
        ))
    }
}

@Composable
fun ConnectJobsTabContent(
    currentCity: String,
    onShowCityPicker: () -> Unit,
    onNavigate: (String) -> Unit,
    onSelectJobId: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterJobType by remember { mutableStateOf("All") }
    val jobTypes = listOf("All", "Full-time", "Part-time", "Freelance", "Internship", "Daily Wage")

    val tealPrimary = Color(0xFF1FAE7A)
    val slateContent = Color(0xFF1E293B)
    val graySub = Color(0xFF64748B)

    // Filter posts by selected city and filter types, and search query
    val filteredJobs = ConnectJobsManager.jobPosts.filter { job ->
        val cityMatches = job.city.equals(currentCity, ignoreCase = true)
        val typeMatches = selectedFilterJobType == "All" || job.jobType.equals(selectedFilterJobType, ignoreCase = true)
        val queryMatches = job.title.contains(searchQuery, ignoreCase = true) ||
                job.companyName.contains(searchQuery, ignoreCase = true) ||
                job.description.contains(searchQuery, ignoreCase = true)
        cityMatches && typeMatches && queryMatches && !job.isClosed
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            // Searh and Location row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search local jobs...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp), tint = graySub) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("jobs_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tealPrimary,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Location Pill (same city picker as News)
                Surface(
                    onClick = onShowCityPicker,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("jobs_location_pill"),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(currentCity, color = slateContent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Quick access to "Apply Tracker" / "Poster dashboard" icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate("MY_APPLICATIONS") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = tealPrimary),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("My Applications", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = { onNavigate("MY_JOB_POSTS") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = tealPrimary),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBox, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("My Job Posts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Filter chips list
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(jobTypes) { filter ->
                    val isSelected = filter == selectedFilterJobType
                    val chipColor = if (isSelected) tealPrimary else Color.White
                    val textColor = if (isSelected) Color.White else slateContent
                    val borderColor = if (isSelected) tealPrimary else Color(0xFFE2E8F0)

                    Surface(
                        modifier = Modifier
                            .clickable { selectedFilterJobType = filter }
                            .testTag("job_filter_chip_$filter"),
                        color = chipColor,
                        border = BorderStroke(1.dp, borderColor),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = filter,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (filteredJobs.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💼", fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No jobs available",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = slateContent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "There are currently no active job listings matching your filters in $currentCity.",
                        fontSize = 12.sp,
                        color = graySub,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredJobs) { job ->
                        val context = LocalContext.current
                        val appliesCount = ConnectJobsManager.jobApplications.count { it.jobId == job.id }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectJobId(job.id)
                                    onNavigate("JOB_DETAIL")
                                }
                                .testTag("job_card_${job.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = CrowmixElevation.Low)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Creator icon or Business logo
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .background(Color(0xFFEDFBF5), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(job.posterAvatar, fontSize = 28.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = job.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = slateContent,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = job.companyName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = tealPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(" • ", color = graySub, fontSize = 11.sp)
                                            Text(
                                                text = "${job.locality}, ${job.city}",
                                                fontSize = 11.sp,
                                                color = graySub,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Salary and Job Type chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Salary range or custom amount
                                    val salaryStr = when (job.salaryType) {
                                        "Fixed" -> "₹${job.salaryFixed?.toInt()}/mo"
                                        "Range" -> "₹${job.salaryMin?.toInt()} - ₹${job.salaryMax?.toInt()}/mo"
                                        else -> "Salary Negotiable"
                                    }
                                    Surface(
                                        color = Color(0xFFEDFBF5),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = salaryStr,
                                            color = tealPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Job type chip
                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = job.jobType,
                                            color = graySub,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = job.postedTime,
                                        fontSize = 11.sp,
                                        color = graySub
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Divider(color = Color(0xFFF1F5F9))

                                Spacer(modifier = Modifier.height(8.dp))

                                // Preview description
                                Text(
                                    text = job.description,
                                    fontSize = 12.sp,
                                    color = slateContent.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val hasApplied = ConnectJobsManager.jobApplications.any { it.jobId == job.id && it.applicantName == "Rahul Prasad" } // Simulate as logged user "Rahul Prasad"
                                    
                                    Text(
                                        text = if (appliesCount == 0) "No applicants yet" else "$appliesCount local applicant(s)",
                                        fontSize = 11.sp,
                                        color = graySub,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (hasApplied) {
                                        Button(
                                            onClick = {},
                                            enabled = false,
                                            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFF1F5F9), disabledContentColor = graySub),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("Applied ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                // Quick Apply
                                                val app = ConnectJobApplication(
                                                    id = "app_${System.currentTimeMillis()}",
                                                    jobId = job.id,
                                                    applicantName = "Rahul Prasad", // Simulated user
                                                    applicantAvatar = "🤠",
                                                    applicantPhone = "+91 99887 76655",
                                                    coverNote = "Quick Applied with Crowmix Connection profile info",
                                                    resumeName = null,
                                                    status = "Applied",
                                                    appliedDate = "Just now"
                                                )
                                                ConnectJobsManager.jobApplications.add(app)
                                                Toast.makeText(context, "Quick Applied to ${job.companyName}!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("jobs_quick_apply_btn_${job.id}"),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Text("Quick Apply", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to post a job
        FloatingActionButton(
            onClick = { onNavigate("POST_JOB") },
            containerColor = tealPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("jobs_post_fab"),
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, "Post Job")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Post Job", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    onNavigate: (String) -> Unit,
    onOpenApply: () -> Unit
) {
    val job = ConnectJobsManager.jobPosts.firstOrNull { it.id == jobId }

    val tealPrimary = Color(0xFF1FAE7A)
    val slateContent = Color(0xFF1E293B)
    val graySub = Color(0xFF64748B)

    if (job == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Job expired or not found")
        }
        return
    }

    val context = LocalContext.current
    val hasApplied = ConnectJobsManager.jobApplications.any { it.jobId == job.id && it.applicantName == "Rahul Prasad" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details", fontWeight = FontWeight.Bold, color = slateContent) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("DASHBOARD") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = {
                        val shareStr = "Position: ${job.title} at ${job.companyName} (${job.locality}, ${job.city})\nCheck this local job out on Crowmix Connect!"
                        val intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, shareStr)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Job Info"))
                    }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Message poster secondary button
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Opening direct chat connection...", Toast.LENGTH_SHORT).show()
                            onNavigate("DASHBOARD") // Direct to chat/dashboard
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.5.dp, tealPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tealPrimary)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Message Poster", fontWeight = FontWeight.Bold)
                    }

                    // Apply Button
                    Button(
                        onClick = {
                            if (!hasApplied) onOpenApply()
                        },
                        enabled = !hasApplied,
                        modifier = Modifier.weight(1f).testTag("details_apply_now_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tealPrimary,
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledContentColor = graySub
                        )
                    ) {
                        if (hasApplied) {
                            Text("Applied ✓", fontWeight = FontWeight.Bold)
                        } else {
                            Text("Apply Now", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFEDFBF5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(job.posterAvatar, fontSize = 36.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(job.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = slateContent)
                            Spacer(Modifier.height(2.dp))
                            Text(job.companyName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = tealPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Location, Salary, job type table grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("JOB LOCATION", fontSize = 10.sp, color = graySub, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("📍 ${job.locality}, ${job.city}", color = slateContent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Column {
                            Text("EMPLOYMENT TYPE", fontSize = 10.sp, color = graySub, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("💼 ${job.jobType}", color = slateContent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("SALARY / PAY", fontSize = 10.sp, color = graySub, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            val salaryStr = when (job.salaryType) {
                                "Fixed" -> "₹${job.salaryFixed?.toInt()}/month (Fixed)"
                                "Range" -> "₹${job.salaryMin?.toInt()} - ₹${job.salaryMax?.toInt()}/month"
                                else -> "Salary Negotiable"
                            }
                            Text("💵 $salaryStr", color = tealPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Text("POSTED ON", fontSize = 10.sp, color = graySub, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("🕒 ${job.postedTime}", color = slateContent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Description card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Job Description", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = slateContent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = job.description,
                        fontSize = 13.sp,
                        color = slateContent.copy(0.9f),
                        lineHeight = 20.sp
                    )

                    if (job.requirements.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Requirements", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = slateContent)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = job.requirements,
                            fontSize = 13.sp,
                            color = slateContent.copy(0.9f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Poster card connection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEDFBF5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(job.posterAvatar, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Posted by", fontSize = 11.sp, color = graySub, fontWeight = FontWeight.SemiBold)
                        Text(job.posterName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = slateContent)
                    }
                    Button(
                        onClick = {
                            if (job.posterType == "BUSINESS") {
                                onNavigate("BUSINESS_STOREFRONT")
                            } else {
                                Toast.makeText(context, "This posting is by an individual context", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1FAE7A).copy(alpha = 0.1f), contentColor = tealPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("View Storefront", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    currentCity: String,
    onNavigate: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedJobType by remember { mutableStateOf("Full-time") }
    val jobTypes = listOf("Full-time", "Part-time", "Freelance", "Internship", "Daily Wage")

    var salaryType by remember { mutableStateOf("Fixed") } // "Fixed", "Range", "Negotiable"
    var salaryFixed by remember { mutableStateOf("") }
    var salaryMin by remember { mutableStateOf("") }
    var salaryMax by remember { mutableStateOf("") }

    var locality by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var contactPreference by remember { mutableStateOf("Applicants message me in-app") }

    var isTypeExpanded by remember { mutableStateOf(false) }

    val tealPrimary = Color(0xFF1FAE7A)
    val slateContent = Color(0xFF1E293B)
    val graySub = Color(0xFF64748B)

    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Job", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("DASHBOARD") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "JOB RECRUITMENT DETAILS",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = graySub,
                letterSpacing = 1.2.sp
            )

            // Job Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Job Title (e.g. Counter Assistant)") },
                modifier = Modifier.fillMaxWidth().testTag("post_job_title"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
            )

            // Job Type Dropdown Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedJobType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Employment Type") },
                    trailingIcon = {
                        IconButton(onClick = { isTypeExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { isTypeExpanded = true }
                )
                DropdownMenu(
                    expanded = isTypeExpanded,
                    onDismissRequest = { isTypeExpanded = false }
                ) {
                    jobTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedJobType = type
                                isTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Salary Selection row
            Text(
                "SALARY ARRANGEMENT",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = graySub,
                letterSpacing = 1.2.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Fixed", "Range", "Negotiable").forEach { opt ->
                    val isSelected = salaryType == opt
                    Button(
                        onClick = { salaryType = opt },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) tealPrimary else Color.White,
                            contentColor = if (isSelected) Color.White else slateContent
                        ),
                        border = BorderStroke(1.dp, if (isSelected) tealPrimary else Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(opt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Conditionally show salary inputs
            when (salaryType) {
                "Fixed" -> {
                    OutlinedTextField(
                        value = salaryFixed,
                        onValueChange = { salaryFixed = it },
                        label = { Text("Monthly Salary (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("post_job_salary_fixed"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
                    )
                }
                "Range" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = salaryMin,
                            onValueChange = { salaryMin = it },
                            label = { Text("Minimum Pay (₹)") },
                            modifier = Modifier.weight(1f).testTag("post_job_salary_min"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
                        )
                        OutlinedTextField(
                            value = salaryMax,
                            onValueChange = { salaryMax = it },
                            label = { Text("Maximum Pay (₹)") },
                            modifier = Modifier.weight(1f).testTag("post_job_salary_max"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
                        )
                    }
                }
            }

            // Location settings
            Text(
                "LOCATION INFO",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = graySub,
                letterSpacing = 1.2.sp
            )

            OutlinedTextField(
                value = currentCity,
                onValueChange = {},
                readOnly = true,
                label = { Text("City (uses current region)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = locality,
                onValueChange = { locality = it },
                label = { Text("Locality / Area Name (e.g. Sector 62)") },
                modifier = Modifier.fillMaxWidth().testTag("post_job_locality"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
            )

            // Details
            Text(
                "DESCRIPTION & GUIDELINES",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = graySub,
                letterSpacing = 1.2.sp
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Full description of duties (Required)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("post_job_desc"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
            )

            OutlinedTextField(
                value = requirements,
                onValueChange = { requirements = it },
                label = { Text("Specific Requirements (one bullet per line)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("post_job_requirements"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
            )

            // Contact preferences toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = contactPreference == "Show phone number",
                    onCheckedChange = { checked ->
                        contactPreference = if (checked) "Show phone number" else "Applicants message me in-app"
                    },
                    colors = CheckboxDefaults.colors(checkedColor = tealPrimary)
                )
                Text("Show my verified phone number to applicants instead of in-app messaging", fontSize = 12.sp, color = slateContent)
            }

            if (showError) {
                Text("Please fill out Job Title, Locality and Job Description", color = Color.Red, fontSize = 12.sp)
            }

            // Submit button
            Button(
                onClick = {
                    if (title.isBlank() || locality.isBlank() || description.isBlank()) {
                        showError = true
                    } else {
                        val fixedVal = salaryFixed.toDoubleOrNull()
                        val minVal = salaryMin.toDoubleOrNull()
                        val maxVal = salaryMax.toDoubleOrNull()

                        val post = ConnectJobPost(
                            id = "job_${System.currentTimeMillis()}",
                            title = title,
                            companyName = "My Business",
                            posterAvatar = "💼",
                            posterType = "BUSINESS",
                            city = currentCity,
                            locality = locality,
                            salaryType = salaryType,
                            salaryFixed = fixedVal,
                            salaryMin = minVal,
                            salaryMax = maxVal,
                            jobType = selectedJobType,
                            postedTime = "Just now",
                            description = description,
                            requirements = requirements,
                            contactPreference = contactPreference,
                            contactNumber = "+91 98765 43210",
                            posterName = "Self Post"
                        )
                        ConnectJobsManager.jobPosts.add(0, post)
                        Toast.makeText(context, "Job successfully posted!", Toast.LENGTH_SHORT).show()
                        onNavigate("DASHBOARD")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("post_job_submit_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Post Job Immediately 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyJobPostsScreen(
    onNavigate: (String) -> Unit,
    onSelectJobId: (String) -> Unit
) {
    val tealPrimary = Color(0xFF1FAE7A)
    val slateContent = Color(0xFF1E293B)
    val graySub = Color(0xFF64748B)

    // Filter jobs that have companyName == "Bake House" (simulate as poster "Bake House")
    val postedJobs = ConnectJobsManager.jobPosts.filter { it.companyName == "Bake House" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Job Postings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("DASHBOARD") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (postedJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You have not posted any jobs yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(postedJobs) { job ->
                        val applicantsCount = ConnectJobsManager.jobApplications.count { it.jobId == job.id }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectJobId(job.id)
                                    onNavigate("JOB_APPLICANTS")
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = job.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = slateContent
                                    )
                                    Surface(
                                        color = if (job.isClosed) Color.LightGray else tealPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (job.isClosed) "Filled" else "Active",
                                            color = if (job.isClosed) graySub else tealPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📍 ${job.locality}, ${job.city}", fontSize = 12.sp, color = graySub)
                                    Text(" • ", color = graySub)
                                    Text(job.jobType, fontSize = 12.sp, color = graySub)
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$applicantsCount Local Applicant(s)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = tealPrimary
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Review Applications", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tealPrimary)
                                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = tealPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobApplicantsScreen(
    jobId: String,
    onNavigate: (String) -> Unit
) {
    val job = ConnectJobsManager.jobPosts.firstOrNull { it.id == jobId }
    val applicants = ConnectJobsManager.jobApplications.filter { it.jobId == jobId }

    val tealPrimary = Color(0xFF1FAE7A)
    val slateContent = Color(0xFF1E293B)
    val graySub = Color(0xFF64748B)

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(job?.title ?: "Applicants", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("MY_JOB_POSTS") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (applicants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No applicants yet for this job role")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(applicants) { app ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0xFFEDFBF5), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(app.applicantAvatar, fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.applicantName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = slateContent)
                                        Text("Applied on ${app.appliedDate}", fontSize = 11.sp, color = graySub)
                                    }
                                    Surface(
                                        color = when (app.status) {
                                            "Shortlisted" -> Color(0xFFEDFBF5)
                                            "Rejected" -> Color(0xFFFDF2F2)
                                            else -> Color(0xFFF1F5F9)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = app.status,
                                            color = when (app.status) {
                                                "Shortlisted" -> tealPrimary
                                                "Rejected" -> Color.Red
                                                else -> graySub
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (app.coverNote.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "\"${app.coverNote}\"",
                                            fontSize = 12.sp,
                                            color = slateContent.copy(0.85f),
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                if (app.resumeName != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Icon(Icons.Default.Info, null, tint = tealPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = app.resumeName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = slateContent,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("Open", color = tealPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Opening direct messaging...", Toast.LENGTH_SHORT).show()
                                            onNavigate("DASHBOARD")
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = tealPrimary, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Send, null, Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Message", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val currentIdx = ConnectJobsManager.jobApplications.indexOf(app)
                                            if (currentIdx != -1) {
                                                ConnectJobsManager.jobApplications[currentIdx] = app.copy(status = "Shortlisted")
                                            }
                                            Toast.makeText(context, "${app.applicantName} shortlisted!", Toast.LENGTH_SHORT).show()
                                        },
                                        enabled = app.status != "Shortlisted",
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDFBF5), contentColor = tealPrimary, disabledContainerColor = Color(0xFFEDFBF5).copy(0.4f), disabledContentColor = graySub),
                                        border = BorderStroke(1.dp, tealPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Shortlist ⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobApplicationScreen(
    jobId: String,
    onNavigate: (String) -> Unit
) {
    val job = ConnectJobsManager.jobPosts.firstOrNull { it.id == jobId }

    var coverNote by remember { mutableStateOf("") }
    var resumeName by remember { mutableStateOf<String?>(null) }

    val tealPrimary = Color(0xFF1FAE7A)
    val slateContent = Color(0xFF1E293B)
    val graySub = Color(0xFF64748B)

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply Now", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("JOB_DETAIL") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary header
            if (job != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFEDFBF5), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(job.posterAvatar, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(job.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = slateContent)
                            Text(job.companyName, fontSize = 11.sp, color = graySub)
                        }
                    }
                }
            }

            Text(
                "APPLICANT PROFILE INFO (AUTO-FILLED)",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = graySub,
                letterSpacing = 1.2.sp
            )

            // Auto-filled profile specs
            OutlinedTextField(
                value = "Rahul Prasad",
                onValueChange = {},
                readOnly = true,
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = "+91 99887 76655",
                onValueChange = {},
                readOnly = true,
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth()
            )

            // Cover Note (Free Text)
            Text(
                "COVER NOTE / BIO FOR POSTER",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = graySub,
                letterSpacing = 1.2.sp
            )

            OutlinedTextField(
                value = coverNote,
                onValueChange = { coverNote = it },
                label = { Text("Pitch yourself (optional info about experience...)") },
                placeholder = { Text("Hi, I have similar experience and would love to help out!") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("apply_cover_note"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tealPrimary)
            )

            // Resume attach simulation
            Text(
                "ATTACH RESUME / WORK SAMPLES (OPTIONAL)",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = graySub,
                letterSpacing = 1.2.sp
            )

            Surface(
                onClick = {
                    resumeName = "Resume_Rahul_Prasad_${System.currentTimeMillis() % 10000}.pdf"
                    Toast.makeText(context, "Attached mock PDF resume!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_resume_picker"),
                color = Color.White,
                border = BorderStroke(1.5.dp, if (resumeName != null) tealPrimary else Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (resumeName != null) {
                        Icon(Icons.Default.Check, null, tint = tealPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(resumeName ?: "", fontWeight = FontWeight.Bold, color = slateContent, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap to change attached document", fontSize = 11.sp, color = graySub)
                    } else {
                        Icon(Icons.Default.Info, null, tint = tealPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Simulate Attaching Resume Document", fontWeight = FontWeight.Bold, color = slateContent, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Supports PDF, DOCX up to 5MB", fontSize = 11.sp, color = graySub)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit application
            Button(
                onClick = {
                    val app = ConnectJobApplication(
                        id = "app_${System.currentTimeMillis()}",
                        jobId = jobId,
                        applicantName = "Rahul Prasad",
                        applicantAvatar = "🤠",
                        applicantPhone = "+91 99887 76655",
                        coverNote = coverNote,
                        resumeName = resumeName,
                        status = "Applied",
                        appliedDate = "Today"
                    )
                    ConnectJobsManager.jobApplications.add(0, app)
                    Toast.makeText(context, "Application submitted successfully!", Toast.LENGTH_SHORT).show()
                    onNavigate("DASHBOARD")
                },
                colors = ButtonDefaults.buttonColors(containerColor = tealPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_job_application_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit Application 📑", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    onNavigate: (String) -> Unit,
    onSelectJobId: (String) -> Unit
) {
    val tealPrimary = Color(0xFF1FAE7A)
    val slateContent = Color(0xFF1E293B)
    val graySub = Color(0xFF64748B)

    // Applications submitted by Rahul Prasad
    val applications = ConnectJobsManager.jobApplications.filter { it.applicantName == "Rahul Prasad" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Applications Tracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("DASHBOARD") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (applications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You have not applied to any jobs yet")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(applications) { app ->
                        val job = ConnectJobsManager.jobPosts.firstOrNull { it.id == app.jobId }

                        if (job != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectJobId(job.id)
                                        onNavigate("JOB_DETAIL")
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = job.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = slateContent
                                        )
                                        Surface(
                                            color = when (app.status) {
                                                "Shortlisted" -> Color(0xFFEDFBF5)
                                                "Rejected" -> Color(0xFFFDF2F2)
                                                else -> Color(0xFFF1F5F9)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = app.status,
                                                color = when (app.status) {
                                                    "Shortlisted" -> tealPrimary
                                                    "Rejected" -> Color.Red
                                                    else -> graySub
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(job.companyName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = tealPrimary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Applied on: ${app.appliedDate}", fontSize = 11.sp, color = graySub)

                                    if (app.status == "Shortlisted") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            color = Color(0xFFEDFBF5),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, null, tint = tealPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    "Congratulations! The poster shortlisted your profile. Check messages for follow-up details.",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = tealPrimary
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
        }
    }
}
