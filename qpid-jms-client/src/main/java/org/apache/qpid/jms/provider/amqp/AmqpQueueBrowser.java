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
package org.apache.qpid.jms.provider.amqp;

import java.io.IOException;

import org.apache.qpid.jms.message.JmsInboundMessageDispatch;
import org.apache.qpid.jms.meta.JmsConsumerInfo;
import org.apache.qpid.jms.util.IOExceptionSupport;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue Browser implementation for AMQP
 */
public class AmqpQueueBrowser extends AmqpConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpQueueBrowser.class);

    /**
     * @param session
     * @param info
     */
    public AmqpQueueBrowser(AmqpSession session, JmsConsumerInfo info) {
        super(session, info);
    }

    /**
     * QueueBrowser will attempt to initiate a pull whenever there are no pending Messages.
     *
     * We need to initiate a drain to see if there are any messages and if the remote sender
     * indicates it is drained then we can send end of browse.  We only do this when there
     * are no pending incoming deliveries and all delivered messages have become settled
     * in order to give the remote a chance to dispatch more messages once all deliveries
     * have been settled.
     *
     * @param timeout
     *        ignored in this context.
     */
    @Override
    public void pull(long timeout) {
        if (!getEndpoint().getDrain() && getEndpoint().current() == null && getEndpoint().getUnsettled() == 0) {
            LOG.trace("QueueBrowser {} will try to drain remote.", getConsumerId());
            getEndpoint().drain(resource.getPrefetchSize());
        } else {
            getEndpoint().setDrain(false);
        }
    }

    @Override
    public void processFlowUpdates(AmqpProvider provider) throws IOException {
        if (getEndpoint().getDrain() && getEndpoint().getCredit() == getEndpoint().getRemoteCredit()) {
            JmsInboundMessageDispatch browseDone = new JmsInboundMessageDispatch(getNextIncomingSequenceNumber());
            browseDone.setConsumerId(getConsumerId());
            try {
                deliver(browseDone);
            } catch (Exception e) {
                throw IOExceptionSupport.create(e);
            }
        } else {
            getEndpoint().setDrain(false);
        }

        super.processFlowUpdates(provider);
    }

    @Override
    public void processDeliveryUpdates(AmqpProvider provider) throws IOException {
        if (getEndpoint().getDrain() && getEndpoint().current() != null) {
            LOG.trace("{} incoming delivery, cancel drain.", getConsumerId());
            getEndpoint().setDrain(false);
        }

        super.processDeliveryUpdates(provider);

        if (getEndpoint().getDrain() && getEndpoint().getCredit() == getEndpoint().getRemoteCredit()) {
            JmsInboundMessageDispatch browseDone = new JmsInboundMessageDispatch(getNextIncomingSequenceNumber());
            browseDone.setConsumerId(getConsumerId());
            try {
                deliver(browseDone);
            } catch (Exception e) {
                throw IOExceptionSupport.create(e);
            }
        } else {
            getEndpoint().setDrain(false);
        }
    }

    @Override
    protected void configureSource(Source source) {
        if (resource.isBrowser()) {
            source.setDistributionMode(COPY);
        }

        super.configureSource(source);
    }

    @Override
    public boolean isBrowser() {
        return true;
    }
}
