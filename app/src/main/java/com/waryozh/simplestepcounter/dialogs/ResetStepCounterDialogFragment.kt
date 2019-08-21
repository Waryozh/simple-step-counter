package com.waryozh.simplestepcounter.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.waryozh.simplestepcounter.R

class ResetStepCounterDialogFragment : DialogFragment() {
    private lateinit var listener: ResetStepCounterDialogListener

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    interface ResetStepCounterDialogListener {
        fun onDialogPositiveClick()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            listener = context as ResetStepCounterDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            AlertDialog.Builder(it, android.R.style.Theme_Material_Dialog_Alert)
                .setMessage(R.string.reset_step_counter_question)
                .setPositiveButton(R.string.reset) { _, _ ->
                    listener.onDialogPositiveClick()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    dismiss()
                }
                .create()
        }
    }
}