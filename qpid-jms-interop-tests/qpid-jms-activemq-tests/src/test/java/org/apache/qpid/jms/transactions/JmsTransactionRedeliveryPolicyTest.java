/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.qpid.jms.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.qpid.jms.support.AmqpTestSupport;
import org.apache.qpid.jms.support.Wait;
import org.junit.Test;

/**
 * test redelivery policy application in a TX session.
 */
public class JmsTransactionRedeliveryPolicyTest extends AmqpTestSupport {

    @Override
    public String getAmqpConnectionURIOptions() {
        return "jms.redeliveryPolicy.maxRedeliveries=5";
    }

    @Test(timeout = 30000)
    public void testConsumeAndRollbackWithMaxRedeliveries() throws Exception {
        final int MAX_REDELIVERIES = 5;
        final int MSG_COUNT = 5;

        connection = createAmqpConnection();
        connection.start();

        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = session.createQueue(getDestinationName());
        MessageConsumer consumer = session.createConsumer(queue);
        sendMessages(connection, queue, MSG_COUNT);

        final QueueViewMBean queueView = getProxyToQueue(getDestinationName());

        // Consume the message for the first time.
        Message incoming = null;
        for (int i = 0; i < MSG_COUNT; ++i) {
            incoming = consumer.receive(2000);
            assertNotNull(incoming);
            assertFalse(incoming.getJMSRedelivered());
            assertTrue(incoming instanceof TextMessage);
        }
        session.rollback();

        for (int i = 0; i < MAX_REDELIVERIES; ++i) {
            LOG.info("Queue size before consume is: {}", queueView.getQueueSize());
            assertEquals(MSG_COUNT, queueView.getQueueSize());

            for (int j = 0; j < MSG_COUNT; ++j) {
                incoming = consumer.receive(2000);
                assertNotNull(incoming);
                assertTrue(incoming.getJMSRedelivered());
                assertTrue(incoming instanceof TextMessage);
            }

            assertEquals(MSG_COUNT, queueView.getQueueSize());

            session.rollback();
            LOG.info("Queue size after session rollback is: {}", queueView.getQueueSize());
        }

        assertTrue("Message should get DLQ'd", Wait.waitFor(new Wait.Condition() {

            @Override
            public boolean isSatisified() throws Exception {
                return queueView.getQueueSize() == 0;
            }
        }));

        QueueViewMBean dlq = getProxyToQueue("ActiveMQ.DLQ");
        assertEquals(MSG_COUNT, dlq.getQueueSize());

        assertNull(consumer.receive(50));

        session.commit();
    }
}
