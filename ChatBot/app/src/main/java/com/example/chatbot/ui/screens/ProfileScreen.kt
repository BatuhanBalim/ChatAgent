package com.example.chatbot.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chatbot.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Profile screen to display and edit user's personal information
 */
@Composable
fun ProfileScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.profileState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    
    // Editable profile fields
    var nameValue by remember { mutableStateOf("") }
    var birthdayValue by remember { mutableStateOf("") }
    var occupationValue by remember { mutableStateOf("") }
    var hobbiesValue by remember { mutableStateOf("") }
    
    // Update local state when profile data changes
    LaunchedEffect(uiState) {
        nameValue = uiState.name
        birthdayValue = uiState.birthday
        occupationValue = uiState.occupation
        hobbiesValue = uiState.hobbies
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Personal Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isEditing) {
                        // Edit mode
                        ProfileEditField(
                            label = "Name",
                            value = nameValue,
                            onValueChange = { nameValue = it }
                        )
                        
                        BirthdayEditField(
                            label = "Birthday",
                            value = birthdayValue,
                            onValueChange = { birthdayValue = it }
                        )
                        
                        ProfileEditField(
                            label = "Occupation",
                            value = occupationValue,
                            onValueChange = { occupationValue = it }
                        )
                        
                        ProfileEditField(
                            label = "Hobbies/Interests",
                            value = hobbiesValue,
                            onValueChange = { hobbiesValue = it }
                        )
                    } else {
                        // View mode
                        ProfileField(label = "Name", value = uiState.name)
                        ProfileField(label = "Birthday", value = uiState.birthday)
                        ProfileField(label = "Occupation", value = uiState.occupation)
                        ProfileField(label = "Hobbies/Interests", value = uiState.hobbies)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Edit/Save button
            Button(
                onClick = {
                    if (isEditing) {
                        // Save changes
                        viewModel.updateProfile(
                            name = nameValue,
                            birthday = birthdayValue,
                            occupation = occupationValue,
                            hobbies = hobbiesValue
                        )
                    }
                    isEditing = !isEditing
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isEditing) "Save Profile" else "Edit Profile")
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value.ifEmpty { "Not set" },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable { showDatePicker = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date"
                    )
                }
            },
            placeholder = {
                Text("Select your birthday")
            }
        )
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                selectedDate?.let {
                    val formattedDate = dateFormatter.format(Date(it))
                    onValueChange(formattedDate)
                }
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            },
            datePickerState = datePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp)
        )
    }
}
