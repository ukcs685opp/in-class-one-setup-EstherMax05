package routing;

import core.Connection;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import routing.groupdetection.Group;
import routing.groupdetection.GroupMember;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of a distributed group detection algorithm.
 * This router actually does not forward messages. It only uses
 * node's contacts to detect groups.
 */
public class GroupDetectionRouter extends ActiveRouter {

    /**
     * Responses threshold used by the algorithm.
     */
    private static final String THRESHOLD_ST = "threshold";

    /**
     * Group destroy coefficient. When the proportion of inactive nodes
     * in the current group is equal or greater this value, the group is
     * considered destroyed.
     */
    private static final String DESTROY_COEFFICIENT_ST = "destroyCoefficient";

    // Defines the interval between scan action in seconds
    //private static int scanInterval = 60;

    // The number of responses a node needs to have to be considered a group member
    private static int responses = 15;

    // The number of no responses a node needs to have to be removed from the beaconsGroup
    private static int noResponses = 15;

    // The number of no responses a group member needs to have to be considered inactive
    private static int groupDestroyNoResponses = GroupDetectionRouter.noResponses;

    private static double groupDestroyCoefficient = 0.7;

    // Stores the current group
    private Group currentGroup;

    // Stores nodes that are in analysis
    private Group beaconGroup;

    // Stores groups detected
    private List<Group> groupHistory;

    public GroupDetectionRouter(Settings set) {
        super(set);

        set.setNameSpace("GroupDetectionRouter");
        if (set.contains(THRESHOLD_ST)) {
            GroupDetectionRouter.responses = set.getInt(THRESHOLD_ST);
            GroupDetectionRouter.noResponses = GroupDetectionRouter.responses;
            GroupDetectionRouter.groupDestroyNoResponses = GroupDetectionRouter.responses;
        }
        if (set.contains(GroupDetectionRouter.DESTROY_COEFFICIENT_ST)) {
            GroupDetectionRouter.groupDestroyCoefficient = set.getDouble(DESTROY_COEFFICIENT_ST);
        }
        set.restoreNameSpace();

        this.currentGroup = new Group();
        this.beaconGroup = new Group();
        this.groupHistory = new ArrayList<Group>();
    }

    /**
     * Copy constructor.
     * @param prot Prototype.
     */
    public GroupDetectionRouter(GroupDetectionRouter prot) {
        super(prot);

        this.currentGroup = prot.currentGroup.replicate();
        this.currentGroup.setStartTime(0);
        this.beaconGroup = prot.beaconGroup.replicate();
        this.groupHistory = new ArrayList<Group>();
        for(Group g: prot.groupHistory) {
            this.groupHistory.add(g.replicate());
        }
    }

    @Override
    public MessageRouter replicate() {
        return new GroupDetectionRouter(this);
    }

    @Override
    public void update() {
        super.update();

        // captures all conected nodes
        Set<Integer> responses = new HashSet<Integer>();
        for (Connection con: this.getConnections()) {
            responses.add(con.getOtherNode(this.getHost()).getAddress());
        }

        // The list of connected nodes are considered as beacon responses

        // 1. Check current group members that aren't available
        Set<Integer> currentGroupNoResponses = new HashSet<Integer>();
        currentGroupNoResponses.addAll(this.currentGroup.getMembers().keySet());
        currentGroupNoResponses.removeAll(responses);
        for(int mid: currentGroupNoResponses) {
            this.currentGroup.getMembers().get(mid).incrementNoResponseCounter();
        }


        // 2. Check current group members that are available
        Set<Integer> currentGroupResponses = new HashSet<Integer>();
        currentGroupResponses.addAll(this.currentGroup.getMembers().keySet());
        currentGroupResponses.retainAll(responses);
        for(int mid: currentGroupResponses) {
            this.currentGroup.getMembers().get(mid).incrementResponseCounter();
        }

        // 3. Check current beacon group members that aren't available
        Set<Integer> beaconGroupNoResponses = new HashSet<Integer>();
        beaconGroupNoResponses.addAll(this.beaconGroup.getMembers().keySet());
        beaconGroupNoResponses.removeAll(responses);
        for(int mid: beaconGroupNoResponses) {
            this.beaconGroup.getMembers().get(mid).incrementNoResponseCounter();
        }

        // 4. Check current beacon group members that are available
        Set<Integer> beaconGroupResponses = new HashSet<Integer>();
        beaconGroupResponses.addAll(this.beaconGroup.getMembers().keySet());
        beaconGroupResponses.retainAll(responses);
        for(int mid: beaconGroupResponses) {
            this.beaconGroup.getMembers().get(mid).incrementResponseCounter();
        }

        // 5. Check discovered devices
        Set<Integer> newResponses = new HashSet<Integer>();
        newResponses.addAll(responses);
        newResponses.removeAll(currentGroupResponses);
        newResponses.removeAll(beaconGroupResponses);
        for(int mid: newResponses) {
            this.beaconGroup.getMembers().put(mid, new GroupMember(mid));
            this.beaconGroup.getMembers().get(mid).incrementResponseCounter();
            this.beaconGroup.getMembers().get(mid).setStatus(GroupMember.Status.Active);
            //System.out.println("Added to beacon list");
        }



        // Analysing the beacon group
        Set<Integer> beaconRemove = new HashSet<Integer>();
        for(GroupMember bmember: this.beaconGroup.getMembers().values()) {
            // after N sequential responses a node is added to the current group
            if (bmember.getResponseCounter() >= GroupDetectionRouter.responses) {
                if (this.currentGroup.getMembers().size() == 0) {
                    this.currentGroup.setStartTime(SimClock.getIntTime());
                }
                this.currentGroup.getMembers().put(bmember.getId(), bmember);
                this.currentGroup.getMembers().get(bmember.getId()).setStatus(GroupMember.Status.Active);
                //System.out.println("Added current group");
            }
            // after F sequential no responses a node is removed from the beacon group
            else if (bmember.getNoResponseCounter() >= GroupDetectionRouter.noResponses) {
                beaconRemove.add(bmember.getId());
                //System.out.println("Removed from beacon");
            }
        }
        for(int r: beaconRemove) {
            this.beaconGroup.getMembers().remove(r);
        }


        // Analysing the current group
        int active = 0;
        int inactive = 0;
        for(GroupMember member: this.currentGroup.getMembers().values()) {
            // after F sequential no responses a node is considered inactive
            if (member.getNoResponseCounter() >= GroupDetectionRouter.groupDestroyNoResponses) {
                member.setStatus(GroupMember.Status.Inactive);
            }
            // Counts the number of active vs inactive members
            if (member.getStatus() == GroupMember.Status.Active) {
                active++;
            }
            else {
                inactive++;
            }
        }

        // if P% of group members are inactive, then the group is finished
        if (this.currentGroup.getMembers().size() > 0 &&  (inactive*1.0/(inactive+active+1)) > GroupDetectionRouter.groupDestroyCoefficient) {
            if (this.currentGroup.getMembers().size() > 1) {
                this.currentGroup.setEndTime(SimClock.getIntTime());
                this.groupHistory.add(this.currentGroup);
            }
            this.currentGroup = new Group();
        }
    }

    public Group getCurrentGroup() {
        return currentGroup;
    }

    public List<Group> getGroupHistory() {
        return groupHistory;
    }
}
