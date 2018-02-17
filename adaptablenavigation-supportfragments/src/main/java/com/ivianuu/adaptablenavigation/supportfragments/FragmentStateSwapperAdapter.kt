/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.adaptablenavigation.supportfragments

import android.annotation.SuppressLint
import android.os.Parcelable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.ViewGroup
import com.ivianuu.adaptablenavigation.SwapperAdapter

/**
 * A [SwapperAdapter] for [Fragment]'s
 * Basically it replicates the behavior of on FragmentStatePagerAdapter
 */
abstract class FragmentStateSwapperAdapter(private val fm: FragmentManager) : SwapperAdapter() {

    private val savedState = ArrayList<Fragment.SavedState?>()
    private val fragments = ArrayList<Fragment?>()

    private var currentTransaction: FragmentTransaction? = null

    override fun startUpdate(container: ViewGroup) {}

    @SuppressLint("CommitTransaction")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (fragments.size > position) {
            val f = fragments[position]
            if (f != null) {
                return f
            }
        }

        if (currentTransaction == null) {
            currentTransaction = fm.beginTransaction()
        }
        
        val fragment = getItem(position)
        
        if (savedState.size > position) {
            val fss = savedState[position]
            if (fss != null) {
                fragment.setInitialSavedState(fss)
            }
        }
        
        while (fragments.size <= position) {
            fragments.add(null)
        }
        
        fragment.setMenuVisibility(true)
        fragment.userVisibleHint = true
        
        fragments[position] = fragment
        currentTransaction!!.add(container.id, fragment)
        return fragment
    }

    @SuppressLint("CommitTransaction")
    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        val fragment = item as Fragment
        if (currentTransaction == null) {
            currentTransaction = fm.beginTransaction()
        }

        while (savedState.size <= position) {
            savedState.add(null)
        }

        savedState[position] = fm.saveFragmentInstanceState(fragment)
        fragments[position] = null

        currentTransaction?.remove(fragment)
    }

    override fun finishUpdate(container: ViewGroup) {
        currentTransaction?.commitNowAllowingStateLoss()
        currentTransaction = null
    }
    
    override fun saveState(): Parcelable? {
        var state: Bundle? = null
        if (savedState.size > 0) {
            state = Bundle()
            val fss = arrayOfNulls<Fragment.SavedState>(savedState.size)
            savedState.toArray(fss)
            state.putParcelableArray("states", fss)
        }

        for (i in 0 until fragments.size) {
            val f = fragments[i]
            if (f != null) {
                if (state == null) {
                    state = Bundle()
                }
                val key = "f" + i
                fm.putFragment(state, key, f)
            }
        }
        return state
    }

    override fun restoreState(state: Parcelable) {
        savedState.clear()
        fragments.clear()

        val bundle = state as Bundle
        val fss = bundle.getParcelableArray("states")
        fss?.indices?.mapTo(savedState) { fss[it] as Fragment.SavedState }

        val keys = bundle.keySet()

        for (key in keys) {
            if (key.startsWith("f")) {
                val index = Integer.parseInt(key.substring(1))
                val f = fm.getFragment(bundle, key)
                if (f != null) {
                    while (fragments.size <= index) {
                        fragments.add(null)
                    }

                    f.setMenuVisibility(true)
                    f.userVisibleHint = true

                    fragments[index] = f
                }
            }
        }
    }

    abstract fun getItem(position: Int): Fragment
}