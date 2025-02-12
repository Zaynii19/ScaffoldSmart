package com.example.scaffoldsmart.client.client_fragments

import android.app.DatePickerDialog
import androidx.appcompat.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_viewmodel.InventoryViewModel
import com.example.scaffoldsmart.databinding.FragmentRentalReqBinding
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RentalReqFragment : BottomSheetDialogFragment() {
    private val binding by lazy {
        FragmentRentalReqBinding.inflate(layoutInflater)
    }
    private var clientName: String = ""
    private var rentalAddress: String = ""
    private var clientPhone: String = ""
    private var clientEmail: String = ""
    private var clientCnic: String = ""
    private var pipeQuantity: String = ""
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var fromDate: Calendar? = null
    private var toDate: Calendar? = null
    private var durationStart: String = ""
    private var durationEnd: String = ""
    private var diffInDays: Int = 0
    private var selectedPipeLength: Int = 0
    private var pipeLength: String = ""
    private var wenchQuantity : String = ""
    private var jointsQuantity : String = ""
    private var pumpsQuantity : String = ""
    private var motorsQuantity : String = ""
    private var generatorsQuantity : String = ""
    private var wheelQuantity : String = ""
    private lateinit var dialog: AlertDialog
    private var datePickerDialog: DatePickerDialog? = null
    private var onSendReqListener: OnSendReqListener? = null
    private lateinit var viewModel: InventoryViewModel
    private var itemList = ArrayList<InventoryModel>()
    private var totalPrice : Int = 0

    interface OnSendReqListener {
        fun onReqSendUpdated(
            clientName: String,
            rentalAddress: String,
            clientEmail: String,
            clientPhone: String,
            clientCnic: String,
            startDuration: String,
            endDuration: String,
            pipes: String,
            pipesLength: String,
            joints: String,
            wench: String,
            pumps: String,
            motors: String,
            generators: String,
            wheel: String,
            rent: Int
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[InventoryViewModel::class.java]
        viewModel.retrieveInventory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setSpinner()
        observeInventoryLiveData()
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRentalInfo()

        binding.sendBtn.setOnClickListener {
            getClientInfo()
            jointsQuantity = binding.joints.text.toString()

            if (!isClientInfoValid()) {
                Toast.makeText(context, "Please fill client info", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isDurationValid()) {
                Toast.makeText(context, "Please add start and end duration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isRentalItemSelected()) {
                Toast.makeText(context, "Please add at least one rental item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

           showSendReqDialog()
        }
    }

    private fun getClientInfo() {
        clientName = binding.name.text.toString()
        rentalAddress = binding.address.text.toString()
        clientEmail = binding.email.text.toString()
        clientPhone = binding.phone.text.toString()
        clientCnic = binding.cnic.text.toString()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getRentalInfo() {
        getRentalDuration()

        if (pipeQuantity.isEmpty()) {
            binding.pipesLength.setOnClickListener {
                // Display a message when trying to select pipe length without valid quantity
                Toast.makeText(requireContext(), "Please enter pipe quantity first.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.pipes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this specific scenario
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pipeQuantity = binding.pipes.text.toString()
                pipeLength = binding.pipesLength.text.toString()

                if (pipeQuantity.isNotEmpty()) {
                    binding.pipesLength.setOnClickListener {
                        getPipesLength()
                    }
                } else if (pipeLength.isNotEmpty()) {
                    // Clear pipe length if pipe quantity is empty and pipe length is not empty
                    binding.pipesLength.setText("")
                    binding.pipesLength.setOnClickListener(null) // Remove click listener
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for this specific scenario
            }
        })

        getSpinnerValues()
    }

    private fun getRentalDuration() {
        binding.rentalDurationFrom.setOnClickListener {
            val c = Calendar.getInstance()
            year = c[Calendar.YEAR]
            month = c[Calendar.MONTH]
            day = c[Calendar.DAY_OF_MONTH]

            datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    fromDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }

                    durationStart = formatCalendarDate(fromDate!!)

                    binding.rentalDurationFrom.setText(buildString {
                        append(selectedDay.toString())
                        append("-")
                        append((selectedMonth + 1))
                        append("-")
                        append(selectedYear)
                    })
                    datePickerDialog!!.dismiss()

                    // Only call calculateDateDifference here if toDate is already set
                    toDate?.let { calculateDateDifference() }
                }, year, month, day
            )

            datePickerDialog!!.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog!!.show()
            // Customize button colors after the dialog is shown
            datePickerDialog!!.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            datePickerDialog!!.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }

        binding.rentalDurationTo.setOnClickListener {
            val c = Calendar.getInstance()
            year = c[Calendar.YEAR]
            month = c[Calendar.MONTH]
            day = c[Calendar.DAY_OF_MONTH]

            datePickerDialog = DatePickerDialog(
                requireActivity(),
                { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                    toDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }

                    durationEnd = formatCalendarDate(toDate!!)

                    binding.rentalDurationTo.setText(buildString {
                        append(selectedDay.toString())
                        append("-")
                        append((selectedMonth + 1))
                        append("-")
                        append(selectedYear)
                    })
                    datePickerDialog!!.dismiss()

                    // Now that both dates are set, calculate the difference
                    fromDate?.let { calculateDateDifference() }
                }, year, month, day
            )

            datePickerDialog!!.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog!!.show()

            // Customize button colors after the dialog is shown
            datePickerDialog!!.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            datePickerDialog!!.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
    }

    private fun calculateDateDifference() {
        // Ensure both dates are available
        if (fromDate != null && toDate != null) {
            val diffInMillis = toDate!!.timeInMillis - fromDate!!.timeInMillis
            diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

            // Display the difference
            Toast.makeText(requireActivity(), "Difference: $diffInDays days", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatCalendarDate(calendar: Calendar): String {
        // Create a SimpleDateFormat instance with the desired format
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        // Format the date and return the string
        return dateFormat.format(calendar.time)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getPipesLength() {
        // Create a new NumberPicker
        val numberPicker = NumberPicker(requireContext()).apply {
            minValue = 5
            maxValue = 20
        }
        numberPicker.textColor = Color.BLACK

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Pipe Length")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                selectedPipeLength = numberPicker.value // Capture the selected value
                pipeLength = selectedPipeLength.toString()
                binding.pipesLength.setText(
                    buildString {
                        append(pipeLength)
                        append(" feet")
                    }
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Just dismiss the dialog
            }
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.curved_msg_view_client))
            .create()

        dialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            //Customize button colors after the dialog is shown
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
    }

    private fun setSpinner() {
        // Define the options for the Spinner
        val pumpsOptions = listOf("", "1", "2", "3")
        val generatorsOptions = listOf("", "1", "2")
        val wenchOptions = listOf("", "0", "1")
        val wheelOptions = listOf("", "1", "2", "3", "4", "5")
        val motorOptions = listOf("", "1", "2", "3")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val pumpsAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, pumpsOptions)
        val generatorsAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, generatorsOptions)
        val wenchAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, wenchOptions)
        val wheelAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, wheelOptions)
        val motorAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, motorOptions)

        // Specify the layout to use when the list of choices appears
        pumpsAdapter.setDropDownViewResource(R.layout.spinner_item)
        generatorsAdapter.setDropDownViewResource(R.layout.spinner_item)
        wenchAdapter.setDropDownViewResource(R.layout.spinner_item)
        wheelAdapter.setDropDownViewResource(R.layout.spinner_item)
        motorAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.slugPumpsQuantitySpinner.adapter = pumpsAdapter
        binding.generatorsQuantitySpinner.adapter = generatorsAdapter
        binding.wenchQuantitySpinner.adapter = wenchAdapter
        binding.wheelQuantitySpinner.adapter = wheelAdapter
        binding.motorsQuantitySpinner.adapter = motorAdapter
    }

    private fun getSpinnerValues() {
        binding.wenchQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                wenchQuantity = parent.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        binding.slugPumpsQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                pumpsQuantity = parent.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        binding.motorsQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                motorsQuantity = parent.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        binding.generatorsQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                generatorsQuantity = parent.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        binding.wheelQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected item
                wheelQuantity = parent.getItemAtPosition(position) as String
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }

    // Validation functions
    private fun isClientInfoValid(): Boolean {
        return clientName.isNotEmpty() && rentalAddress.isNotEmpty() &&
                clientPhone.isNotEmpty() && clientEmail.isNotEmpty() && clientCnic.isNotEmpty()
    }

    private fun isDurationValid(): Boolean {
        return durationStart.isNotEmpty() && durationEnd.isNotEmpty()
    }

    private fun isRentalItemSelected(): Boolean {
        return pipeQuantity.isNotEmpty() || jointsQuantity.isNotEmpty() ||
                wenchQuantity.isNotEmpty() || pumpsQuantity.isNotEmpty() ||
                motorsQuantity.isNotEmpty() || generatorsQuantity.isNotEmpty() ||
                wheelQuantity.isNotEmpty()
    }

    private fun observeInventoryLiveData() {
        viewModel.observeInventoryLiveData().observe(viewLifecycleOwner) { items ->
            itemList.clear()
            items?.let {
                itemList.addAll(it)
                Log.d("RentReqDebug", "observeInventoryLiveData: ${it.size}")
            }
        }
    }

    private fun calculateTotalPrice(): Int {
        totalPrice = 0 // Reset total price before calculation

        // Calculate price for pipes
        if (pipeQuantity.isNotEmpty()) {
            Log.d("RentReqDebug", "Pipe Quantity: $pipeQuantity")
            itemList.forEach {
                if (it.itemName == "Pipes" || it.itemName == "Scaffolding Pipes") {
                    totalPrice += it.price.toInt() * pipeQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Pipes: ${it.price} * $pipeQuantity * $diffInDays = ${it.price.toInt() * pipeQuantity.toInt() * diffInDays}")
                }
            }
        }

        // Calculate price for joints
        if (jointsQuantity.isNotEmpty()) {
            Log.d("RentReqDebug", "Joints Quantity: $jointsQuantity")
            itemList.forEach {
                if (it.itemName == "Joints") {
                    totalPrice += it.price.toInt() * jointsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Joints: ${it.price} * $jointsQuantity * $diffInDays = ${it.price.toInt() * jointsQuantity.toInt() * diffInDays}")
                }
            }
        }

        // Calculate price for wench
        if (wenchQuantity.isNotEmpty()) {
            Log.d("RentReqDebug", "Wench Quantity: $wenchQuantity")
            itemList.forEach {
                if (it.itemName == "Wench") {
                    totalPrice += it.price.toInt() * wenchQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Wench: ${it.price} * $wenchQuantity * $diffInDays = ${it.price.toInt() * wenchQuantity.toInt() * diffInDays}")
                }
            }
        }

        // Calculate price for pumps
        if (pumpsQuantity.isNotEmpty()) {
            Log.d("RentReqDebug", "Pumps Quantity: $pumpsQuantity")
            itemList.forEach {
                if (it.itemName == "Pumps" || it.itemName == "Slug Pumps") {
                    totalPrice += it.price.toInt() * pumpsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Pumps: ${it.price} * $pumpsQuantity * $diffInDays = ${it.price.toInt() * pumpsQuantity.toInt() * diffInDays}");
                }
            }
        }

        // Calculate price for generators
        if (generatorsQuantity.isNotEmpty()) {
            Log.d("RentReqDebug", "Generators Quantity: $generatorsQuantity")
            itemList.forEach {
                if (it.itemName == "Generators") {
                    totalPrice += it.price.toInt() * generatorsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Generators: ${it.price} * $generatorsQuantity * $diffInDays = ${it.price.toInt() * generatorsQuantity.toInt() * diffInDays}");
                }
            }
        }

        // Calculate price for motors
        if (motorsQuantity.isNotEmpty()) {
            Log.d("RentReqDebug", "Motors Quantity: $motorsQuantity")
            itemList.forEach {
                if (it.itemName == "Motors") {
                    totalPrice += it.price.toInt() * motorsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Motors: ${it.price} * $motorsQuantity * $diffInDays = ${it.price.toInt() * motorsQuantity.toInt() * diffInDays}");
                }
            }
        }

        // Calculate price for wheels
        if (wheelQuantity.isNotEmpty()) {
            Log.d("RentReqDebug", "Wheels Quantity: $wheelQuantity")
            itemList.forEach {
                if (it.itemName == "Wheels" || it.itemName == "Wheel Borrows") {
                    totalPrice += it.price.toInt() * wheelQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Wheels: ${it.price} * $wheelQuantity * $diffInDays = ${it.price.toInt() * wheelQuantity.toInt() * diffInDays}");
                }
            }
        }

        Log.d("RentReqDebug", "Total Price Calculated: $totalPrice")
        return totalPrice
    }

    private fun showSendReqDialog() {
        val customDialog = LayoutInflater.from(requireActivity()).inflate(R.layout.rentals_details_dialog, null)
        val binder = RentalsDetailsDialogBinding.bind(customDialog)

        binder.clientName.text = clientName
        binder.address.text = rentalAddress
        binder.phoneNum.text = clientPhone
        binder.email.text = clientEmail
        binder.cnic.text = clientCnic
        binder.estimatedRent.text = "${calculateTotalPrice()}"
        binder.rentalDurationFrom.text = durationStart
        binder.rentalDurationTo.text = durationEnd
        setViewVisibilityAndText(binder.pipes, pipeQuantity, binder.entry8)
        setViewVisibilityAndText(binder.pipesLength, pipeLength, binder.entry9)
        setViewVisibilityAndText(binder.joints, jointsQuantity, binder.entry10)
        setViewVisibilityAndText(binder.wench, wenchQuantity, binder.entry11)
        setViewVisibilityAndText(binder.slugPumps, pumpsQuantity, binder.entry12)
        setViewVisibilityAndText(binder.motors, motorsQuantity, binder.entry13)
        setViewVisibilityAndText(binder.generators, generatorsQuantity, binder.entry14)
        setViewVisibilityAndText(binder.wheel, wheelQuantity, binder.entry15)

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Send Rental Request")
            .setView(customDialog)
            .setPositiveButton("Send") { _, _ ->
                // If all validations pass, proceed with the action
                onSendReqListener?.onReqSendUpdated(
                    clientName, rentalAddress, clientEmail, clientPhone, clientCnic, durationStart,
                    durationEnd, pipeQuantity, pipeLength, jointsQuantity, wenchQuantity, pumpsQuantity,
                    motorsQuantity, generatorsQuantity, wheelQuantity, totalPrice
                )
                dismiss() // Dismiss ReqFrag only after saving data
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Just dismiss the dialog
            }
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.curved_msg_view_client))
            .create()

        dialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            //Customize button colors after the dialog is shown
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
    }

    private fun setViewVisibilityAndText(view: TextView, text: String, entry: ConstraintLayout) {
        if (text.isNotEmpty()) {
            view.text = text
        } else {
            entry.visibility = View.GONE
        }
    }

    companion object {
        fun newInstance(listener: OnSendReqListener): RentalReqFragment {
            val fragment = RentalReqFragment()
            fragment.onSendReqListener = listener
            return fragment
        }
    }
}