package com.example.myapplication.long.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R


class ProgressDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(activity).setTitle("提示")
                .setView(layoutInflater.inflate(R.layout.fragment_progress_dialog,null))
                .create()
        }!!
    }

}