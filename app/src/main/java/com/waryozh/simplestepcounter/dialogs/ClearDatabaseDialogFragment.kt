package com.waryozh.simplestepcounter.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.waryozh.simplestepcounter.R

class ClearDatabaseDialogFragment : DialogFragment() {
    private lateinit var listener: ClearDatabaseDialogListener

    // The activity that creates an instance of this dialog fragment must
    // implement this interface in order to receive event callbacks.
    interface ClearDatabaseDialogListener {
        fun onClearDatabaseDialogPositiveClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            listener = context as ClearDatabaseDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement ClearDatabaseDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity!!.let {
            AlertDialog.Builder(it, 0)
                .setTitle(R.string.clear_db_question)
                .setPositiveButton(R.string.clear) { _, _ ->
                    listener.onClearDatabaseDialogPositiveClick()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    dismiss()
                }
                .create()
        }
    }
}
