# 🚀 ERROR HANDLING - QUICK REFERENCE
**App-Wide Error Handling System**

---

## ⚡ **Quick Start**

### **For New Activities:**

```kotlin
// 1. Extend BaseActivity instead of AppCompatActivity
class MyActivity : BaseActivity() {
    
    // 2. Use built-in error handling methods
    fun loadData() {
        viewModel.data.observe(this) { state ->
            when (state) {
                is DataState.Error -> {
                    showError(state.message)  // ✅ That's it!
                }
                is DataState.Success -> {
                    showSuccess("Data loaded successfully")
                }
            }
        }
    }
}
```

---

## 📋 **Available Methods**

### **Error Dialog**
```kotlin
showError("Something went wrong")
showError("Failed to load", "Error Title")
showError("Invalid data") {
    // Callback after user dismisses
    finish()
}
```

### **Success Dialog**
```kotlin
showSuccess("Item saved!")
showSuccess("User created", "Success!") {
    finish()
}
```

### **Warning Dialog**
```kotlin
showWarning("This will delete all data") {
    // User clicked Continue
    deleteData()
}
```

### **Confirmation Dialog**
```kotlin
showConfirmation(
    message = "Delete this item?",
    positiveText = "Delete",
    negativeText = "Cancel",
    onConfirm = { deleteItem() }
)
```

### **Toast (Quick Messages)**
```kotlin
showToast("Copied to clipboard")
showToast("Item selected", Toast.LENGTH_LONG)
```

### **Loading Dialog**
```kotlin
showLoading("Please wait...")
// ... do work ...
dismissLoading()
```

### **Input Validation (Inline)**
```kotlin
// Don't use dialogs for validation!
// Use inline errors instead:
if (name.isEmpty()) {
    etName.error = "Name is required"
    etName.requestFocus()
    return
}
```

---

## 🎯 **Best Practices**

### **✅ DO:**
- Use `showError()` for API errors
- Use `showSuccess()` for confirmations
- Use inline `.error` for form validation
- Use `showToast()` for quick info
- Use `showConfirmation()` for destructive actions
- Always provide callbacks when needed

### **❌ DON'T:**
- Don't use `Toast` for errors (use `showError()`)
- Don't use `AlertDialog` directly (use methods)
- Don't create your own error dialogs
- Don't show toasts for important errors
- Don't forget to extend `BaseActivity`

---

## 🔥 **Common Patterns**

### **Pattern 1: ViewModel State**
```kotlin
viewModel.state.observe(this) { state ->
    when (state) {
        is State.Loading -> showLoading()
        is State.Success -> {
            dismissLoading()
            showSuccess("Done!")
        }
        is State.Error -> {
            dismissLoading()
            showError(state.message)
        }
    }
}
```

### **Pattern 2: Form Validation**
```kotlin
fun validateForm(): Boolean {
    if (etName.text.isEmpty()) {
        etName.error = "Name is required"
        etName.requestFocus()
        return false
    }
    
    if (etEmail.text.isEmpty()) {
        etEmail.error = "Email is required"
        etEmail.requestFocus()
        return false
    }
    
    return true
}
```

### **Pattern 3: Delete Confirmation**
```kotlin
fun onDeleteClick() {
    showConfirmation(
        message = "Are you sure you want to delete this item?",
        title = "Confirm Delete",
        positiveText = "Delete",
        negativeText = "Cancel",
        onConfirm = {
            viewModel.deleteItem()
        }
    )
}
```

### **Pattern 4: Network Operation**
```kotlin
fun saveData() {
    showLoading("Saving...")
    
    viewModel.save().observe(this) { result ->
        dismissLoading()
        when (result) {
            is Result.Success -> {
                showSuccess("Data saved successfully") {
                    finish()
                }
            }
            is Result.Error -> {
                showError(result.message)
            }
        }
    }
}
```

---

## 🎨 **What Users Will See**

### **Instead of:**
```
Toast: Error occurred
(disappears in 2 seconds)
```

### **They see:**
```
╔═══════════════════════╗
║ ⚠️ Error              ║
║                       ║
║ Unable to connect to  ║
║ the server.           ║
║                       ║
║ Please check your     ║
║ internet connection.  ║
║                       ║
║       [ OK ]          ║
╚═══════════════════════╝
```

---

## 📱 **Error Types Handled Automatically**

The `ErrorHandler` automatically detects and formats:

✅ Authentication errors (401, INVALID_CREDENTIALS)  
✅ Permission errors (403, FORBIDDEN)  
✅ Network errors (timeout, no connection)  
✅ Server errors (500, 502, 503)  
✅ Not found errors (404)  
✅ Validation errors (400, VALIDATION_ERROR)  
✅ Duplicate errors (409, ALREADY_EXISTS)  
✅ File errors (FILE_TOO_LARGE, INVALID_FILE_TYPE)  

**40+ error types in total!**

---

## 🛠️ **Migration Guide**

### **Step 1:** Change class declaration
```kotlin
// OLD
class MyActivity : AppCompatActivity() {

// NEW  
class MyActivity : BaseActivity() {
```

### **Step 2:** Replace Toast with appropriate method
```kotlin
// OLD - Errors
Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show()

// NEW - Errors
showError("Error occurred")

// OLD - Success
Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()

// NEW - Success
showSuccess("Success!")
```

### **Step 3:** Replace validation Toasts with inline errors
```kotlin
// OLD
Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()

// NEW
etName.error = "Name is required"
etName.requestFocus()
```

---

## 📚 **Full Documentation**

See `APP_WIDE_ERROR_HANDLING_COMPLETE.md` for:
- Complete feature list
- All 40+ error types
- Architecture details
- Testing guide
- Examples

---

## ✅ **Checklist for New Activities**

When creating a new activity:

- [ ] Extend `BaseActivity` instead of `AppCompatActivity`
- [ ] Use `showError()` for API/operation errors
- [ ] Use `showSuccess()` for confirmations
- [ ] Use inline `.error` for form validation
- [ ] Use `showConfirmation()` for destructive actions
- [ ] Use `showLoading()` / `dismissLoading()` for async ops
- [ ] Test error scenarios (network off, wrong data, etc.)

---

**That's it! Your app now has professional error handling! 🎉**

