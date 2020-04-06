package com.example.coroutines.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coroutines.CoroutinesApp
import com.example.coroutines.R
import com.example.coroutines.domain.MinStarCount
import com.example.coroutines.domain.NoMinStarCount
import com.example.coroutines.domain.RepoOwner
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.user_repos_fragment.*
import javax.inject.Inject

private const val GRID_COLUMN_COUNT = 2
private const val STAR_COUNT_OVER_1_000 = 1_000
private const val STAR_COUNT_OVER_100 = 100

class UserReposFragment : Fragment() {

    private val repoOwnerClickListener = this::onRepoOwnerClicked

    private val recyclerViewAdapter = RepoAdapter(repoOwnerClickListener)

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UserReposViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity().application as CoroutinesApp)
            .appComponent
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.user_repos_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reposRecyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = GridLayoutManager(activity, GRID_COLUMN_COUNT)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.userRepos.observe(viewLifecycleOwner, Observer {
            recyclerViewAdapter.submitList(it)
        })

        viewModel.isError.observe(viewLifecycleOwner, Observer { isError ->
            if (!isError) return@Observer

            Snackbar.make(
                userReposContainer,
                R.string.error_message,
                Snackbar.LENGTH_LONG
            ).show()
        })

        viewModel.showSpinner.observe(viewLifecycleOwner, Observer { isSpinnerVisible ->
            spinner.isVisible = isSpinnerVisible
        })

        viewModel.lookupUserRepos("JakeWharton")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.repos_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.over1000Stars -> {
                viewModel.filterRepos(MinStarCount(STAR_COUNT_OVER_1_000))
                true
            }
            R.id.over100Stars -> {
                viewModel.filterRepos(MinStarCount(STAR_COUNT_OVER_100))
                true
            }
            R.id.allStars -> {
                viewModel.filterRepos(NoMinStarCount)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onRepoOwnerClicked(repoOwner: RepoOwner) {
        val action = UserReposFragmentDirections
            .actionReposToUserDetails(repoOwner.login)
        findNavController().navigate(action)
    }
}
