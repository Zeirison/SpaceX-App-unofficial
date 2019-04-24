package romansytnyk.spacex.ui.launches.details

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_launch_details.*
import romansytnyk.spacex.R
import romansytnyk.spacex.data.api.model.Launch
import romansytnyk.spacex.ui.base.BaseActivity
import romansytnyk.spacex.util.formatLaunchDateToNativeTimezone
import romansytnyk.spacex.util.formatLaunchDateToUTC
import romansytnyk.spacex.util.formatLaunchDateToUserTimezone

class LaunchDetailsActivity : BaseActivity() {
    private lateinit var launchData: Launch

    companion object {
        private const val EXTRA_LAUNCH = "launch"

        fun start(context: Context?, launch: Launch) {
            val intent = Intent(context, LaunchDetailsActivity::class.java)
            intent.putExtra(EXTRA_LAUNCH, launch)
            context?.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_details)
        launchData = intent.getParcelableExtra(EXTRA_LAUNCH)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "${launchData.rocket?.rocketName} (${launchData.rocket?.rocketType})"
        initViews()
    }

    private fun initViews() {
        initBaseInformation()
        initCores()
        initPayloads()
        initLinks()
    }

    @SuppressLint("SetTextI18n")
    private fun initBaseInformation() {
        startPlace.text = launchData.launchSite?.siteNameLong
        time.text =
                (launchData.launchDateLocal ?: "").formatLaunchDateToNativeTimezone() + "\n" +
                (launchData.launchDateUtc ?: "").formatLaunchDateToUTC() + "\n" +
                (launchData.launchDateUtc ?: "").formatLaunchDateToUserTimezone()

        launchData.details?.let {
            description.visibility = View.VISIBLE
            description.text = it
        }

        launchData.launchSuccess?.let {
            launchResult.visibility = View.VISIBLE
            if (it) {
                launchResult.text = getText(R.string.launch_success)
                launchResult.setTextColor(
                        ContextCompat.getColor(this, R.color.colorSuccess))
            } else {
                launchResult.text = getText(R.string.launch_fail)
                launchResult.setTextColor(
                        ContextCompat.getColor(this, R.color.colorFail))
            }
        }
    }

    private fun initLinks() {
        launchData.links?.missionPatch?.let {
            if (it.isNotEmpty()) {
                image.visibility = View.VISIBLE
                Glide.with(this)
                        .load(it)
                        .into(image)
            }
        }

        launchData.links?.videoLink?.let {
            youtubeLink.visibility = View.VISIBLE
            youtubeLink.setOnClickListener {
                openLink(launchData.links?.videoLink)
            }
        }

        launchData.links?.articleLink?.let {
            articleLink.visibility = View.VISIBLE
            articleLink.setOnClickListener {
                openLink(launchData.links?.articleLink)
            }
        }

        launchData.links?.redditLink?.let {
            redditLink.visibility = View.VISIBLE
            redditLink.setOnClickListener {
                openLink(launchData.links?.redditLink)
            }
        }
    }

    private fun initCores() {
        val coreList = launchData.rocket?.firstStage?.cores
        coreList?.let {
            firstStage.visibility = View.VISIBLE
            cores.visibility = View.VISIBLE

            var str = ""
            for (core in coreList) {
                if (core?.coreSerial == null) continue

                str += String.format(getString(R.string.launch_core_info),
                        core.coreSerial,
                        core.flight,
                        if (core.reused == true) getString(R.string.launch_reused_core) else getString(R.string.launch_new_core))
            }
            cores.text = str
                    .replace("null", getString(R.string.launch_unknown))
                    .trim()
        }

        if (cores.text.isEmpty()) {
            firstStage.visibility = View.GONE
            cores.visibility = View.GONE
        }
    }

    private fun initPayloads() {
        val payloadList = launchData.rocket?.secondStage?.payloads
        payloadList?.let {
            secondStage.visibility = View.VISIBLE
            payload.visibility = View.VISIBLE

            var str = ""
            for (load in payloadList) {
                if (load?.payloadId == null) continue

                // Make customers list
                var customers = ""
                load.customers?.let {
                    for ((index, value) in load.customers.withIndex()) {
                        customers += value
                        if (index != load.customers.size - 1) customers += ", "
                    }
                }

                str += String.format(getString(R.string.launch_payload_info),
                        load.payloadId,
                        load.payloadType,
                        load.payloadMassKg?.toInt(),
                        load.orbit,
                        customers)
            }
            payload.text = str
                    .replace("null", getString(R.string.launch_unknown))
                    .trim()
        }

        if (payload.text.isEmpty()) {
            secondStage.visibility = View.GONE
            payload.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == android.R.id.home) {
            onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openLink(url: String?) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(webIntent)
    }
}
