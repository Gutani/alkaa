package com.escodro.category.presentation.list

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.escodro.category.R
import com.escodro.category.model.Category
import com.escodro.core.extension.dialog
import com.escodro.core.extension.negativeButton
import com.escodro.core.extension.positiveButton
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_category_list.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * [Fragment] responsible to show all [com.escodro.domain.viewdata.ViewData.Category].
 */
internal class CategoryListFragment : Fragment() {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val viewModel: CategoryListViewModel by viewModel()

    private var navigator: NavController? = null

    private var isLandscape: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView()")

        return inflater.inflate(R.layout.fragment_category_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated()")

        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        updateRecyclerView()
        viewModel.loadCategories(onListLoaded = ::updateList)
        navigator = NavHostFragment.findNavController(this)

        button_categorylist_add?.setOnClickListener { addCategory() }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Timber.d("onConfigurationChanged()")

        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        Timber.d("updateRecyclerView()")

        recyclerview_categorylist_list?.adapter = adapter
        recyclerview_categorylist_list?.layoutManager = getLayoutManager()
    }

    private fun getLayoutManager(): RecyclerView.LayoutManager {
        val layoutSpan = if (isLandscape) LAYOUT_SPAN_LANDSCAPE else LAYOUT_SPAN_PORTRAIT
        return GridLayoutManager(context, layoutSpan)
    }

    private fun updateList(list: List<Category>) {
        Timber.d("updateList() - Size = ${list.size}")

        if (list.isEmpty()) {
            recyclerview_categorylist_list?.visibility = View.INVISIBLE
            textview_categorylist_empty?.visibility = View.VISIBLE
        } else {
            recyclerview_categorylist_list?.visibility = View.VISIBLE
            textview_categorylist_empty?.visibility = View.INVISIBLE
        }

        val items = list.map { CategoryListItem(it, ::onOptionMenuClicked) }
        adapter.update(items)
    }

    private fun onOptionMenuClicked(view: View, category: Category) {
        Timber.d("onOptionMenuClicked() - clicked = ${category.name}")

        val popupMenu = context?.let { PopupMenu(it, view) }
        val inflater = popupMenu?.menuInflater
        inflater?.inflate(R.menu.category_menu, popupMenu.menu)
        popupMenu?.setOnMenuItemClickListener(onMenuItemClicked(category))
        popupMenu?.show()
    }

    private fun onMenuItemClicked(category: Category) =
        PopupMenu.OnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.key_action_remove_category -> confirmRemoval(category)
                R.id.key_action_edit_category -> editCategory(category)
            }
            true
        }

    private fun confirmRemoval(category: Category) {
        val description = getString(R.string.category_list_dialog_remove_description, category.name)

        dialog(R.string.category_list_dialog_remove_title, description) {
            positiveButton(R.string.category_list_dialog_remove_positive) { removeCategory(category) }
            negativeButton(R.string.category_list_dialog_remove_negative) { /* Do nothing */ }
        }?.show()
    }

    private fun addCategory() {
        val action = CategoryListFragmentDirections.actionAddCategory()
        navigator?.navigate(action)
    }

    private fun editCategory(category: Category) {
        val action = CategoryListFragmentDirections.actionEditCategory(category.id)
        navigator?.navigate(action)
    }

    private fun removeCategory(category: Category) {
        viewModel.deleteCategory(category)
    }

    companion object {

        private const val LAYOUT_SPAN_PORTRAIT = 2

        private const val LAYOUT_SPAN_LANDSCAPE = 3
    }
}
