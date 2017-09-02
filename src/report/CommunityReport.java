/*
 * Copyright (C) 2016 Michael Dougras da Silva
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package report;

import core.DTNHost;
import core.SimScenario;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import routing.MessageRouter;
import routing.CommunityAndRankRouter;
/**
 * 
 * Report the all the nodes' communities.
 * 
 */
public class CommunityReport extends Report {

	/**
	 * 
	 * Constructor.
	 * 
	 */
	public CommunityReport() {
		init();
	}

	@Override
        public void done() {
		
            List<DTNHost> hosts = SimScenario.getInstance().getHosts();
            List<Set<DTNHost>> communities = new LinkedList<Set<DTNHost>>();
            
            for (DTNHost host : hosts) {
                MessageRouter router = host.getRouter();
		if (router instanceof CommunityAndRankRouter) {
                    CommunityAndRankRouter cr = (CommunityAndRankRouter) router;
                    
                    boolean alreadyHaveCommunity = false;
                    Set<DTNHost> nodeComm = cr.getCommunity();
			
                    // Test to see if another node already reported this community
                    for(Set<DTNHost> c : communities)
                    {
                        if(c.containsAll(nodeComm) && nodeComm.containsAll(c))
                        {
                            alreadyHaveCommunity = true;
			}	
                    }
			
                    if(!alreadyHaveCommunity && nodeComm.size() > 0)
                    {
                        communities.add(nodeComm);
                    } 
                }
            }
            // print each community and its size out to the file
            for(Set<DTNHost> c : communities)
		write("" + c.size() + ' ' + c);
            super.done();
        }
}