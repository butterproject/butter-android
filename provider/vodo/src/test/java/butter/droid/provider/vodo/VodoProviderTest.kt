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

package butter.droid.provider.vodo

import butter.droid.provider.base.module.Media
import butter.droid.provider.base.module.Movie
import butter.droid.provider.vodo.api.VodoService
import io.reactivex.observers.TestObserver
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import java.lang.NullPointerException

@RunWith(RobolectricTestRunner::class)
class VodoProviderTest {

    private lateinit var behaviorDelegate: BehaviorDelegate<VodoService>

    private lateinit var vodoProvider: VodoProvider;


    @Before
    fun setUp() {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://localhost")
//                .callFactory(RxJava2CallAdapterFactory::create())
//                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val mockRetrofit = MockRetrofit.Builder(retrofit)
                .build()

        behaviorDelegate = mockRetrofit.create(VodoService::class.java)
    }

    @Test
    fun items() {

        vodoProvider = VodoProvider(behaviorDelegate.returningResponse())




        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun details() {
        val media = mock(Movie::class.java)

        val o = TestObserver<Media>()
        vodoProvider.detail(media)
                .subscribe(o)

        o.await()

        o.assertComplete()
        assertSame(media, o.values()[0])
    }

    @Test(expected = NullPointerException::class)
    fun detailsNull() {
        val o = TestObserver<Media>()
        vodoProvider.detail(null)
                .subscribe(o)

        o.await()

        o.assertComplete()
    }
}
