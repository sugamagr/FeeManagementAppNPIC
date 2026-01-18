package com.navoditpublic.fees.presentation.screens.settings.school_profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.navoditpublic.fees.R
import com.navoditpublic.fees.presentation.components.LoadingScreen
import com.navoditpublic.fees.presentation.theme.Saffron
import com.navoditpublic.fees.presentation.theme.SaffronAmber
import com.navoditpublic.fees.presentation.theme.SaffronDeep
import kotlinx.coroutines.delay

// Section accent colors
private val BasicInfoColor = Color(0xFF5C6BC0)    // Indigo
private val AddressColor = Color(0xFF26A69A)       // Teal
private val ContactColor = Color(0xFF7E57C2)       // Purple
private val PreviewColor = Color(0xFF78909C)       // Blue Grey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolProfileScreen(
    navController: NavController,
    viewModel: SchoolProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSuccess by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SchoolProfileEvent.Success -> {
                    showSuccess = true
                    delay(2000)
                    showSuccess = false
                }
                is SchoolProfileEvent.Error -> {
                    // Keep toast for errors
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // ==================== HERO SECTION ====================
                    item {
                        HeroSection(
                            schoolName = state.schoolName,
                            tagline = state.tagline
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                    
                    // ==================== BASIC INFORMATION ====================
                    item {
                        SectionCard(
                            title = "Basic Information",
                            icon = Icons.Outlined.School,
                            accentColor = BasicInfoColor
                        ) {
                            IconTextField(
                                value = state.schoolName,
                                onValueChange = viewModel::updateSchoolName,
                                label = "School Name",
                                icon = Icons.Outlined.School,
                                isRequired = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            IconTextField(
                                value = state.tagline,
                                onValueChange = viewModel::updateTagline,
                                label = "Tagline / Motto",
                                icon = Icons.Outlined.FormatQuote,
                                placeholder = "e.g., Nurturing Excellence"
                            )
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    
                    // ==================== ADDRESS ====================
                    item {
                        SectionCard(
                            title = "Address",
                            icon = Icons.Outlined.LocationOn,
                            accentColor = AddressColor
                        ) {
                            IconTextField(
                                value = state.addressLine1,
                                onValueChange = viewModel::updateAddressLine1,
                                label = "Address Line 1",
                                icon = Icons.Outlined.LocationOn,
                                isRequired = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            IconTextField(
                                value = state.addressLine2,
                                onValueChange = viewModel::updateAddressLine2,
                                label = "Village / Area",
                                icon = Icons.Outlined.Home
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconTextField(
                                    value = state.district,
                                    onValueChange = viewModel::updateDistrict,
                                    label = "District",
                                    icon = Icons.Outlined.LocationCity,
                                    modifier = Modifier.weight(1f)
                                )
                                IconTextField(
                                    value = state.pincode,
                                    onValueChange = viewModel::updatePincode,
                                    label = "Pincode",
                                    icon = Icons.Outlined.Pin,
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    
                    // ==================== CONTACT ====================
                    item {
                        SectionCard(
                            title = "Contact",
                            icon = Icons.Outlined.Phone,
                            accentColor = ContactColor
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconTextField(
                                    value = state.phone,
                                    onValueChange = viewModel::updatePhone,
                                    label = "Phone",
                                    icon = Icons.Outlined.Phone,
                                    keyboardType = KeyboardType.Phone,
                                    modifier = Modifier.weight(1f)
                                )
                                IconTextField(
                                    value = state.email,
                                    onValueChange = viewModel::updateEmail,
                                    label = "Email",
                                    icon = Icons.Outlined.Email,
                                    keyboardType = KeyboardType.Email,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    
                    // ==================== RECEIPT PREVIEW ====================
                    item {
                        ReceiptPreviewCard(
                            schoolName = state.schoolName,
                            tagline = state.tagline,
                            address = buildString {
                                if (state.addressLine1.isNotBlank()) append(state.addressLine1)
                                if (state.addressLine2.isNotBlank()) {
                                    if (isNotBlank()) append(", ")
                                    append(state.addressLine2)
                                }
                                if (state.district.isNotBlank()) {
                                    if (isNotBlank()) append(", ")
                                    append(state.district)
                                }
                                if (state.pincode.isNotBlank()) {
                                    if (isNotBlank()) append(" - ")
                                    append(state.pincode)
                                }
                            },
                            phone = state.phone,
                            email = state.email
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
                
                // ==================== SUCCESS BANNER ====================
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = paddingValues.calculateTopPadding() + 16.dp)
                ) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Settings saved successfully!",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // ==================== BOTTOM SAVE BUTTON ====================
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { viewModel.save() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save Changes",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ==================== HERO SECTION ====================
@Composable
private fun HeroSection(
    schoolName: String,
    tagline: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = 120.dp.toPx(),
                    center = Offset(size.width * 0.9f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = 80.dp.toPx(),
                    center = Offset(size.width * 0.1f, size.height * 0.8f)
                )
            }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SaffronDeep, Saffron, SaffronAmber),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // School Logo
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(72.dp)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.npic),
                    contentDescription = "School Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Preview text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schoolName.ifBlank { "School Name" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (schoolName.isBlank()) Color.White.copy(alpha = 0.5f) else Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (tagline.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"$tagline\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ==================== SECTION CARD ====================
@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Card Content
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.15f),
                            accentColor.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

// ==================== ICON TEXT FIELD ====================
@Composable
private fun IconTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = if (isRequired) "$label *" else label,
                color = if (isRequired && value.isBlank()) 
                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f) 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) } },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

// ==================== RECEIPT PREVIEW CARD ====================
@Composable
private fun ReceiptPreviewCard(
    schoolName: String,
    tagline: String,
    address: String,
    phone: String,
    email: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PreviewColor.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Outlined.Receipt,
                        contentDescription = null,
                        tint = PreviewColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "PDF Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "How it will appear on fee receipts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Preview Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Receipt-like styling
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dotted top border effect
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // School Name
                Text(
                    text = schoolName.uppercase().ifBlank { "SCHOOL NAME" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (schoolName.isBlank()) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
                
                // Tagline
                if (tagline.isNotBlank()) {
                    Text(
                        text = "\"$tagline\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Address
                if (address.isNotBlank()) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
                
                // Contact info
                if (phone.isNotBlank() || email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (phone.isNotBlank()) {
                            Text(
                                text = "📞 $phone",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (phone.isNotBlank() && email.isNotBlank()) {
                            Text(
                                text = "  |  ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        if (email.isNotBlank()) {
                            Text(
                                text = "✉️ $email",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Dotted bottom border effect
                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
