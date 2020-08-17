package com.escodro.preference

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceFragmentCompat
import com.escodro.core.extension.getVersionName
import com.escodro.core.viewmodel.ToolbarViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber

/**
 * [PreferenceFragmentCompat] containing the application preferences.
 */
internal class PreferenceFragment : PreferenceFragmentCompat() {

    private var navigator: NavController? = null

    private val sharedViewModel: ToolbarViewModel by sharedViewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Timber.d("onCreatePreferences()")

        setPreferencesFromResource(R.xml.preferences, rootKey)
        initPreferences()
    }

    private fun initPreferences() {
        val aboutPref = findPreference(R.string.key_pref_about)
        aboutPref.setOnPreferenceClickListener {
            navigator?.navigate(R.id.key_action_open_about)
            true
        }

        val versionPref = findPreference(R.string.key_pref_version)
        versionPref?.summary = context?.getVersionName()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.updateTitle(getString(R.string.preference_title))
        navigator = NavHostFragment.findNavController(this)
    }

    private fun findPreference(@StringRes resId: Int) =
        findPreference(getString(resId))
}
