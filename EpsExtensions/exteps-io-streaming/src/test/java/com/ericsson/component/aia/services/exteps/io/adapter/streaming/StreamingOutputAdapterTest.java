/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.streaming;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ericsson.component.aia.services.exteps.io.adapter.streaming.constants.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import com.codahale.metrics.Meter;
import com.ericsson.component.aia.itpf.common.config.Configuration;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.component.aia.mediation.parsers.streamrecord.StreamedRecord;
import com.ericsson.component.aia.services.eps.EpsEngineConstants;
import com.ericsson.component.aia.services.eps.statistics.EpsStatisticsRegister;
import com.ericsson.component.aia.services.exteps.io.adapter.streaming.exceptions.ClientNotConnectedException;

@RunWith(MockitoJUnitRunner.class)
public class StreamingOutputAdapterTest {

    private static final int TIME_BETWEEN_RETRIES_FOR_TEST = 100;

    private static final int NUMBER_OF_CONNECTION_RETRIES = 4;

    private static final byte[] event = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    private final String streamType = "type1";

    private StubbedSocketChannel stubbedSocketChannel;

    @Spy
    private StreamingOutputAdapter spyStreamingOutputAdapter;

    @Mock
    private StreamedRecord mockStreamedRecord;

    @Mock
    private EventHandlerContext mockEventHandlerContext;

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private Socket mockSocket;

    @Mock
    private EpsStatisticsRegister mockEpsStatisticsRegister;

    @Before
    public void setUp() throws IOException {
        System.setProperty(Constants.OUTPUT_IP, "2.2.2.2");
        System.setProperty(Constants.OUTPUT_PORT, "11101");
        System.setProperty(Constants.OUTPUT_IP + "_" + streamType, "3.3.3.3");
        System.setProperty(Constants.OUTPUT_PORT + "_" + streamType, "11102");
        mockDependencies(true);
    }

    @After
    public void teardown() {
        System.getProperties().remove(Constants.OUTPUT_IP);
        System.getProperties().remove(Constants.OUTPUT_PORT);
    }

    private void mockDependencies(final boolean isStatisticsOn) throws IOException {
        mockEventHandlerContextBehaviour();
        mockSocketBehaviour();
        mockStatisticRegisterBehaviour(isStatisticsOn);
        mockStreamRecordBehaviour();
    }

    private void mockEventHandlerContextBehaviour() {
        Whitebox.setInternalState(spyStreamingOutputAdapter, "eventHandlerContext", mockEventHandlerContext);
        doReturn(mockConfiguration).when(mockEventHandlerContext).getEventHandlerConfiguration();
    }

    private void mockSocketBehaviour() throws IOException {
        stubbedSocketChannel = new StubbedSocketChannel(SelectorProvider.provider());
        doReturn(stubbedSocketChannel).when(spyStreamingOutputAdapter).getNewSocket();
        doReturn(TIME_BETWEEN_RETRIES_FOR_TEST).when(spyStreamingOutputAdapter).getTimeBetweenRetries();
        doReturn(NUMBER_OF_CONNECTION_RETRIES).when(spyStreamingOutputAdapter).getConnectionRetries();
        doReturn(new StreamingConfig()).when(spyStreamingOutputAdapter).getOutputAdapterConfig();

    }

    private void mockStatisticRegisterBehaviour(final boolean isStatisticsOn) {
        doReturn(isStatisticsOn).when(mockEpsStatisticsRegister).isStatisticsOn();
        doReturn(new Meter()).when(mockEpsStatisticsRegister).createMeter(Mockito.anyString());
        doReturn(mockEpsStatisticsRegister).when(mockEventHandlerContext)
                .getContextualData(EpsEngineConstants.STATISTICS_REGISTER_CONTEXTUAL_DATA_NAME);
    }

    private void mockStreamRecordBehaviour() {
        doReturn(event).when(mockStreamedRecord).getData();
    }

    @Test
    public void test_understandsURI_true() {
        final StreamingOutputAdapter adapter = new StreamingOutputAdapter();
        assertTrue(adapter.understandsURI("streaming:/"));
    }

    @Test
    public void test_understandsURI_false() {
        final StreamingOutputAdapter adapter = new StreamingOutputAdapter();
        assertFalse(adapter.understandsURI(""));
        assertFalse(adapter.understandsURI(null));
    }

