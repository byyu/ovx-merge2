/*******************************************************************************
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 *    Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior
 *    University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

/**
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
package org.openflow.protocol.action;

import java.util.Arrays;

import net.onrc.openvirtex.packet.IPv4;
import net.onrc.openvirtex.util.MACAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFPhysicalPort;

/**
 * Represents an ofp_action_dl_addr
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
//kllaf
public class OFActionEncap extends OFAction {
    public static int MINIMUM_LENGTH = 32;
    
    protected byte[] dataLayerSrcAddress;
    protected byte[] dataLayerDstAddress;
    protected int networkSrcAddress;
    protected int networkDstAddress;
    protected short transportSrcPort;
    protected short transportDstPort;
    
    
    
    public OFActionEncap() {
        super();
        super.setType(OFActionType.ENCAP);
        super.setLength((short) OFActionEncap.MINIMUM_LENGTH);
    }

    public OFActionEncap(final byte[] dataLayerSrcAddress, final byte[] dataLayerDstAddress, final int networkSrcAddress,
    		final int networkDstAddress, final short transportSrcPort, final short transportDstPort) {
        this();
        this.dataLayerSrcAddress = dataLayerSrcAddress;
        this.dataLayerDstAddress = dataLayerDstAddress;
        this.networkSrcAddress = networkSrcAddress;
        this.networkDstAddress = networkDstAddress;
        this.transportSrcPort = transportSrcPort;
        this.transportDstPort = transportDstPort;
    }
    
    
    
    
    public byte[] getDataLayerSrcAddress() {
        return this.dataLayerSrcAddress;
    }
    public byte[] getDataLayerDstAddress() {
        return this.dataLayerDstAddress;
    }
    public int getNetworkSrcAddress() {
        return this.networkSrcAddress;
    }
    public int getNetworkDstAddress() {
        return this.networkDstAddress;
    }
    public short getTransportSrcPort() {
        return this.transportSrcPort;
    }
    public short getTransportDstPort() {
        return this.transportDstPort;
    }

    
    
    
    public void setDataLayerSrcAddress(final byte[] dataLayerSrcAddress) {
        this.dataLayerSrcAddress = dataLayerSrcAddress;
    }
    public void setDataLayerDstAddress(final byte[] dataLayerDstAddress) {
        this.dataLayerDstAddress = dataLayerDstAddress;
    }
    public void setNetworkSrcAddress(final int networkSrcAddress) {
        this.networkSrcAddress = networkSrcAddress;
    }
    public void setNetworkDstAddress(final int networkDstAddress) {
        this.networkDstAddress = networkDstAddress;
    }
    public void setTransportSrcPort(final short transportSrcPort) {
        this.transportSrcPort = transportSrcPort;
    }
    public void setTransportDstPort(final short transportDstPort) {
        this.transportDstPort = transportDstPort;
    }
    
    
    @Override
    public void readFrom(final ChannelBuffer data) {
        super.readFrom(data);
        if (this.dataLayerSrcAddress == null) {
            this.dataLayerSrcAddress = new byte[OFPhysicalPort.OFP_ETH_ALEN];
        }
        if (this.dataLayerDstAddress == null) {
        	this.dataLayerDstAddress = new byte[OFPhysicalPort.OFP_ETH_ALEN];
        }
    	
        data.readBytes(this.dataLayerSrcAddress);
        data.readBytes(this.dataLayerDstAddress);
        this.networkSrcAddress = data.readInt();
        this.networkDstAddress = data.readInt();
        this.transportSrcPort = data.readShort();
        this.transportDstPort = data.readShort();
        data.readInt();
    }

    @Override
    public void writeTo(final ChannelBuffer data) {
        super.writeTo(data);
        data.writeBytes(this.dataLayerSrcAddress);
        data.writeBytes(this.dataLayerDstAddress);
        data.writeInt(this.networkSrcAddress);
        data.writeInt(this.networkDstAddress);
        data.writeShort(this.transportSrcPort);
        data.writeShort(this.transportDstPort);
        data.writeInt(0);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(this.dataLayerSrcAddress);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFActionEncap)) {
            return false;
        }
        final OFActionEncap other = (OFActionEncap) obj;
        if (!Arrays.equals(this.dataLayerSrcAddress, other.dataLayerSrcAddress) ||
        		!Arrays.equals(this.dataLayerDstAddress, other.dataLayerDstAddress) ||
        		this.networkSrcAddress != other.networkSrcAddress ||
        		this.networkDstAddress != other.networkDstAddress ||
        		this.transportSrcPort != other.transportSrcPort ||
        		this.transportDstPort != other.transportDstPort) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.type);
        builder.append("[");
        builder.append(MACAddress.valueOf(this.dataLayerSrcAddress).toString());
        builder.append(MACAddress.valueOf(this.dataLayerDstAddress).toString());
        builder.append("]");
        
        builder.append("[");
        builder.append(IPv4.fromIPv4Address(this.networkSrcAddress));
        builder.append(IPv4.fromIPv4Address(this.networkDstAddress));
        builder.append("]");
        
        builder.append("[");
        builder.append(this.transportSrcPort);
        builder.append(this.transportDstPort);
        builder.append("]");
        
        return builder.toString();
    }
}
