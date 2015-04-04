package com.connectsdk.discovery.provider;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;

import com.connectsdk.discovery.DiscoveryProviderListener;
import com.connectsdk.discovery.provider.CastDiscoveryProvider;
import com.connectsdk.service.config.ServiceDescription;
import com.google.android.gms.cast.CastDevice;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
@PrepareForTest({MediaRouter.class})
public class CastDiscoveryProviderTest {
	
	private CastDiscoveryProvider dp;
	
	private MediaRouter mediaRouter = PowerMockito.mock(MediaRouter.class);

	/**
	 * CastDiscoveryProvider with injected MediaRouter object for testing behavior
	 * 
	 * @author oleksii.frolov
	 */
	class StubCastDiscoveryProvider extends CastDiscoveryProvider {

		public StubCastDiscoveryProvider(Context context) {
			super(context);
			
		}
		
		protected MediaRouter createMediaRouter(Context context) {
			return mediaRouter;
		}

	};

	@Before
	public void setUp() {
		dp = new StubCastDiscoveryProvider(Robolectric.application);
		assertNotNull(dp);
	}

	@Test
	public void testStart() throws Exception {
		// TEST DESC.: start method should invoke MediaRouter removeCallback and addCalback for stopping and starting services
		
		// when
		dp.start();
		
		// waiting for timer call
		Thread.sleep(200);
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		
		// then
		verify(mediaRouter).addCallback(any(MediaRouteSelector.class), 
				any(MediaRouter.Callback.class), eq(MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN));
	}
	
	@Test 
	public void testStop() throws Exception {
		// Test desc.: stop should invoke MediaRouter removeCallback
		
		// when
		dp.stop();
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		
		// then
		verify(mediaRouter).removeCallback(any(MediaRouter.Callback.class));
	}

	@Test
	public void testReset() throws Exception {
		// Test desc.: reset method should stop discovering and clear found services
		
		// given
		dp.foundServices.put("service", mock(ServiceDescription.class));
		Assert.assertFalse(dp.foundServices.isEmpty());
		
		// when
		dp.reset();
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		
		// then
		verify(mediaRouter).removeCallback(any(MediaRouter.Callback.class));
		Assert.assertTrue(dp.foundServices.isEmpty());
	}
	
	@Test
	public void testAddListener() {
		// Test desc.: there is no listeners by default, addListener should save listener 
		DiscoveryProviderListener listener = mock(DiscoveryProviderListener.class);
		Assert.assertTrue(dp.serviceListeners.isEmpty());
		
		dp.addListener(listener);
		Assert.assertEquals(1, dp.serviceListeners.size());
	}

	@Test
	public void testRemoveListener() {
		// Test desc.: there is no listeners by default, addListener should save listener 
		DiscoveryProviderListener listener = mock(DiscoveryProviderListener.class);
		Assert.assertTrue(dp.serviceListeners.isEmpty());
		
		dp.serviceListeners.add(listener);
		dp.removeListener(listener);
		Assert.assertTrue(dp.serviceListeners.isEmpty());
	}
	
	
}
