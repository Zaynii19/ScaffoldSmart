package com.example.scaffoldsmart.client.client_bottomsheets

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
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.client.ClientCostComparisonActivity
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.example.scaffoldsmart.databinding.SendRentalReqBinding
import com.example.scaffoldsmart.util.CheckNetConnectvity
import com.example.scaffoldsmart.util.DateFormater
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class SendRentalReq : BottomSheetDialogFragment() {
    private val binding by lazy {
        SendRentalReqBinding.inflate(layoutInflater)
    }
    private var clientName: String? = null
    private var rentalAddress: String? = null
    private var clientPhone: String? = null
    private var clientEmail: String? = null
    private var clientCnic: String? = null
    private var clientAddress: String? = null
    private var year: Int? = null
    private var month: Int? = null
    private var day: Int? = null
    private var fromDate: Calendar? = null
    private var toDate: Calendar? = null
    private var durationStart: String? = null
    private var durationEnd: String? = null
    private var diffInDays: Int? = null
    private var pipeQuantity: Int? = null
    private var selectedPipeLength: Int? = null
    private var pipeLength: Int? = null
    private var wenchQuantity : Int? = null
    private var jointsQuantity : Int? = null
    private var pumpsQuantity : Int? = null
    private var motorsQuantity : Int? = null
    private var generatorsQuantity : Int? = null
    private var wheelQuantity : Int? = null
    private lateinit var dialog: AlertDialog
    private var datePickerDialog: DatePickerDialog? = null
    private var onSendReqListener: OnSendReqListener? = null
    private var itemList = ArrayList<InventoryModel>()
    private var totalPrice : Int = 0

    interface OnSendReqListener {
        fun onReqSendUpdated(
            rentalAddress: String?,
            startDuration: String?,
            endDuration: String?,
            pipes: Int?,
            pipesLength: Int?,
            joints: Int?,
            wench: Int?,
            pumps: Int?,
            motors: Int?,
            generators: Int?,
            wheel: Int?,
            rent: Int?
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the list of rental requests from arguments safely
        arguments?.let {
            itemList = it.getSerializable(ARG_LIST) as ArrayList<InventoryModel>
            Log.d("RentReqDebug", "Inventory size: ${it.size()}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setEditTest()
        setSpinner()
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRentalInfo()

        binding.sendBtn.setOnClickListener {
            if (CheckNetConnectvity.hasInternetConnection(requireActivity())) {

                getClientInfo()
                rentalAddress = binding.rentalAddress.text.toString()

                jointsQuantity = try {
                    binding.jointsQuantity.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    0
                }

                if (rentalAddress.isNullOrEmpty()) {
                    Toast.makeText(context, "Please add rental Address", Toast.LENGTH_SHORT).show()
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

            } else {
                Toast.makeText(requireActivity(), "Please check your internet connection and try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getClientInfo() {
        val client: ClientModel = arguments?.getSerializable(ARG_CLIENT) as ClientModel
        clientName = client.name
        clientEmail = client.email
        clientPhone = client.phone
        clientCnic = client.cnic
        clientAddress = client.address
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getRentalInfo() {
        getRentalDuration()

        binding.pipesQuantity.text?.let {
            if (it.isEmpty()) {
                binding.pipesLength.setOnClickListener {
                    // Display a message when trying to select pipe length without valid quantity
                    Toast.makeText(requireContext(), "Please enter pipe quantity first.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.pipesQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this specific scenario
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                pipeQuantity = try {
                    binding.pipesQuantity.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    0
                }

                pipeLength = try {
                    binding.pipesLength.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    0
                }

                binding.pipesQuantity.text?.let {
                    if (it.isNotEmpty()) {
                        binding.pipesLength.setOnClickListener {
                            getPipesLength()
                        }
                    } else binding.pipesLength.text?.let { it1 ->
                        if (it1.isNotEmpty()) {
                            // Clear pipe length if pipe quantity is empty and pipe length is not empty
                            binding.pipesLength.setText("")
                            binding.pipesLength.setOnClickListener(null) // Remove click listener
                        }
                    }
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

            datePickerDialog = year?.let { y ->
                month?.let { m ->
                    day?.let { d ->
                        DatePickerDialog(
                            requireActivity(),
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
                            requireActivity(),
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
                Toast.makeText(requireActivity(), "Difference: $diffInDays days", Toast.LENGTH_SHORT).show()
            }
        }
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
                pipeLength = selectedPipeLength
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
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
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

    private fun setEditTest() {
        itemList.forEach { item ->
            val lowerName = item.itemName?.lowercase() ?: return@forEach
            when {
                lowerName.contains("pipe") -> binding.pipes.hint = "${item.itemName} Quantity"
                lowerName.contains("joint") -> binding.joints.hint = "${item.itemName} Quantity"
            }
        }
    }

    private fun setSpinner() {
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
            val lowerName = item.itemName?.lowercase() ?: run {
                Log.w("SpinnerSetup", "Skipped item with null name")
                return@forEach
            }

            val quantity = item.quantity ?: run {
                Log.w("SpinnerSetup", "Skipped ${item.itemName} with null quantity")
                return@forEach
            }

            spinnerConfigs.forEach { (keyword, config) ->
                if (lowerName.contains(keyword)) {
                    val (textView, spinner, optionsGenerator) = config

                    textView.text = item.itemName
                    val options = optionsGenerator(quantity)
                    val adapter = ArrayAdapter(
                        requireActivity(),
                        R.layout.spinner_item,
                        options
                    ).apply {
                        setDropDownViewResource(R.layout.spinner_item)
                    }
                    spinner.adapter = adapter
                    return@forEach  // Exit after first match
                }
            }
        }
    }

    private fun generateSpinnerOptions(quantity: Int): List<String> {
        return listOf("0") + (1..quantity).map { it.toString() }
    }

    private fun getSpinnerValues() {
        binding.wenchQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                wenchQuantity = if (selected.isNotEmpty()) selected.toInt() else 0
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                wenchQuantity = 0
            }
        }

        binding.slugPumpsQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                pumpsQuantity = if (selected.isNotEmpty()) selected.toInt() else 0
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                pumpsQuantity = 0
            }
        }

        binding.motorsQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                motorsQuantity = if (selected.isNotEmpty()) selected.toInt() else 0
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                motorsQuantity = 0
            }
        }

        binding.generatorsQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                generatorsQuantity = if (selected.isNotEmpty()) selected.toInt() else 0
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                generatorsQuantity = 0
            }
        }

        binding.wheelQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                wheelQuantity = if (selected.isNotEmpty()) selected.toInt() else 0
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                wheelQuantity = 0
            }
        }
    }

    private fun isDurationValid(): Boolean {
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

    private fun calculateTotalPrice(): Int {
        totalPrice = 0 // Reset total price before calculation

        val itemQuantities = mapOf(
            "pipe" to pipeQuantity,
            "joint" to jointsQuantity,
            "wench" to wenchQuantity,
            "pump" to pumpsQuantity,
            "motor" to motorsQuantity,
            "generator" to generatorsQuantity,
            "wheel" to wheelQuantity
        ).filterValues { it != 0 } // Filter empty quantities

        itemList.forEach { item ->
            val lowerName = item.itemName?.lowercase() ?: run {
                Log.w("PriceCalc", "Skipped item with null name")
                return@forEach
            }

            val price = item.price ?: run {
                Log.w("PriceCalc", "Skipped $lowerName with null price")
                return@forEach
            }

            itemQuantities.forEach { (keyword, quantity) ->
                if (lowerName.contains(keyword)) {
                    val itemTotal = quantity?.let { it ->
                        diffInDays?.let { q ->
                            price * it * q
                        }
                    }
                    itemTotal?.let { totalPrice += it }
                    Log.d("RentReqDebug",
                        "${item.itemName}: $price * $quantity * $diffInDays = $itemTotal")
                    return@forEach // Exit after first match
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
        binder.address.text = clientAddress
        binder.phoneNum.text = clientPhone
        binder.email.text = clientEmail
        binder.cnic.text = clientCnic
        binder.rent.text = buildString {
            append(calculateTotalPrice())
            append(" .Rs")
        }
        binder.rentalAddress.text = rentalAddress
        binder.rentalDurationFrom.text = durationStart
        binder.rentalDurationTo.text = durationEnd

        // Regular quantities (just numbers)
        pipeQuantity?.let { setViewVisibilityAndText(binder.pipes, it, binder.entry8) }
        jointsQuantity?.let { setViewVisibilityAndText(binder.joints, it, binder.entry10) }
        wenchQuantity?.let { setViewVisibilityAndText(binder.wench, it, binder.entry11) }
        pumpsQuantity?.let { setViewVisibilityAndText(binder.slugPumps, it, binder.entry12) }
        motorsQuantity?.let { setViewVisibilityAndText(binder.motors, it, binder.entry13) }
        generatorsQuantity?.let { setViewVisibilityAndText(binder.generators, it, binder.entry14) }
        wheelQuantity?.let { setViewVisibilityAndText(binder.wheel, it, binder.entry15) }

        // Special case for pipe length (with "feet" unit)
        if (pipeLength != 0) {
            binder.pipesLength.text = buildString {
                append(pipeLength)
                append(" feet")
            }
            binder.entry9.visibility = View.VISIBLE
        } else {
            binder.entry9.visibility = View.GONE
        }

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Send Rental Request")
            .setView(customDialog)
            .setPositiveButton("Send") { _, _ ->
                onSendReqListener?.onReqSendUpdated(
                    rentalAddress, durationStart, durationEnd, pipeQuantity,
                    pipeLength, jointsQuantity, wenchQuantity, pumpsQuantity,
                    motorsQuantity, generatorsQuantity, wheelQuantity, totalPrice
                )
                Toast.makeText(requireActivity(), "Your rental request sent to Admin", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.msg_view_received))
            .create()

        dialog.apply {
            show()
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
        }
    }

    private fun setViewVisibilityAndText(view: TextView, quantity: Int, entry: ConstraintLayout) {
        if (quantity != 0) {
            view.text = "$quantity"
            entry.visibility = View.VISIBLE
        } else {
            entry.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_CLIENT = "arg_client"
        private const val ARG_LIST = "arg_list"
        fun newInstance(listener: OnSendReqListener, client: ClientModel?, itemList: List<InventoryModel>): SendRentalReq {
            val fragment = SendRentalReq()
            fragment.onSendReqListener = listener

            val args = Bundle()
            args.putSerializable(ARG_CLIENT, client)  // Pass the client as Serializable
            args.putSerializable(ARG_LIST, ArrayList(itemList))  // Make sure to convert it to ArrayList
            fragment.arguments = args

            return fragment
        }
    }
}