    @Test
    public void test_doInit_connectedClient_statisticsOn() {
        stubbedSocketChannel.setIsSocketConnected(true);
        spyStreamingOutputAdapter.doInit();
        Mockito.verify(spyStreamingOutputAdapter).tryConnectToClient();
        assertEquals(0, spyStreamingOutputAdapter.getEventMeter().getCount());
        assertNotNull(spyStreamingOutputAdapter.getSocketChannel());
    }

    @Test
    public void test_doInit_connectedClient_statisticsOff() {
        stubbedSocketChannel.setIsSocketConnected(true);
        mockStatisticRegisterBehaviour(false);
        spyStreamingOutputAdapter.doInit();
        Mockito.verify(spyStreamingOutputAdapter).tryConnectToClient();
        assertNull(spyStreamingOutputAdapter.getEventMeter());
        assertNotNull(spyStreamingOutputAdapter.getSocketChannel());
    }

    @Test
    public void test_doInit_with_streamType(){
        doReturn(streamType).when(mockConfiguration).getStringProperty(Constants.STREAM_TYPE);
        spyStreamingOutputAdapter.doInit();
        StreamingConfig config = spyStreamingOutputAdapter.getConfig();
        assertEquals("3.3.3.3", config.getHost());
        assertEquals(11102, config.getPort());
    }

    @Test
    public void test_doInit_without_streamType(){
        doReturn(null).when(mockConfiguration).getStringProperty(Constants.STREAM_TYPE);
        spyStreamingOutputAdapter.doInit();
        StreamingConfig config = spyStreamingOutputAdapter.getConfig();
        spyStreamingOutputAdapter.getOutputAdapterConfig();
        assertEquals("2.2.2.2", config.getHost());
        assertEquals(11101, config.getPort());
    }

    @Test
    public void test_onEvent_connectedClient_statisticsOn() {
        stubbedSocketChannel.setIsSocketConnected(true);
        spyStreamingOutputAdapter.doInit();
        spyStreamingOutputAdapter.onEvent(mockStreamedRecord.getData());
        Mockito.verify(spyStreamingOutputAdapter).trySendEvent(Mockito.any(Object.class));
        assertEquals(1, spyStreamingOutputAdapter.getEventMeter().getCount());
    }

    @Test
    public void test_onEvent_connectedClient_statisticsOff() {
        stubbedSocketChannel.setIsSocketConnected(true);
        mockStatisticRegisterBehaviour(false);
        spyStreamingOutputAdapter.doInit();
        spyStreamingOutputAdapter.onEvent(mockStreamedRecord.getData());
        Mockito.verify(spyStreamingOutputAdapter).trySendEvent(Mockito.any(Object.class));
        assertNull(spyStreamingOutputAdapter.getEventMeter());
    }

    @Test
    public void test_onEvent_disconnectedClient() throws IOException {
        stubbedSocketChannel.setIsSocketConnected(false);
        spyStreamingOutputAdapter.doInit();
        Mockito.verify(spyStreamingOutputAdapter).tryConnectToClient();
        spyStreamingOutputAdapter.onEvent("123456".getBytes());
        Mockito.verify(spyStreamingOutputAdapter, times(2)).tryConnectToClient();
        Mockito.verify(spyStreamingOutputAdapter).trySendEvent("123456".getBytes());
        final String receivedString = StandardCharsets.UTF_8.decode(stubbedSocketChannel.getReceivedBuffer()).toString();
        assertEquals("123456",receivedString);
        assertEquals(0, spyStreamingOutputAdapter.getEventMeter().getCount());
    }

    @Test
    public void test_onEvent_loseConnectionWithClient_statisticsOn() throws ClientNotConnectedException, InterruptedException {
        // connect to client on startup
        stubbedSocketChannel.setIsSocketConnected(true);
        spyStreamingOutputAdapter.doInit();
        Mockito.verify(spyStreamingOutputAdapter).tryConnectToClient();
        Mockito.verify(spyStreamingOutputAdapter).connectWithRetries(Mockito.any(Integer.class));

        // lose connection with client
        stubbedSocketChannel.setIsSocketConnected(false);

        // try to reconnect with client before processing event
        spyStreamingOutputAdapter.onEvent(mockStreamedRecord.getData());
        Mockito.verify(spyStreamingOutputAdapter, times(2)).tryConnectToClient();
        Mockito.verify(spyStreamingOutputAdapter, times(6)).connectWithRetries(Mockito.any(Integer.class));
        Mockito.verify(spyStreamingOutputAdapter).trySendEvent(Mockito.any(Object.class));
        assertEquals(0, spyStreamingOutputAdapter.getEventMeter().getCount());

    }

