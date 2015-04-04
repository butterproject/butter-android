package com.connectsdk.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.connectsdk.service.capability.MediaControl.DurationListener;
import com.connectsdk.service.capability.MediaControl.PositionListener;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.ServiceConfig;
import com.connectsdk.service.config.ServiceDescription;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
@PrepareForTest(GoogleApiClient.class)
public class CastServiceTest {
	
	CastService service;
	
	GoogleApiClient googleApiClient = PowerMockito.mock(GoogleApiClient.class);
	
	RemoteMediaPlayer mediaPlayer = Mockito.mock(RemoteMediaPlayer.class);
	
	class StubCastService extends CastService {
		
		public StubCastService(ServiceDescription serviceDescription,
				ServiceConfig serviceConfig) {
			super(serviceDescription, serviceConfig);
		}

		protected GoogleApiClient createApiClient() {
			return googleApiClient;
		}

	}

	@Before
	public void setUp() {
		service = new StubCastService(mock(ServiceDescription.class), mock(ServiceConfig.class));
		Assert.assertNotNull(service);
	}
	
	@Test
	public void testConnect() {
		// Test desc.: connect creates mApiClient and invokes google api connect
		
		Assert.assertFalse(service.connected);
		Assert.assertNull(service.mApiClient);
		service.connect();
		
		Assert.assertNotNull(service.mApiClient);
		Assert.assertSame(googleApiClient, service.mApiClient);
		verify(googleApiClient).connect();
	}
	
	@Test
	public void testConnectOnlyOnce() {
		// Test desc.: if service is already connected it shouldn't invoke connect
		
		Assert.assertNull(service.mApiClient);
		service.connect();
		Assert.assertNotNull(service.mApiClient);
		service.connect();
		
		verify(googleApiClient, Mockito.times(1)).connect();
	}
	
	@Test
	public void testDisconnect() {
		// Test desc.: disconnect invokes google api disconnect
		
		service.connect();
		Assert.assertNotNull(service.mApiClient);
		
		service.connected = true;
		Mockito.when(googleApiClient.isConnected()).thenReturn(true);
		
		service.disconnect();
		Assert.assertNull(service.mApiClient);
		
		verify(googleApiClient).disconnect();
	}
	
	@Test
	public void testDisconnectShouldBeInvokedWhenConnected() {
		// Test desc.: if service is not connected disconnect do nothing
		
		Assert.assertNull(service.mApiClient);
		service.disconnect();
		Assert.assertNull(service.mApiClient);
		
		verify(googleApiClient, Mockito.times(0)).disconnect();
	}
	
	@Test
	public void testPlay() {
		// Test desc.: should invoke player play
		
		service.mMediaPlayer = mediaPlayer;
		Mockito.when(googleApiClient.isConnected()).thenReturn(true);
		service.mApiClient = googleApiClient;
		ResponseListener<Object> listener = mock(ResponseListener.class);
		service.play(listener);
		
		verify(mediaPlayer).play(googleApiClient);
	}
	
	@Test
	public void testPause() {
		// Test desc.: should invoke player pause

		service.mMediaPlayer = mediaPlayer;
		Mockito.when(googleApiClient.isConnected()).thenReturn(true);
		service.mApiClient = googleApiClient;
		ResponseListener<Object> listener = mock(ResponseListener.class);
		service.pause(listener);
		
		verify(mediaPlayer).pause(googleApiClient);
	}
	
	@Test
	public void testStop() {
		// Test desc.: should invoke player stop

		service.mMediaPlayer = mediaPlayer;
		Mockito.when(googleApiClient.isConnected()).thenReturn(true);
		service.mApiClient = googleApiClient;
		ResponseListener<Object> listener = mock(ResponseListener.class);
		service.stop(listener);
		
		verify(mediaPlayer).stop(googleApiClient);
	}
	
