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
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.view.ViewGroup
import com.ivianuu.adaptablenavigation.SwapperAdapter

/**
 * A [SwapperAdapter] for [Fragment]'s
 * Basically it replicates the behavior of on FragmentPagerAdapter
 */
abstract class FragmentSwapperAdapter(private val fm: FragmentManager) : SwapperAdapter() {

    private var currentTransaction: FragmentTransaction? = null

    override fun startUpdate(container: ViewGroup) {
        if (container.id == View.NO_ID) {
            throw IllegalStateException("ViewSwapper with adapter $this requires a view id")
        }
    }

    @SuppressLint("CommitTransaction")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (currentTransaction == null) {
            currentTransaction = fm.beginTransaction()
        }

        val itemId = getItemId(position)

        // Do we already have this fragment?
        val name = makeFragmentName(container.id, itemId)
        var fragment = fm.findFragmentByTag(name)

        if (fragment != null) {
            currentTransaction?.attach(fragment)
        } else {
            fragment = getItem(position)
            currentTransaction?.add(
                container.id, fragment,
                makeFragmentName(container.id, itemId)
            )
        }

        fragment.setMenuVisibility(true)
        fragment.userVisibleHint = true

        return fragment
    }

    @SuppressLint("CommitTransaction")
    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        if (currentTransaction == null) {
            currentTransaction = fm.beginTransaction()
        }

        val fragment = item as Fragment

        fragment.setMenuVisibility(false)
        fragment.userVisibleHint = false

        currentTransaction?.detach(fragment)
    }

    override fun finishUpdate(container: ViewGroup) {
        currentTransaction?.commitNowAllowingStateLoss()
        currentTransaction = null
    }

    abstract fun getItem(position: Int): Fragment

    open fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun makeFragmentName(viewId: Int, id: Long): String {
        return "android:switcher:$viewId:$id"
    }
}