    @Test
    public void test_onEvent_loseConnectionWithClient_regainConnection_statisticsOn() throws ClientNotConnectedException, InterruptedException {
        stubbedSocketChannel.setIsSocketConnected(false);
        // fail to connect to client on startup
        spyStreamingOutputAdapter.doInit();
        Mockito.verify(spyStreamingOutputAdapter).tryConnectToClient();
        Mockito.verify(spyStreamingOutputAdapter, times(5)).connectWithRetries(Mockito.any(Integer.class));

        stubbedSocketChannel.setIsSocketConnected(true);
        // reconnect with client before processing event
        spyStreamingOutputAdapter.onEvent(mockStreamedRecord.getData());
        Mockito.verify(spyStreamingOutputAdapter).tryConnectToClient();
        Mockito.verify(spyStreamingOutputAdapter, times(5)).connectWithRetries(Mockito.any(Integer.class));
        Mockito.verify(spyStreamingOutputAdapter).trySendEvent(Mockito.any(Object.class));
        assertEquals(1, spyStreamingOutputAdapter.getEventMeter().getCount());
    }

    @Test
    public void test_trySendEvent_writableSocket() {
        stubbedSocketChannel.setIsSocketConnected(true);
        spyStreamingOutputAdapter.doInit();
        spyStreamingOutputAdapter.trySendEvent(mockStreamedRecord.getData());
        Mockito.verify(spyStreamingOutputAdapter).trySendEvent(Mockito.any(Object.class));
    }

    @Test
    public void test_trySendEvent_unwritableSocket() {
        stubbedSocketChannel.setIsSocketConnected(false);
        spyStreamingOutputAdapter.doInit();
        spyStreamingOutputAdapter.trySendEvent(mockStreamedRecord.getData());
    }

    @Test
    public void test_destroy_openConnection() {
        stubbedSocketChannel.setIsSocketConnected(true);
        spyStreamingOutputAdapter.doInit();
        spyStreamingOutputAdapter.destroy();
        assertFalse(stubbedSocketChannel.isOpen());
    }

    public class StubbedSocketChannel extends SocketChannel {
        private boolean isSocketConnected;

        private ByteBuffer receivedBuffer;

        protected StubbedSocketChannel(final SelectorProvider provider) {
            super(provider);
        }

        public void setIsSocketConnected(final boolean isSocketConnected) {
            this.isSocketConnected = isSocketConnected;
        }

        @Override
        public SocketAddress getLocalAddress() throws IOException {
            return null;
        }

        @Override
        public <T> T getOption(final SocketOption<T> name) throws IOException {
            return null;
        }

        @Override
        public Set<SocketOption<?>> supportedOptions() {
            return null;
        }

        @Override
        public SocketChannel bind(final SocketAddress local) throws IOException {
            return null;
        }

        @Override
        public <T> SocketChannel setOption(final SocketOption<T> name, final T value) throws IOException {
            return null;
        }

        @Override
        public SocketChannel shutdownInput() throws IOException {
            return null;
        }

        @Override
        public SocketChannel shutdownOutput() throws IOException {
            return null;
        }

        @Override
        public Socket socket() {
            return mockSocket;
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public boolean isConnectionPending() {
            return false;
        }

        @Override
        public boolean connect(final SocketAddress remote) throws IOException {
            return false;
        }

        @Override
        public boolean finishConnect() throws IOException {
            return isSocketConnected;
        }

        @Override
        public SocketAddress getRemoteAddress() throws IOException {
            return null;
        }

        @Override
        public int read(final ByteBuffer dst) throws IOException {
            return 0;
        }

        @Override
        public long read(final ByteBuffer[] dsts, final int offset, final int length) throws IOException {
            return 0;
        }

        @Override
        public int write(final ByteBuffer src) throws IOException {
            receivedBuffer = src.duplicate();
            if (isSocketConnected) {
                while (src.hasRemaining()) {
                    src.get();
                }
                return 0;
            } else {
                throw new IOException("throwing IOException");
            }
        }

        @Override
        public long write(final ByteBuffer[] srcs, final int offset, final int length) throws IOException {
            return 0;
        }

        @Override
        protected void implCloseSelectableChannel() throws IOException {
        }

        @Override
        protected void implConfigureBlocking(final boolean block) throws IOException {
        }

        public ByteBuffer getReceivedBuffer() {
            return receivedBuffer;
        }
    }
}
