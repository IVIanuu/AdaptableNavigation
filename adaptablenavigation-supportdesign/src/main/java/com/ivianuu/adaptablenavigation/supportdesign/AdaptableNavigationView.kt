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

package com.ivianuu.adaptablenavigation.supportdesign

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.util.AttributeSet
import android.view.MenuItem
import com.ivianuu.adaptablenavigation.ViewSwapper
import kotlinx.android.parcel.Parcelize

// todo needs some work

/**
 * A [NavigationView] which can be used with a [ViewSwapper]
 */
class AdaptableNavigationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NavigationView(context, attrs, defStyleAttr) {

    private var currentViewSwapperSelectedListener: ViewSwapperOnItemSelectedListener? = null
    private var viewChangeListener: OnNavigationItemSelectedListener? = null
    private var selectedPosition = 0

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(selectedPosition, super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        selectedPosition = state.selectedPosition
        menu.getItem(selectedPosition).isChecked = true
    }

    override fun setNavigationItemSelectedListener(listener: OnNavigationItemSelectedListener?) {
        viewChangeListener = listener
    }

    fun setupWithViewSwapper(viewSwapper: ViewSwapper?) {
        if (currentViewSwapperSelectedListener != null) {
            currentViewSwapperSelectedListener = null
        }

        if (viewSwapper != null) {
            currentViewSwapperSelectedListener = ViewSwapperOnItemSelectedListener(viewSwapper)
            super.setNavigationItemSelectedListener(currentViewSwapperSelectedListener)
        }
    }

    private inner class ViewSwapperOnItemSelectedListener(
        val viewSwapper: ViewSwapper
    ) : OnNavigationItemSelectedListener {

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            val handled = viewChangeListener?.onNavigationItemSelected(item) ?: false
            if (handled) return true

            for (i in 0 until menu.size()) {
                if (menu.getItem(i).itemId == item.itemId) {
                    selectedPosition = i
                    viewSwapper.showItemAt(selectedPosition)
                    val parent = parent
                    if (parent is DrawerLayout) {
                        parent.closeDrawers()
                    }
                    break
                }
            }


            return true
        }
    }

    @SuppressLint("ParcelCreator")
    @Parcelize
    private data class SavedState(
        val selectedPosition: Int,
        val superState: Parcelable?
    ) : Parcelable

}