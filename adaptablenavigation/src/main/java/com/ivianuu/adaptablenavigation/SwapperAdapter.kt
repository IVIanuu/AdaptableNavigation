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

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.os.Parcelable
import android.view.ViewGroup

/**
 * Base adapter for a [ViewSwapper]
 */
abstract class SwapperAdapter {

    private val observable = DataSetObservable()
    private var viewSwapperObserver: DataSetObserver? = null

    abstract fun startUpdate(container: ViewGroup)

    abstract fun instantiateItem(container: ViewGroup, position: Int): Any

    open fun clearItem(container: ViewGroup, position: Int, item: Any) {
        destroyItem(container, position, item)
    }

    abstract fun destroyItem(container: ViewGroup, position: Int, item: Any)

    abstract fun finishUpdate(container: ViewGroup)

    abstract fun getCount(): Int

    open fun saveState(): Parcelable? {
        return null
    }

    open fun restoreState(state: Parcelable) {}

    open fun getItemPosition(item: Any): Int {
        return POSITION_UNCHANGED
    }

    open fun notifyDataSetChanged() {
        synchronized(this) {
            viewSwapperObserver?.onChanged()
        }
        observable.notifyChanged()
    }

    open fun registerDataSetObserver(observer: DataSetObserver) {
        observable.registerObserver(observer)
    }

    open fun unregisterDataSetObserver(observer: DataSetObserver) {
        observable.unregisterObserver(observer)
    }

    open fun setViewSwapperObserver(observer: DataSetObserver?) {
        synchronized(this) {
            viewSwapperObserver = observer
        }
    }

    companion object {
        const val POSITION_UNCHANGED = -1
        const val POSITION_NONE = -2
    }

}