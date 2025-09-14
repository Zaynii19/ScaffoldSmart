    package com.example.scaffoldsmart.client.client_bottomsheets

    import android.app.DatePickerDialog
    import androidx.appcompat.app.AlertDialog
    import android.graphics.Color
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.DatePicker
    import android.widget.TextView
    import android.widget.Toast
    import androidx.annotation.RequiresApi
    import androidx.core.content.ContextCompat
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.scaffoldsmart.R
    import com.example.scaffoldsmart.admin.admin_models.RentalItem
    import com.example.scaffoldsmart.client.client_adapters.RentalDetailRcvAdapter
    import com.example.scaffoldsmart.client.client_models.CartModel
    import com.example.scaffoldsmart.client.client_models.ClientModel
    import com.example.scaffoldsmart.databinding.RentalsDetailsDialogBinding
    import com.example.scaffoldsmart.databinding.SendRentalReqBinding
    import com.example.scaffoldsmart.util.CheckNetConnectvity
    import com.example.scaffoldsmart.util.DateFormater
    import com.example.scaffoldsmart.util.parcelable
    import com.example.scaffoldsmart.util.parcelableArrayList
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
        private var fromDate: Calendar? = null
        private var toDate: Calendar? = null
        private var durationStart: String? = null
        private var durationEnd: String? = null
        private var diffInDays: Int? = null
        private lateinit var dialog: AlertDialog
        private var datePickerDialog: DatePickerDialog? = null
        private var onSendReqListener: OnSendReqListener? = null
        private var cartItemList = ArrayList<CartModel>()
        private var itemList = ArrayList<RentalItem>()
        private var totalRent : Int = 0
        private lateinit var adapter: RentalDetailRcvAdapter

        interface OnSendReqListener {
            fun onReqSend(
                rentalAddress: String?,
                startDuration: String?,
                endDuration: String?,
                rent: Int?,
                itemList: ArrayList<RentalItem>?
            )
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Retrieve the list of rental requests from arguments safely
            cartItemList = arguments?.parcelableArrayList<CartModel>(ARG_LIST) ?: arrayListOf()

            populateItemList(cartItemList)
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            // Inflate the layout for this fragment
            return binding.root
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            getRentalDuration()

            binding.sendBtn.setOnClickListener {
                if (CheckNetConnectvity.hasInternetConnection(requireActivity())) {

                    getClientInfo()
                    rentalAddress = binding.rentalAddress.text.toString()

                    if (rentalAddress.isNullOrEmpty()) {
                        Toast.makeText(context, "Please add rental Address", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (isDurationNotValid()) {
                        Toast.makeText(context, "Please add start and end duration", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                   showSendReqDialog()

                } else {
                    Toast.makeText(requireActivity(), "Please check your internet connection and try again", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun populateItemList (cartItems: ArrayList<CartModel>) {
            itemList.clear()
            for (item in cartItems) {
                itemList.add(RentalItem(item.itemName, item.itemQuantity, item.itemPrice, item.pipeLength))
            }
        }

        private fun getClientInfo() {
            val client: ClientModel = arguments?.parcelable<ClientModel>(ARG_CLIENT) ?: ClientModel()
            clientName = client.name
            clientEmail = client.email
            clientPhone = client.phone
            clientCnic = client.cnic
            clientAddress = client.address
        }

        private fun getRentalDuration() {
            binding.rentalDurationFrom.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c[Calendar.YEAR]
                val month = c[Calendar.MONTH]
                val day = c[Calendar.DAY_OF_MONTH]
                datePickerDialog = DatePickerDialog(requireActivity(),
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
                    }, year, month, day)

                datePickerDialog?.datePicker?.minDate = System.currentTimeMillis() - 1000
                datePickerDialog?.show()
                // Customize button colors after the dialog is shown
                datePickerDialog?.getButton(DatePickerDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLUE)
                datePickerDialog?.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLUE)
            }

            binding.rentalDurationTo.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c[Calendar.YEAR]
                val month = c[Calendar.MONTH]
                val day = c[Calendar.DAY_OF_MONTH]

                datePickerDialog = DatePickerDialog(requireActivity(),
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

                        // Only call calculateDateDifference here if toDate is already set
                        toDate?.let { calculateDateDifference() }
                    }, year, month, day)

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

        private fun isDurationNotValid(): Boolean {
            return durationStart.isNullOrEmpty() && durationEnd.isNullOrEmpty()
        }

        private fun calculateTotalPrice(): Int {
            totalRent = 0 // Reset total price before calculation

            cartItemList.forEach { item ->

                val isPipe = item.itemName?.lowercase()?.contains("pipe") == true
                val itemTotal = if (isPipe) {
                    // Calculate total for pipe: quantity * price * duration (days)
                    diffInDays?.let { (item.itemQuantity ?: 0) * (item.itemPrice ?: 0) * (item.pipeLength ?: 0)  * it }
                } else {
                    // Calculate total for all other each item: quantity * price * duration (days)
                    diffInDays?.let { (item.itemQuantity ?: 0) * (item.itemPrice ?: 0) * it }
                }

                // Add to the running total
                itemTotal?.let { totalRent += it }

                Log.d("RentReqDebug", "${item.itemName}: ${item.itemQuantity} * ${item.itemPrice} * $diffInDays = $itemTotal")
            }

            Log.d("RentReqDebug", "Total Price Calculated: $totalRent")
            return totalRent
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

            setRcv(binder)

            dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Send Rental Request")
                .setView(customDialog)
                .setPositiveButton("Send") { _, _ ->
                    onSendReqListener?.onReqSend(rentalAddress, durationStart, durationEnd, totalRent, itemList)
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

        private fun setRcv(binder: RentalsDetailsDialogBinding) {
            binder.rcv.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = RentalDetailRcvAdapter(requireActivity(), itemList)
            binder.rcv.adapter = adapter
            binder.rcv.setHasFixedSize(true)
        }

        companion object {
            private const val ARG_CLIENT = "arg_client"
            private const val ARG_LIST = "arg_list"
            fun newInstance(listener: OnSendReqListener, client: ClientModel?, cartItemList: ArrayList<CartModel>): SendRentalReq {
                val fragment = SendRentalReq()
                fragment.onSendReqListener = listener

                val args = Bundle()
                args.putParcelable(ARG_CLIENT, client)  // Pass the client as Parcelable
                args.putParcelableArrayList(ARG_LIST, ArrayList(cartItemList))
                fragment.arguments = args

                return fragment
            }
        }
    }