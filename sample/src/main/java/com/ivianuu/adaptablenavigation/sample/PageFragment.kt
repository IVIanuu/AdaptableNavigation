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

package com.ivianuu.adaptablenavigation.sample

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivianuu.adaptablenavigation.supportfragments.FragmentSwapperAdapter
import kotlinx.android.synthetic.main.fragment_page.*

private fun Any.d(m: () -> String) {
    Log.d(this::class.java.simpleName, m())
}

class PageAdapter(fm: FragmentManager) : FragmentSwapperAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> FragmentOne()
            1 -> FragmentTwo()
            2 -> FragmentThree()
            else -> throw IllegalArgumentException("unknown position $position")
        }
    }

    override fun getCount() = 3

}

abstract class PageFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        d { "on create" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        page.text = "Page $title"

        detail_button.setOnClickListener {
            activity!!.supportFragmentManager!!.beginTransaction()
                .replace(android.R.id.content, DetailFragment.newInstance(title))
                .addToBackStack("detail_$title")
                .commit()
        }

        d { "on view created" }
    }

    override fun onResume() {
        super.onResume()
        d { "on resume" }
    }

    override fun onPause() {
        super.onPause()
        d { "on pause" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        d { "on destroy view" }
    }

    override fun onDestroy() {
        super.onDestroy()
        d { "on destroy" }
    }

    abstract val title: String
}

class FragmentOne : PageFragment() {
    override val title: String = "One"
}

class FragmentTwo : PageFragment() {
    override val title: String = "Two"
}

class FragmentThree : PageFragment() {
    override val title: String = "Three"
}