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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivianuu.adaptablenavigation.supportdesign.d

/**
 * @author Manuel Wrage (IVIanuu)
 */
class ContainerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        d { "on view created $savedInstanceState" }

        val type = arguments!!.getString("type")

        if (childFragmentManager.findFragmentByTag(type) == null) {
            val fragment = when(type) {
                "bottom_nav" -> BottomBarFragment()
                "drawer" -> DrawerFragment()
                else -> throw IllegalArgumentException("unknown type")
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, type)
                .commitNow()
        }
    }

    companion object {

        fun bottomNav() = ContainerFragment().apply {
            arguments = Bundle().apply {
                putString("type", "bottom_nav")
            }
        }

        fun drawer() = ContainerFragment().apply {
            arguments = Bundle().apply {
                putString("type", "drawer")
            }
        }

    }
}