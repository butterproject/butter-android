/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.provider.base.module

import butter.droid.provider.base.filter.Genre
import butter.droid.provider.base.module.Media
import org.parceler.Parcel
import org.parceler.ParcelConstructor

@org.parceler.Parcel(org.parceler.Parcel.Serialization.BEAN)
data class Season @org.parceler.ParcelConstructor constructor(override val id: String, override val title: String, override val year: Int,
                                                              override val genres: Array<butter.droid.provider.base.filter.Genre>, override val rating: Float, override val poster: String?,
                                                              override val backdrop: String, override val synopsis: String) : butter.droid.provider.base.module.Media
