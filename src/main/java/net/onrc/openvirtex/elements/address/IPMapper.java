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
package net.onrc.openvirtex.elements.address;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.Wildcards.Flag;
import org.openflow.protocol.action.OFAction;

import net.onrc.openvirtex.elements.Mappable;
import net.onrc.openvirtex.elements.OVXMap;
import net.onrc.openvirtex.elements.datapath.OVXSwitch;
import net.onrc.openvirtex.elements.datapath.PhysicalSwitch;
import net.onrc.openvirtex.exceptions.IndexOutOfBoundException;
import net.onrc.openvirtex.exceptions.AddressMappingException;
import net.onrc.openvirtex.exceptions.NetworkMappingException;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerDestination;
import net.onrc.openvirtex.messages.actions.OVXActionNetworkLayerSource;
import net.onrc.openvirtex.exceptions.SwitchMappingException;
import net.onrc.openvirtex.messages.actions.OVXActionDecap;
import net.onrc.openvirtex.messages.actions.OVXActionEncap;

/**
 * Utility class for IP mapping operations. Implements methods
 * rewrite or add actions for IP translation.
 */
public final class IPMapper {
    private static Logger log = LogManager.getLogger(IPMapper.class.getName());

    /**
     * Overrides default constructor to no-op private constructor.
     * Required by checkstyle.
     */
    private IPMapper() {
    }

    //kllaf
    public static Integer getPhysicalIp(Integer tenantId, Integer virtualIP, OVXSwitch sw) {
        final Mappable map = OVXMap.getInstance();
        final OVXIPAddress vip = new OVXIPAddress(tenantId, virtualIP);
        List<PhysicalSwitch> phySwList;
        PhysicalIPAddress pip;
        
        try {
        	if (map.hasPhysicalIP(vip, tenantId)) {
        		pip = map.getPhysicalIP(vip, tenantId);
        		return pip.getIp();
        	} else {
        		try {
        			//todo : big switch
        			phySwList = map.getPhysicalSwitches(sw);
        	        PhysicalSwitch psw1 = phySwList.get(0);
        	        
        	        if(psw1.isEmptyIpList()) {
        	        	//0000 - 0000
        	        	PhysicalIPAddress pip0 = new PhysicalIPAddress(0);
        	        	OVXIPAddress vip0 = new OVXIPAddress(tenantId, 0);
        	        	map.addIP(pip0, vip0);
        	        	
        	        	pip = new PhysicalIPAddress(psw1.addIp());
        	        } else {
        	        	//todo : flow별로 다른 ip 할당 
        	        	pip = new PhysicalIPAddress(psw1.getIp(0));
        	        }
        	        map.addIP(pip, vip);
        	        System.out.println("kllaf : " + pip.toString() + vip.toString());
        	        return pip.getIp();
        	        
        		} catch (SwitchMappingException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        	}
        } catch (AddressMappingException e) {
            log.error("Inconsistency in Physical-Virtual mapping : {}", e);
        }
		return 0;
    }
    
    public static Integer getPhysicalIp(Integer tenantId, Integer virtualIP) {
        final Mappable map = OVXMap.getInstance();
        final OVXIPAddress vip = new OVXIPAddress(tenantId, virtualIP);
        try {
            PhysicalIPAddress pip;
            if (map.hasPhysicalIP(vip, tenantId)) {
                pip = map.getPhysicalIP(vip, tenantId);
            } else {
                pip = new PhysicalIPAddress(map.getVirtualNetwork(tenantId)
                        .nextIP());
                log.debug("Adding IP mapping {} -> {} for tenant {}", vip, pip,
                        tenantId);
                map.addIP(pip, vip);
            }
            return pip.getIp();
        } catch (IndexOutOfBoundException e) {
            log.error(
                    "No available physical IPs for virtual ip {} in tenant {}",
                    vip, tenantId);
        } catch (NetworkMappingException e) {
            log.error(e);
        } catch (AddressMappingException e) {
            log.error("Inconsistency in Physical-Virtual mapping : {}", e);
        }
        return 0;
    }

    //kllaf
    public static void rewriteMatch(final Integer tenantId, final OFMatch match, OVXSwitch sw) {
        match.setNetworkSource(getPhysicalIp(tenantId, match.getNetworkSource(), sw));
        match.setNetworkDestination(getPhysicalIp(tenantId, match.getNetworkDestination(), sw));
    }
    
    //kllaf
    public static OFAction prependEncapAction(final OVXSwitch sw, final OFMatch match, Integer tenantID) {
    	final OVXActionEncap encapAct = new OVXActionEncap();
    	
    	encapAct.setDataLayerSrcAddress(match.getDataLayerSource());
    	encapAct.setDataLayerDstAddress(match.getDataLayerDestination());
    	
    	encapAct.setNetworkSrcAddress(IPMapper.getPhysicalIp(sw.getTenantId(),
            		match.getNetworkSource(), sw));
    	encapAct.setNetworkDstAddress(IPMapper.getPhysicalIp(sw.getTenantId(),
        		match.getNetworkDestination(), sw));
    	
    	encapAct.setTransportSrcPort((short)(tenantID >> 16));
    	encapAct.setTransportDstPort((short)(tenantID & 0xFFFF));
    	
    	return encapAct;
    }
    //kllaf
    public static OFAction prependDecapAction() {
    	final OVXActionDecap encapAct = new OVXActionDecap();
    	return encapAct;
    }
    
    
    
    
    
    public static void rewriteMatch(final Integer tenantId, final OFMatch match) {
        match.setNetworkSource(getPhysicalIp(tenantId, match.getNetworkSource()));
        match.setNetworkDestination(getPhysicalIp(tenantId,
                match.getNetworkDestination()));
    }

    public static List<OFAction> prependRewriteActions(final Integer tenantId,
            final OFMatch match) {
        final List<OFAction> actions = new LinkedList<OFAction>();
        if (!match.getWildcardObj().isWildcarded(Flag.NW_SRC)) {
            final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource();
            srcAct.setNetworkAddress(getPhysicalIp(tenantId,
                    match.getNetworkSource()));
            actions.add(srcAct);
        }
        if (!match.getWildcardObj().isWildcarded(Flag.NW_DST)) {
            final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination();
            dstAct.setNetworkAddress(getPhysicalIp(tenantId,
                    match.getNetworkDestination()));
            actions.add(dstAct);
        }
        return actions;
    }

    public static List<OFAction> prependUnRewriteActions(final OFMatch match) {
        final List<OFAction> actions = new LinkedList<OFAction>();
        if (!match.getWildcardObj().isWildcarded(Flag.NW_SRC)) {
            final OVXActionNetworkLayerSource srcAct = new OVXActionNetworkLayerSource();
            srcAct.setNetworkAddress(match.getNetworkSource());
            actions.add(srcAct);
        }
        if (!match.getWildcardObj().isWildcarded(Flag.NW_DST)) {
            final OVXActionNetworkLayerDestination dstAct = new OVXActionNetworkLayerDestination();
            dstAct.setNetworkAddress(match.getNetworkDestination());
            actions.add(dstAct);
        }
        return actions;
    }
}
