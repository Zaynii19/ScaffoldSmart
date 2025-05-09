package com.example.scaffoldsmart.client

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.databinding.ActivityClientCostComparisonBinding
import com.example.scaffoldsmart.util.DateFormater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.util.Calendar

class ClientCostComparisonActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientCostComparisonBinding.inflate(layoutInflater)
    }
    private var year: Int? = null
    private var month: Int? = null
    private var day: Int? = null
    private var fromDate: Calendar? = null
    private var toDate: Calendar? = null
    private var durationStart: String? = null
    private var durationEnd: String? = null
    private var diffInDays: Int? = null
    private var selectedPipeLength: Int? = null
    private var pipeQuantity: Int? = null
    private var pipeLength: Int? = null
    private var wenchQuantity: Int? = null
    private var jointsQuantity: Int? = null
    private var pumpsQuantity: Int? = null
    private var motorsQuantity: Int? = null
    private var generatorsQuantity: Int? = null
    private var wheelQuantity: Int? = null
    private lateinit var dialog: AlertDialog
    private var datePickerDialog: DatePickerDialog? = null
    private lateinit var viewModel: InventoryViewModel
    private var itemList = ArrayList<InventoryModel>()
    private var totalPrice: Int? = null
    private var senderUid: String? = null
    private lateinit var chatPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setStatusBarColor()

        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)

        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()
        observeInventoryLiveData()

        getRentalInfo()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this@ClientCostComparisonActivity, ClientSettingActivity::class.java))
        }

        // Add listeners to all input fields to update the total rent automatically
        addInputChangeListeners()

        // Update total rent on initial load (if any fields are pre-filled)
        updateTotalRent()
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun observeInventoryLiveData() {
        viewModel.observeInventoryLiveData().observe(this@ClientCostComparisonActivity) { items ->
            itemList.clear()
            items?.let {
                itemList.addAll(it)
                setEditTest(itemList)
                setSpinner(itemList)
                Log.d("CostComparisonDebug", "observeInventoryLiveData: ${it.size}")
            }
        }
    }

    private fun setEditTest(itemList: ArrayList<InventoryModel>) {
        itemList.forEach { item ->
            val lowerName = item.itemName?.lowercase() ?: return@forEach

            when {
                lowerName.contains("pipe") -> binding.pipes.hint = "${item.itemName} Quantity"
                lowerName.contains("joint") -> binding.joints.hint = "${item.itemName} Quantity"
            }
        }
    }

    private fun setSpinner(itemList: ArrayList<InventoryModel>) {
        if (itemList.isEmpty()) {
            Log.d("RentReqDebug", "itemList is empty")
            return
        }

        // Map of item keywords to their corresponding UI components
        val spinnerConfigs = mapOf(
            "wench" to Triple(binding.wench, binding.wenchQuantitySpinner, ::generateSpinnerOptions),
            "pump" to Triple(binding.slugPumps, binding.slugPumpsQuantitySpinner, ::generateSpinnerOptions),
            "motor" to Triple(binding.motors, binding.motorsQuantitySpinner, ::generateSpinnerOptions),
            "generator" to Triple(binding.generators, binding.generatorsQuantitySpinner, ::generateSpinnerOptions),
            "wheel" to Triple(binding.wheel, binding.wheelQuantitySpinner, ::generateSpinnerOptions)
        )

        itemList.forEach { item ->
            val lowerName = item.itemName?.lowercase() ?: return@forEach
            val quantity = item.quantity ?: return@forEach

            spinnerConfigs.forEach { (keyword, config) ->
                if (lowerName.contains(keyword)) {
                    val (textView, spinner, optionsGenerator) = config

                    textView.text = item.itemName
                    val options = optionsGenerator(quantity.toInt())
                    val adapter = ArrayAdapter(this@ClientCostComparisonActivity, R.layout.spinner_item, options).apply {
                        setDropDownViewResource(R.layout.spinner_item)
                    }
                    spinner.adapter = adapter
                }
            }
        }

        // Set spinner listeners
        setSpinnerListeners()
    }

    private fun generateSpinnerOptions(quantity: Int): List<String> {
        return listOf("") + (1..quantity).map { it.toString() }
    }

    private fun setSpinnerListeners() {
        binding.wenchQuantitySpinner.onItemSelectedListener = createSpinnerListener { wenchQuantity = it }
        binding.slugPumpsQuantitySpinner.onItemSelectedListener = createSpinnerListener { pumpsQuantity = it }
        binding.motorsQuantitySpinner.onItemSelectedListener = createSpinnerListener { motorsQuantity = it }
        binding.generatorsQuantitySpinner.onItemSelectedListener = createSpinnerListener { generatorsQuantity = it }
        binding.wheelQuantitySpinner.onItemSelectedListener = createSpinnerListener { wheelQuantity = it }
    }

    private fun createSpinnerListener(onItemSelected: (Int) -> Unit): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedValue = parent.getItemAtPosition(position) as Int
                onItemSelected(selectedValue)
                updateTotalRent() // Update rent when spinner value changes
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getRentalInfo() {
        getRentalDuration()

        if (pipeQuantity == 0) {
            binding.pipesLength.setOnClickListener {
                // Display a message when trying to select pipe length without valid quantity
                Toast.makeText(this@ClientCostComparisonActivity, "Please enter pipe quantity first.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pipesQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pipeQuantity = binding.pipesQuantity.text.toString().toInt()
                pipeLength = binding.pipesLength.text.toString().toInt()

                if (pipeQuantity != 0) {
                    binding.pipesLength.setOnClickListener {
                        getPipesLength()
                    }
                } else if (pipeLength != 0) {
                    // Clear pipe length if pipe quantity is empty and pipe length is not empty
                    binding.pipesLength.setText("")
                    binding.pipesLength.setOnClickListener(null) // Remove click listener
                }
            }

            override fun afterTextChanged(s: Editable?) {
                updateTotalRent() // Update rent when pipe quantity changes
            }
        })

        // Add listener for joints quantity
        binding.jointsQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                jointsQuantity = binding.jointsQuantity.text.toString().toInt()
            }
            override fun afterTextChanged(s: Editable?) {
                updateTotalRent() // Update rent when joints quantity changes
            }
        })
    }

    private fun getRentalDuration() {
        binding.rentalDurationFrom.setOnClickListener {
            val c = Calendar.getInstance()
            year = c[Calendar.YEAR]
            month = c[Calendar.MONTH]
            day = c[Calendar.DAY_OF_MONTH]

            datePickerDialog = year?.let { y ->
                month?.let { m ->
                    day?.let { d ->
                        DatePickerDialog(
                            this@ClientCostComparisonActivity,
                            { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                                fromDate = Calendar.getInstance().apply {
                                    set(selectedYear, selectedMonth, selectedDay)
                                }

                                durationStart = DateFormater.formatRentDuration(fromDate)

                                binding.rentalDurationFrom.setText(buildString {
                                    append(selectedDay.toString())
                                    append("-")
                                    append((selectedMonth + 1))
                                    append("-")
                                    append(selectedYear)
                                })
                                datePickerDialog?.dismiss()

                                // Only call calculateDateDifference here if toDate is already set
                                toDate?.let { calculateDateDifference() }
                            }, y, m, d
                        )
                    }
                }
            }

            datePickerDialog?.datePicker?.minDate = System.currentTimeMillis() - 1000
            datePickerDialog?.show()
            // Customize button colors after the dialog is shown
            datePickerDialog?.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            datePickerDialog?.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }

        binding.rentalDurationTo.setOnClickListener {
            val c = Calendar.getInstance()
            year = c[Calendar.YEAR]
            month = c[Calendar.MONTH]
            day = c[Calendar.DAY_OF_MONTH]

            datePickerDialog = year?.let { y ->
                month?.let { m ->
                    day?.let { d ->
                        DatePickerDialog(
                            this@ClientCostComparisonActivity,
                            { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                                toDate = Calendar.getInstance().apply {
                                    set(selectedYear, selectedMonth, selectedDay)
                                }

                                durationEnd = DateFormater.formatRentDuration(toDate)

                                binding.rentalDurationTo.setText(buildString {
                                    append(selectedDay.toString())
                                    append("-")
                                    append((selectedMonth + 1))
                                    append("-")
                                    append(selectedYear)
                                })
                                datePickerDialog?.dismiss()

                                // Now that both dates are set, calculate the difference
                                fromDate?.let { calculateDateDifference() }
                            }, y, m, d
                        )
                    }
                }
            }

            datePickerDialog?.datePicker?.minDate = System.currentTimeMillis() - 1000
            datePickerDialog?.show()

            // Customize button colors after the dialog is shown
            datePickerDialog?.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            datePickerDialog?.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
    }

    private fun calculateDateDifference() {
        // Ensure both dates are available
        fromDate?.let { from ->
            toDate?.let { to ->
                val diffInMillis = to.timeInMillis - from.timeInMillis
                diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                Toast.makeText(this@ClientCostComparisonActivity, "Difference: $diffInDays days", Toast.LENGTH_SHORT).show()
                updateTotalRent()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getPipesLength() {
        // Create a new NumberPicker
        val numberPicker = NumberPicker(this@ClientCostComparisonActivity).apply {
            minValue = 5
            maxValue = 20
        }
        numberPicker.textColor = Color.BLACK

        dialog = MaterialAlertDialogBuilder(this@ClientCostComparisonActivity)
            .setTitle("Select Pipe Length")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                selectedPipeLength = numberPicker.value // Capture the selected value
                pipeLength = selectedPipeLength
                binding.pipesLength.setText(
                    buildString {
                        append(pipeLength)
                        append(" feet")
                    }
                )
                updateTotalRent() // Update rent when pipe length changes
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Just dismiss the dialog
            }
            .setBackground(ContextCompat.getDrawable(this@ClientCostComparisonActivity, R.drawable.msg_view_received))
            .create()

        dialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            // Customize button colors after the dialog is shown
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
    }

    private fun isDurationNotValid(): Boolean {
        return durationStart.isNullOrEmpty() && durationEnd.isNullOrEmpty()
    }

    private fun isRentalItemSelected(): Boolean {
        return (pipeQuantity ?: 0) > 0 ||
                (jointsQuantity ?: 0) > 0 ||
                (wenchQuantity ?: 0) > 0 ||
                (pumpsQuantity ?: 0) > 0 ||
                (motorsQuantity ?: 0) > 0 ||
                (generatorsQuantity ?: 0) > 0 ||
                (wheelQuantity ?: 0) > 0
    }

    private fun calculateTotalPrice(): Int? {
        totalPrice = 0 // Reset total price before calculation

        val itemQuantities = mapOf(
            "pipe" to pipeQuantity,
            "joint" to jointsQuantity,
            "wench" to wenchQuantity,
            "pump" to pumpsQuantity,
            "motor" to motorsQuantity,
            "generator" to generatorsQuantity,
            "wheel" to wheelQuantity
        ).filterValues { it != 0 } // Only process non-empty quantities

        itemList.forEach { item ->
            val lowerName = item.itemName?.lowercase() ?: return@forEach
            val price = item.price ?: return@forEach

            itemQuantities.forEach { (keyword, quantity) ->
                if (lowerName.contains(keyword)) {
                    val quantity = quantity
                    val itemTotal = quantity?.let { it ->
                        diffInDays?.let { q ->
                            price * it * q
                        }
                    }
                    itemTotal?.let { totalPrice = totalPrice?.plus(it) }
                    Log.d("CostComparisonDebug", "${item.itemName}: $price * $quantity * $diffInDays = $itemTotal")
                }
            }
        }

        Log.d("CostComparisonDebug", "Total Price Calculated: $totalPrice")
        return totalPrice
    }

    private fun updateTotalRent() {
        if (isRentalItemSelected() && !isDurationNotValid()) {
            val totalPrice = calculateTotalPrice()
            binding.rent.text = buildString {
                append(totalPrice)
                append(" .Rs")
            }
        } else {
            binding.rent.text = getString(R.string._0_rs) // Reset rent if inputs are invalid
        }
    }

    private fun addInputChangeListeners() {
        // Listen for changes in the rental duration
        binding.rentalDurationFrom.addTextChangedListener(createTextWatcher())
        binding.rentalDurationTo.addTextChangedListener(createTextWatcher())

        // Listen for changes in pipe quantity and length
        binding.pipesQuantity.addTextChangedListener(createTextWatcher())
        binding.pipesLength.addTextChangedListener(createTextWatcher())

        // Listen for changes in joints quantity
        binding.jointsQuantity.addTextChangedListener(createTextWatcher())
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateTotalRent() // Update rent when any input changes
            }
        }
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }

    override fun onPause() {
        super.onPause()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Offline"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }
}
