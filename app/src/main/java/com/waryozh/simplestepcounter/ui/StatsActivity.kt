package com.waryozh.simplestepcounter.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.waryozh.simplestepcounter.App
import com.waryozh.simplestepcounter.R
import com.waryozh.simplestepcounter.adapters.WalkDayAdapter
import com.waryozh.simplestepcounter.databinding.ActivityStatsBinding
import com.waryozh.simplestepcounter.injection.StatsActivityComponent
import com.waryozh.simplestepcounter.viewmodels.StatsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import javax.inject.Inject

class StatsActivity : AppCompatActivity() {

    @Inject
    lateinit var walkViewModelFactory: ViewModelProvider.Factory

    private val viewModel: StatsViewModel by lazy {
        ViewModelProviders.of(this, walkViewModelFactory).get(StatsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        (application as App).appComponent
            .plus(StatsActivityComponent.Module())
            .inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.statistics_activity_toolbar)

        val binding: ActivityStatsBinding = DataBindingUtil.setContentView(this, R.layout.activity_stats)
        binding.statsViewModel = viewModel
        binding.lifecycleOwner = this

        val daysAdapter = WalkDayAdapter()

        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        with(rv_walkday_list) {
            adapter = daysAdapter
            addItemDecoration(divider)
        }

        viewModel.walkDays.observe(this, Observer { daysAdapter.updateData(it) })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
