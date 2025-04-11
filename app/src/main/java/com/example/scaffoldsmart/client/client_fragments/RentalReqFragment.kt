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
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.databinding.FragmentRentalReqBinding
import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
import com.example.scaffoldsmart.util.CheckNetConnectvity
import com.example.scaffoldsmart.util.DateFormater
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class RentalReqFragment : BottomSheetDialogFragment() {
    private val binding by lazy {
        FragmentRentalReqBinding.inflate(layoutInflater)
    }
    private var clientName: String = ""
    private var rentalAddress: String = ""
    private var clientPhone: String = ""
    private var clientEmail: String = ""
    private var clientCnic: String = ""
    private var clientAddress: String = ""
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var fromDate: Calendar? = null
    private var toDate: Calendar? = null
    private var durationStart: String = ""
    private var durationEnd: String = ""
    private var diffInDays: Int = 0
    private var pipeQuantity: Int = 0
    private var selectedPipeLength: Int = 0
    private var pipeLength: Int = 0
    private var wenchQuantity : Int = 0
    private var jointsQuantity : Int = 0
    private var pumpsQuantity : Int = 0
    private var motorsQuantity : Int = 0
    private var generatorsQuantity : Int = 0
    private var wheelQuantity : Int = 0
    private lateinit var dialog: AlertDialog
    private var datePickerDialog: DatePickerDialog? = null
    private var onSendReqListener: OnSendReqListener? = null
    private var itemList = ArrayList<InventoryModel>()
    private var totalPrice : Int = 0

    interface OnSendReqListener {
        fun onReqSendUpdated(
            rentalAddress: String,
            startDuration: String,
            endDuration: String,
            pipes: Int,
            pipesLength: Int,
            joints: Int,
            wench: Int,
            pumps: Int,
            motors: Int,
            generators: Int,
            wheel: Int,
            rent: Int
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

                if (rentalAddress.isEmpty()) {
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

        if (binding.pipesQuantity.text!!.isEmpty()) {
            binding.pipesLength.setOnClickListener {
                // Display a message when trying to select pipe length without valid quantity
                Toast.makeText(requireContext(), "Please enter pipe quantity first.", Toast.LENGTH_SHORT).show()
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

                if (binding.pipesQuantity.text!!.isNotEmpty()) {
                    binding.pipesLength.setOnClickListener {
                        getPipesLength()
                    }
                } else if (binding.pipesLength.text!!.isNotEmpty()) {
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

                    durationStart = DateFormater.formatRentDuration(fromDate!!)

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

                    durationEnd = DateFormater.formatRentDuration(toDate!!)

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
            val lowerName = item.itemName.lowercase()
            when {
                lowerName.contains("pipe") -> {
                    binding.pipes.hint = "${item.itemName} Quantity"
                }
            }
        }

        itemList.forEach { item ->
            val lowerName = item.itemName.lowercase()
            when {
                lowerName.contains("joint") -> {
                    binding.joints.hint = "${item.itemName} Quantity"
                }
            }
        }
    }

    private fun setSpinner() {
        if (itemList.isNotEmpty()) {

            itemList.forEach { item ->
                val lowerName = item.itemName.lowercase()
                when {
                    lowerName.contains("wench") -> {
                        binding.wench.text = item.itemName
                        val wenchOptions = generateSpinnerOptions(item.quantity)
                        val wenchAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, wenchOptions)
                        wenchAdapter.setDropDownViewResource(R.layout.spinner_item)
                        binding.wenchQuantitySpinner.adapter = wenchAdapter
                    }
                }
            }

            itemList.forEach { item ->
                val lowerName = item.itemName.lowercase()
                when {
                    lowerName.contains("pump") -> {
                        binding.slugPumps.text = item.itemName
                        val pumpsOptions = generateSpinnerOptions(item.quantity)
                        val pumpsAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, pumpsOptions)
                        pumpsAdapter.setDropDownViewResource(R.layout.spinner_item)
                        binding.slugPumpsQuantitySpinner.adapter = pumpsAdapter
                    }
                }
            }

            itemList.forEach { item ->
                val lowerName = item.itemName.lowercase()
                when {
                    lowerName.contains("motor") -> {
                        binding.motors.text = item.itemName
                        val motorOptions = generateSpinnerOptions(item.quantity)
                        val motorAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, motorOptions)
                        motorAdapter.setDropDownViewResource(R.layout.spinner_item)
                        binding.motorsQuantitySpinner.adapter = motorAdapter
                    }
                }
            }

            itemList.forEach { item ->
                val lowerName = item.itemName.lowercase()
                when {
                    lowerName.contains("generator") -> {
                        binding.generators.text = item.itemName
                        val generatorsOptions = generateSpinnerOptions(item.quantity)
                        val generatorsAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, generatorsOptions)
                        generatorsAdapter.setDropDownViewResource(R.layout.spinner_item)
                        binding.generatorsQuantitySpinner.adapter = generatorsAdapter
                    }
                }
            }

            itemList.forEach { item ->
                val lowerName = item.itemName.lowercase()
                when {
                    lowerName.contains("wheel") -> {
                        binding.wheel.text = item.itemName
                        val wheelOptions = generateSpinnerOptions(item.quantity)
                        val wheelAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, wheelOptions)
                        wheelAdapter.setDropDownViewResource(R.layout.spinner_item)
                        binding.wheelQuantitySpinner.adapter = wheelAdapter
                    }
                }
            }
        } else {
            Log.d("RentReqDebug", "itemList is empty")
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
        return durationStart.isNotEmpty() && durationEnd.isNotEmpty()
    }

    private fun isRentalItemSelected(): Boolean {
        return pipeQuantity > 0 || jointsQuantity > 0 ||
                wenchQuantity > 0 || pumpsQuantity > 0 ||
                motorsQuantity > 0 || generatorsQuantity > 0 ||
                wheelQuantity > 0
    }

    private fun calculateTotalPrice(): Int {
        totalPrice = 0 // Reset total price before calculation

        // Calculate price for pipes
        if (pipeQuantity.toString().isNotEmpty()) {
            itemList.forEach {
                val lowerName = it.itemName.lowercase()
                when {
                    lowerName.contains("pipe") -> {
                    totalPrice += it.price * pipeQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Pipes: ${it.price} * $pipeQuantity * $diffInDays = ${it.price * pipeQuantity.toInt() * diffInDays}")
                    }
                }
            }
        }

        // Calculate price for joints
        if (jointsQuantity.toString().isNotEmpty()) {
            itemList.forEach {
                val lowerName = it.itemName.lowercase()
                when {
                    lowerName.contains("joint") -> {
                    totalPrice += it.price * jointsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Joints: ${it.price} * $jointsQuantity * $diffInDays = ${it.price * jointsQuantity.toInt() * diffInDays}")
                    }
                }
            }
        }

        // Calculate price for wench
        if (wenchQuantity.toString().isNotEmpty()) {
            itemList.forEach {
                val lowerName = it.itemName.lowercase()
                when {
                    lowerName.contains("wench") -> {
                    totalPrice += it.price * wenchQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Wench: ${it.price} * $wenchQuantity * $diffInDays = ${it.price * wenchQuantity.toInt() * diffInDays}")
                    }
                }
            }
        }

        // Calculate price for pumps
        if (pumpsQuantity.toString().isNotEmpty()) {
            itemList.forEach {
                val lowerName = it.itemName.lowercase()
                when {
                    lowerName.contains("pump") -> {
                    totalPrice += it.price * pumpsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Pumps: ${it.price} * $pumpsQuantity * $diffInDays = ${it.price * pumpsQuantity.toInt() * diffInDays}");
                    }
                }
            }
        }

        // Calculate price for generators
        if (generatorsQuantity.toString().isNotEmpty()) {
            itemList.forEach {
                val lowerName = it.itemName.lowercase()
                when {
                    lowerName.contains("generator") -> {
                    totalPrice += it.price * generatorsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Generators: ${it.price} * $generatorsQuantity * $diffInDays = ${it.price * generatorsQuantity.toInt() * diffInDays}");
                    }
                }
            }
        }

        // Calculate price for motors
        if (motorsQuantity.toString().isNotEmpty()) {
            itemList.forEach {
                val lowerName = it.itemName.lowercase()
                when {
                    lowerName.contains("motor") -> {
                    totalPrice += it.price * motorsQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Motors: ${it.price} * $motorsQuantity * $diffInDays = ${it.price * motorsQuantity.toInt() * diffInDays}");
                    }
                }
            }
        }

        // Calculate price for wheels
        if (wheelQuantity.toString().isNotEmpty()) {
            itemList.forEach {
                val lowerName = it.itemName.lowercase()
                when {
                    lowerName.contains("wheel") -> {
                    totalPrice += it.price * wheelQuantity.toInt() * diffInDays
                    Log.d("RentReqDebug", "Wheels: ${it.price} * $wheelQuantity * $diffInDays = ${it.price * wheelQuantity.toInt() * diffInDays}");
                    }
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
        setViewVisibilityAndText(binder.pipes, pipeQuantity, binder.entry8)
        setViewVisibilityAndText(binder.joints, jointsQuantity, binder.entry10)
        setViewVisibilityAndText(binder.wench, wenchQuantity, binder.entry11)
        setViewVisibilityAndText(binder.slugPumps, pumpsQuantity, binder.entry12)
        setViewVisibilityAndText(binder.motors, motorsQuantity, binder.entry13)
        setViewVisibilityAndText(binder.generators, generatorsQuantity, binder.entry14)
        setViewVisibilityAndText(binder.wheel, wheelQuantity, binder.entry15)

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
        fun newInstance(listener: OnSendReqListener, client: ClientModel, itemList: List<InventoryModel>): RentalReqFragment {
            val fragment = RentalReqFragment()
            fragment.onSendReqListener = listener

            val args = Bundle()
            args.putSerializable(ARG_CLIENT, client)  // Pass the client as Serializable
            args.putSerializable(ARG_LIST, ArrayList(itemList))  // Make sure to convert it to ArrayList
            fragment.arguments = args

            return fragment
        }
    }
}