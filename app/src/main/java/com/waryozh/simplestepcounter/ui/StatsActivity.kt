package com.waryozh.simplestepcounter.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.waryozh.simplestepcounter.App
import com.waryozh.simplestepcounter.R
import com.waryozh.simplestepcounter.adapters.WalkDayAdapter
import com.waryozh.simplestepcounter.databinding.ActivityStatsBinding
import com.waryozh.simplestepcounter.dialogs.ClearDatabaseDialogFragment
import com.waryozh.simplestepcounter.injection.StatsActivityComponent
import com.waryozh.simplestepcounter.viewmodels.StatsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import javax.inject.Inject

private const val CLEAR_DATABASE_DIALOG_TAG = "CLEAR_DATABASE_DIALOG_TAG"

class StatsActivity : AppCompatActivity(), ClearDatabaseDialogFragment.ClearDatabaseDialogListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: StatsViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(StatsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as App).appComponent
            .plus(StatsActivityComponent.Module())
            .inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.statistics_activity_toolbar)

        val binding: ActivityStatsBinding = DataBindingUtil.setContentView(this, R.layout.activity_stats)
        binding.statsViewModel = viewModel
        binding.lifecycleOwner = this

        val daysAdapter = WalkDayAdapter()

        // Add an observer that will scroll to the beginning of the list after new data is inserted.
        // New items are always prepended to the list, so this behaviour is sufficient.
        daysAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                rv_walkday_list.scrollToPosition(0)
            }
        })

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        with(rv_walkday_list) {
            adapter = daysAdapter
            addItemDecoration(divider)
        }

        viewModel.walkDays.observe(this, Observer {
            daysAdapter.submitList(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_stats, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.action_clear_database -> {
            ClearDatabaseDialogFragment().show(
                supportFragmentManager,
                CLEAR_DATABASE_DIALOG_TAG
            )
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onClearDatabaseDialogPositiveClick() {
        viewModel.clearDatabase()
    }
}
