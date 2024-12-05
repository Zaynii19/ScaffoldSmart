package com.example.scaffoldsmart.client.client_fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentRentalReqBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class RentalReqFragment : BottomSheetDialogFragment() {
    private val binding by lazy {
        FragmentRentalReqBinding.inflate(layoutInflater)
    }
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var fromDate: Calendar? = null
    private var toDate: Calendar? = null
    private var diffInDays: Int = 0
    private var datePickerDialog: DatePickerDialog? = null
    private var onSendReqListener: OnSendReqListener? = null

    interface OnSendReqListener {
        fun onReqSendUpdated(clientName: String)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setSpinner()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getRentalDuration()

        binding.sendBtn.setOnClickListener {
            val clientName = binding.name.text.toString()

            if (clientName.isNotEmpty()) {
                onSendReqListener?.onReqSendUpdated(clientName)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add Name", Toast.LENGTH_SHORT).show()
            }
        }

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

    private fun setSpinner() {
        // Define the options for the Spinner
        val pipesLengthOptions = listOf("5 to 10 feet", "10 to 15 feet", "15 to 20 feet")
        val pumpsOptions = listOf("1", "2", "3")
        val generatorsOptions = listOf("1", "2")
        val wenchOptions = listOf("0", "1")
        val wheelOptions = listOf("1", "2", "3", "4", "5")
        val motorOptions = listOf("1", "2", "3")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val pipesLengthAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, pipesLengthOptions)
        val pumpsAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, pumpsOptions)
        val generatorsAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, generatorsOptions)
        val wenchAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, wenchOptions)
        val wheelAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, wheelOptions)
        val motorAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_item, motorOptions)

        // Specify the layout to use when the list of choices appears
        pipesLengthAdapter.setDropDownViewResource(R.layout.spinner_item)
        pumpsAdapter.setDropDownViewResource(R.layout.spinner_item)
        generatorsAdapter.setDropDownViewResource(R.layout.spinner_item)
        wenchAdapter.setDropDownViewResource(R.layout.spinner_item)
        wheelAdapter.setDropDownViewResource(R.layout.spinner_item)
        motorAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.pipesLengthSpinner.adapter = pipesLengthAdapter
        binding.slugPumpsQuantitySpinner.adapter = pumpsAdapter
        binding.generatorsQuantitySpinner.adapter = generatorsAdapter
        binding.wenchQuantitySpinner.adapter = wenchAdapter
        binding.wheelQuantitySpinner.adapter = wheelAdapter
        binding.motorsQuantitySpinner.adapter = motorAdapter
    }

    companion object {
        fun newInstance(listener: RentalReqFragment.OnSendReqListener): RentalReqFragment {
            val fragment = RentalReqFragment()
            fragment.onSendReqListener = listener
            return fragment
        }
    }
}