	@Test
	public void testRewindNotImplemented() {
		// Test desc.: rewind should invoke error - "not supported"
		
		ResponseListener<Object> listener = mock(ResponseListener.class);
		service.rewind(listener);
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		verify(listener).onError(Mockito.any(ServiceCommandError.class));
	}
	

	@Test
	public void testFastForwardNotImplemented() {
		// Test desc.: fastForward should invoke error - "not supported"
		
		ResponseListener<Object> listener = mock(ResponseListener.class);
		service.fastForward(listener);
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		verify(listener).onError(Mockito.any(ServiceCommandError.class));
	}
	

	@Test
	public void testPreviousNotImplemented() {
		// Test desc.: previous should invoke error - "not supported"
		
		ResponseListener<Object> listener = mock(ResponseListener.class);
		service.previous(listener);
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		verify(listener).onError(Mockito.any(ServiceCommandError.class));
	}
	

	@Test
	public void testNextNotImplemented() {
		// Test desc.: next should invoke error - "not supported"
		
		ResponseListener<Object> listener = mock(ResponseListener.class);
		service.next(listener);
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		verify(listener).onError(Mockito.any(ServiceCommandError.class));
	}
	
	@Test
	public void testSeek() {
		// Test desc.: only if googleApi is connected and media player state is not null should invoke seek method
		
		// given
		long position = 10;
		ResponseListener<Object> listener = mock(ResponseListener.class);
		when(mediaPlayer.getMediaStatus()).thenReturn(mock(MediaStatus.class));
		service.mMediaPlayer = mediaPlayer;
		when(googleApiClient.isConnected()).thenReturn(true);
		service.mApiClient = googleApiClient;
		when(mediaPlayer.seek(googleApiClient, position, RemoteMediaPlayer.RESUME_STATE_UNCHANGED))
		.thenReturn(mock(PendingResult.class));
		
		// when
		service.seek(position, listener);
		
		// then
		verify(mediaPlayer).seek(googleApiClient, position, RemoteMediaPlayer.RESUME_STATE_UNCHANGED);
	}
	

	@Test
	public void testSeekWithEmptyMediaState() {
		
		long position = 10;
		ResponseListener<Object> listener = mock(ResponseListener.class);
		when(mediaPlayer.getMediaStatus()).thenReturn(null);
		service.mMediaPlayer = mediaPlayer;
		service.seek(position, listener);
		
		ArgumentCaptor<ServiceCommandError> errorArgument = ArgumentCaptor.forClass(ServiceCommandError.class);
		verify(mediaPlayer, times(0)).seek(googleApiClient, position, RemoteMediaPlayer.RESUME_STATE_UNCHANGED);
		verify(listener).onError(errorArgument.capture());
		
		Assert.assertEquals("There is no media currently available", errorArgument.getValue().getMessage());
	}
	
	@Test
	public void testGetDuration() {
		// Test desc.: should call getStreamDuration method and onSuccess
		
		// given
		when(mediaPlayer.getMediaStatus()).thenReturn(mock(MediaStatus.class));
		service.mMediaPlayer = mediaPlayer;
		DurationListener listener = mock(DurationListener.class);
		
		service.getDuration(listener);
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		
		verify(mediaPlayer).getStreamDuration();
		verify(listener).onSuccess(Mockito.any(Long.class));
	}
	
	@Test
	public void testGetPosition() {
		// Test desc.: should call getApproximateStreamPosition method and onSuccess
		
		// given
		when(mediaPlayer.getMediaStatus()).thenReturn(mock(MediaStatus.class));
		service.mMediaPlayer = mediaPlayer;
		PositionListener listener = mock(PositionListener.class);
		
		service.getPosition(listener);
		Robolectric.runUiThreadTasksIncludingDelayedTasks();
		
		verify(mediaPlayer).getApproximateStreamPosition();
		verify(listener).onSuccess(Mockito.any(Long.class));
	}
	
	@Test
	public void testGetMediaPlayer() {
		Assert.assertSame(service, service.getMediaPlayer());
	}
}
