package com.waryozh.simplestepcounter.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.waryozh.simplestepcounter.R
import kotlinx.android.synthetic.main.dialog_set_step_length.view.*

class SetStepLengthDialogFragment : DialogFragment() {
    companion object {
        private const val STEP_LENGTH_FOR_DIALOG_CREATION = "STEP_LENGTH_FOR_DIALOG_CREATION"
        private const val STEP_LENGTH_FOR_SAVE_STATE = "STEP_LENGTH_FOR_SAVE_STATE"
        private const val MIN_STEP_LENGTH = 1
        private const val MAX_STEP_LENGTH = 200
        private const val DEFAULT_STEP_LENGTH = 70

        fun newInstance(stepLength: Int) : SetStepLengthDialogFragment {
            val args = Bundle().apply {
                putInt(STEP_LENGTH_FOR_DIALOG_CREATION, stepLength)
            }
            return SetStepLengthDialogFragment().apply {
                arguments = args
            }
        }
    }

    private lateinit var listener: SetStepLengthDialogListener
    private lateinit var pickerStepLength: NumberPicker

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
                (context.toString() + " must implement SetStepLengthDialogListener")
            )
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity!!.let {
            // Pass null as the parent view because it's going in the dialog layout
            val view = requireActivity().layoutInflater.inflate(R.layout.dialog_set_step_length, null)
            val stepLength = savedInstanceState?.getInt(STEP_LENGTH_FOR_SAVE_STATE, 0) ?:
                (arguments?.getInt(STEP_LENGTH_FOR_DIALOG_CREATION, 0) ?: 0)

            pickerStepLength = view.picker_step_length.apply {
                minValue = MIN_STEP_LENGTH
                maxValue = MAX_STEP_LENGTH
                value = if (stepLength in MIN_STEP_LENGTH..MAX_STEP_LENGTH) stepLength else DEFAULT_STEP_LENGTH
                wrapSelectorWheel = false
            }

            return AlertDialog.Builder(it, android.R.style.Theme_Material_Dialog_Alert)
                .setView(view)
                .setTitle(R.string.set_step_length_title)
                .setPositiveButton(R.string.ok) { _, _ ->
                    listener.onSetStepLengthDialogPositiveClick(pickerStepLength.value)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    dismiss()
                }
                .create()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STEP_LENGTH_FOR_SAVE_STATE, pickerStepLength.value)
    }
}
