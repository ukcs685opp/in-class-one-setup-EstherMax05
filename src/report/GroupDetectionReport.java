package report;

import core.DTNHost;
import core.SimClock;
import core.SimScenario;
import core.UpdateListener;
import routing.GroupDetectionRouter;
import routing.MessageRouter;
import routing.community.ReportCommunity;
import routing.groupdetection.Group;

import java.util.List;

/**
 * Created by micdoug on 04/07/17.
 */
public class GroupDetectionReport extends Report implements UpdateListener {
    private int simulationDuration;

    /**
     *
     * Constructor.
     *
     */
    public GroupDetectionReport()  {
        init();
        simulationDuration = (int) Math.floor(SimScenario.getInstance().getEndTime());
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        int curTime = SimClock.getIntTime();

        // If it is the end of the simulation it is time to write
        // groups
        if (curTime % simulationDuration == 0) {
            for (DTNHost host : hosts) {
                MessageRouter router = host.getRouter();
                if (router instanceof GroupDetectionRouter) {
                    GroupDetectionRouter groupRouter = (GroupDetectionRouter)router;
                    groupRouter.getGroupHistory().add(groupRouter.getCurrentGroup());

                    write("" + host.getAddress() + " " + (groupRouter.getGroupHistory().size()+1));
                    for (Group group: groupRouter.getGroupHistory()) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(String.format("{startTime: %d, endTime: %d, members: [ ", group.getStartTime(), group.getEndTime()));
                        for (int mid: group.getMembers().keySet()) {
                            builder.append(mid).append(" ");
                        }
                        builder.append("] }");
                        write(builder.toString());
                    }

                }
            }
        }
    }
}
