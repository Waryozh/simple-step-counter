package com.waryozh.simplestepcounter.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.waryozh.simplestepcounter.R

class SetStepLengthDialogFragment : DialogFragment() {
    companion object {
        const val STEP_LENGTH_FOR_DIALOG = "STEP_LENGTH_FOR_DIALOG"
    }

    private lateinit var listener: SetStepLengthDialogListener

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    interface SetStepLengthDialogListener {
        fun onSetStepLengthDialogPositiveClick(length: Int)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            listener = context as SetStepLengthDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() +
                        " must implement SetStepLengthDialogListener")
            )
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity!!.let {
            val view = requireActivity().layoutInflater.inflate(R.layout.dialog_set_step_length, null)
            val editStepLength = view.findViewById<EditText>(R.id.et_step_length)
            val stepLength = arguments?.getInt(STEP_LENGTH_FOR_DIALOG, 0) ?: 0
            editStepLength.setText(stepLength.toString())

            val builder = AlertDialog.Builder(it, android.R.style.Theme_Material_Dialog_Alert)
                // Pass null as the parent view because it's going in the dialog layout
                .setView(view)
                .setTitle(R.string.set_step_length_title)
                .setPositiveButton(R.string.ok) { _, _ -> Unit }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    dismiss()
                }

            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    try {
                        val newStepLen = editStepLength.text.toString().toInt()
                        if (newStepLen in 1..200) {
                            listener.onSetStepLengthDialogPositiveClick(newStepLen)
                            dismiss()
                        } else {
                            editStepLength.error = getString(R.string.invalid_step_length)
                        }
                    } catch (error: NumberFormatException) {
                        editStepLength.error = getString(R.string.invalid_step_length)
                    }
                }
            }

            return dialog
        }
    }
}
