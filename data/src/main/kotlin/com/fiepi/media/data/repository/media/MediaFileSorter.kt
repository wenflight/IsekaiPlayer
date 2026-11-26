/*
 * IsekaiPlayer - Sovereign above myriad realms; shatter every mortal cipher.
 * Copyright (C) 2026 onlymash
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.fiepi.media.data.repository.media

import com.fiepi.media.domain.model.media.MediaFile
import com.fiepi.media.domain.model.preferences.MediaOptions
import com.fiepi.media.domain.model.preferences.SortOrder
import com.fiepi.media.domain.model.preferences.SortType

internal object MediaFileSorter {

    fun sort(items: List<MediaFile>, options: MediaOptions): List<MediaFile> {
        val comparator = compareBy<MediaFile> {
            if (options.foldersFirst) {
                if (it is MediaFile.Folder) 0 else 1
            } else {
                0
            }
        }.then { item1, item2 ->
            val result = when (options.sortType) {
                SortType.Name -> compareByName(item1, item2)

                SortType.Date -> {
                    val d1 = item1.lastModified ?: 0L
                    val d2 = item2.lastModified ?: 0L
                    val cmp = d1.compareTo(d2)
                    if (cmp != 0) cmp else compareByName(item1, item2)
                }

                SortType.Size -> {
                    val s1 = if (item1 is MediaFile.Folder) 0L else (item1.size ?: 0L)
                    val s2 = if (item2 is MediaFile.Folder) 0L else (item2.size ?: 0L)
                    val cmp = s1.compareTo(s2)
                    if (cmp != 0) cmp else compareByName(item1, item2)
                }

                SortType.Duration -> {
                    val dur1 = (item1 as? MediaFile.Video)?.duration ?: 0L
                    val dur2 = (item2 as? MediaFile.Video)?.duration ?: 0L
                    val cmp = dur1.compareTo(dur2)
                    if (cmp != 0) cmp else compareByName(item1, item2)
                }
            }

            if (options.sortOrder == SortOrder.Ascending) result else -result
        }

        return items.sortedWith(comparator)
    }

    private fun compareByName(item1: MediaFile, item2: MediaFile): Int {
        return compareNatural(item1.name, item2.name)
    }

    private fun compareNatural(a: String, b: String): Int {
        var i = 0
        var j = 0
        val lenA = a.length
        val lenB = b.length

        while (i < lenA && j < lenB) {
            val ca = a[i]
            val cb = b[j]

            if (ca in '0'..'9' && cb in '0'..'9') {
                // Skip leading zeros for numeric value comparison
                var startA = i
                while (startA < lenA && a[startA] == '0') startA++
                var endA = startA
                while (endA < lenA && a[endA] in '0'..'9') endA++

                var startB = j
                while (startB < lenB && b[startB] == '0') startB++
                var endB = startB
                while (endB < lenB && b[endB] in '0'..'9') endB++

                val sigLenA = endA - startA
                val sigLenB = endB - startB
                if (sigLenA != sigLenB) {
                    return sigLenA.compareTo(sigLenB)
                }

                for (k in 0 until sigLenA) {
                    val da = a[startA + k]
                    val db = b[startB + k]
                    if (da != db) {
                        return da.compareTo(db)
                    }
                }

                i = endA
                j = endB
            } else {
                val la = ca.lowercaseChar()
                val lb = cb.lowercaseChar()
                if (la != lb) {
                    return la.compareTo(lb)
                }
                i++
                j++
            }
        }

        val lenCmp = (lenA - i).compareTo(lenB - j)
        if (lenCmp != 0) return lenCmp

        return a.compareTo(b)
    }
}
