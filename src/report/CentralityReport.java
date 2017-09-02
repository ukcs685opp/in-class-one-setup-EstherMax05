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
import java.util.List;
import routing.MessageRouter;
import routing.BubbleRapRouter;
import routing.DecisionEngineRouter;
import routing.RoutingDecisionEngine;
import routing.decisionengine.DistributedBubbleRap;
//import routing.community.ReportCommunity;

/**
 * Reports the local and global centrality values of the nodes.
 */
public class CentralityReport extends Report {

    /**
     * Constructor.
     */
    public CentralityReport() {
        init();
    }

    @Override
    public void done() {
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        
        for (DTNHost host : hosts) {
            MessageRouter router = host.getRouter();
            if (router instanceof BubbleRapRouter) {
                BubbleRapRouter br = (BubbleRapRouter) router;
                   
                write("" + host.getAddress() + " " 
                            + br.getLocalRank() + " " 
                            + br.getGlobalRank());
                   
            }else if (router instanceof DecisionEngineRouter) {
                RoutingDecisionEngine de = ((DecisionEngineRouter) router).getDecisionEngine();
                DistributedBubbleRap br = (DistributedBubbleRap)de;
                
                write("" + host.getAddress() + " " 
                            + br.getLocalCentrality() + " " 
                            + br.getGlobalCentrality());  
            }
        }
            
        super.done();
    }
            
}