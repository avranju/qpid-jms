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
package org.apache.qpid.jms.meta;

import org.apache.qpid.jms.util.ToStringSupport;

/**
 * Meta object that contains the JmsConnection identification and configuration
 * options.  Providers can extend this to add Provider specific data as needed.
 */
public final class JmsConnectionInfo implements JmsResource, Comparable<JmsConnectionInfo> {

    public static final long INFINITE = -1;
    public static final long DEFAULT_CONNECT_TIMEOUT = 15000;
    public static final long DEFAULT_CLOSE_TIMEOUT = 15000;
    public static final long DEFAULT_SEND_TIMEOUT = INFINITE;
    public static final long DEFAULT_REQUEST_TIMEOUT = INFINITE;

    private final JmsConnectionId connectionId;
    private String clientId;
    private String username;
    private String password;
    private boolean forceAsyncSend;
    private boolean alwaysSyncSend;
    public long sendTimeout = DEFAULT_SEND_TIMEOUT;
    public long requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    public long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    public long closeTimeout = DEFAULT_CLOSE_TIMEOUT;
    private String queuePrefix = null;
    private String topicPrefix = null;
    private boolean validatePropertyNames = true;

    public JmsConnectionInfo(JmsConnectionId connectionId) {
        if (connectionId == null) {
            throw new IllegalArgumentException("ConnectionId cannot be null");
        }

        this.connectionId = connectionId;
    }

    public JmsConnectionInfo copy() {
        JmsConnectionInfo copy = new JmsConnectionInfo(connectionId);
        copy(copy);
        return copy;
    }

    private void copy(JmsConnectionInfo copy) {
        copy.clientId = clientId;
        copy.username = username;
        copy.password = password;
        copy.forceAsyncSend = forceAsyncSend;
        copy.alwaysSyncSend = alwaysSyncSend;
        copy.sendTimeout = sendTimeout;
        copy.requestTimeout = requestTimeout;
        copy.closeTimeout = closeTimeout;
        copy.queuePrefix = queuePrefix;
        copy.topicPrefix = topicPrefix;
        copy.connectTimeout = connectTimeout;
        copy.validatePropertyNames = validatePropertyNames;
    }

    public boolean isForceAsyncSend() {
        return forceAsyncSend;
    }

    public void setForceAsyncSends(boolean forceAsyncSend) {
        this.forceAsyncSend = forceAsyncSend;
    }

    public boolean isAlwaysSyncSend() {
        return alwaysSyncSend;
    }

    public void setAlwaysSyncSend(boolean alwaysSyncSend) {
        this.alwaysSyncSend = alwaysSyncSend;
    }

    public JmsConnectionId getConnectionId() {
        return connectionId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQueuePrefix() {
        return queuePrefix;
    }

    public void setQueuePrefix(String queuePrefix) {
        this.queuePrefix = queuePrefix;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public boolean isValidatePropertyNames() {
        return validatePropertyNames;
    }

    public void setValidatePropertyNames(boolean validatePropertyNames) {
        this.validatePropertyNames = validatePropertyNames;
    }

    public long getCloseTimeout() {
        return closeTimeout;
    }

    public void setCloseTimeout(long closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(long sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    @Override
    public String toString() {
        return ToStringSupport.toString(this);
    }

    @Override
    public int hashCode() {
        return connectionId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        JmsConnectionInfo other = (JmsConnectionInfo) obj;
        return connectionId.equals(other.connectionId);
    }

    @Override
    public int compareTo(JmsConnectionInfo other) {
        return this.connectionId.compareTo(other.connectionId);
    }

    @Override
    public void visit(JmsResourceVistor vistor) throws Exception {
        vistor.processConnectionInfo(this);
    }
}
