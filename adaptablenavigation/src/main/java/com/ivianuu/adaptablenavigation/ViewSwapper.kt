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

package com.ivianuu.adaptablenavigation

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlinx.android.parcel.Parcelize
import java.util.*

class ViewSwapper @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var adapter: SwapperAdapter? = null
        set(value) {
            // clean up old adapter
            field?.let {
                it.setViewSwapperObserver(null)
                it.startUpdate(this)
                items
                    .filterNotNull()
                    .forEach { item -> it.destroyItem(this, item.position, item.item!!) }
                it.finishUpdate(this)
                items.clear()
            }

            field = value

            if (value != null) {
                for (i in 0 until value.getCount()) {
                    addNewItem(i)
                }

                value.registerDataSetObserver(observer)

                if (currentRestoredItem >= 0) {
                    restoredAdapterState?.let { value.restoreState(it) }
                    showItemInternal(currentRestoredItem)
                    currentRestoredItem = -1
                    restoredAdapterState = null
                }
            }
        }

    var currentItem: Int = 0
        private set

    private val items = ArrayList<ItemInfo?>()
    private val observer = object : DataSetObserver() {
        override fun onChanged() {
            dataSetChanged()
        }

        override fun onInvalidated() {
            dataSetChanged()
        }
    }

    private var currentRestoredItem = -1
    private var restoredAdapterState: Parcelable? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adapter?.let {
            if (currentRestoredItem < 0 && it.getCount() > 0) {
                showItemInternal(0)
            }
        }
    }

    fun showItemAt(position: Int) {
        if (items[position] == null) {
            addNewItem(position)
        }

        val adapter = this.adapter ?: return
        val item = items[currentItem]?.item ?: return

        if (currentItem == position) {
            adapter.clearItem(this, currentItem, item)
        } else if (adapter.getCount() > 0) {
            adapter.destroyItem(this, currentItem, item)
        }

        showItemInternal(position)
    }
    
    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(
            position = currentItem,
            adapterState = adapter?.saveState(),
            superState = super.onSaveInstanceState()
        )
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        val adapter = this.adapter
        if (adapter != null) {
            state.adapterState?.let { adapter.restoreState(it) }
            showItemInternal(if (state.position >= 0) state.position else 0)
        } else {
            restoredAdapterState = state.adapterState
        }

        currentRestoredItem = state.position
    }

    private fun addNewItem(position: Int): ItemInfo {
        val ii = ItemInfo(position = position)
        if (position < 0 || position >= items.size) {
            items.add(ii)
        } else {
            items.add(position, ii)
        }
        return ii
    }

    private fun showItemInternal(position: Int) {
        adapter?.let {
            currentItem = position
            items[position]?.item = it.instantiateItem(this, position)
            it.finishUpdate(this)
        }
    }

    private fun dataSetChanged() {
        val adapter = adapter ?: return
        var needPopulate = items.size < adapter.getCount()
        var newCurrItem = currentItem
        var isUpdating = false
        var i = 0

        while (i < items.size) {
            val ii = items[i] ?: continue
            val pos = ii.position
            val item = ii.item ?: continue

            val newPos = adapter.getItemPosition(item)
            if (newPos == SwapperAdapter.POSITION_UNCHANGED) {
                i++
                continue
            }

            if (newPos == SwapperAdapter.POSITION_NONE) {
                items.removeAt(i)
                i--
                if (!isUpdating) {
                    adapter.startUpdate(this)
                    isUpdating = true
                }
                adapter.destroyItem(this, pos, item)
                needPopulate = true
                if (currentItem == pos) {
                    newCurrItem = Math.max(0, Math.min(currentItem, adapter.getCount() - 1))
                    needPopulate = true
                }
                i++
                continue
            }

            if (pos != newPos) {
                if (pos == currentItem) {
                    newCurrItem = newPos
                }
                ii.position = newPos
                needPopulate = true
            }

            i++
        }

        if (isUpdating) {
            adapter.finishUpdate(this)
        }

        items.sortBy { it?.position }

        if (needPopulate) {
            showItemInternal(newCurrItem)
            requestLayout()
        }
    }

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class SavedState(
        val position: Int,
        val adapterState: Parcelable?,
        val superState: Parcelable?
    ) : Parcelable
    
    data class ItemInfo(var item: Any? = null, var position: Int)
}