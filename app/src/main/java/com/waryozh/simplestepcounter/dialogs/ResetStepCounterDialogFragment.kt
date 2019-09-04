package com.waryozh.simplestepcounter.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.waryozh.simplestepcounter.R

class ResetStepCounterDialogFragment : DialogFragment() {
    private lateinit var listener: ResetStepCounterDialogListener

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    interface ResetStepCounterDialogListener {
        fun onResetStepCounterDialogPositiveClick()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            listener = context as ResetStepCounterDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() +
                        " must implement ResetStepCounterDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            AlertDialog.Builder(it, 0)
                .setMessage(R.string.reset_step_counter_question)
                .setPositiveButton(R.string.reset) { _, _ ->
                    listener.onResetStepCounterDialogPositiveClick()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    dismiss()
                }
                .create()
        }
    }
}
