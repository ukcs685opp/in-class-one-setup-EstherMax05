package routing.groupdetection;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a group data.
 */
public class Group {

    // Stores the current members of the group.
    private Map<Integer, GroupMember> members;

    private int startTime;
    private int endTime;


    public Group() {
        this.members = new HashMap<Integer, GroupMember>();
        this.startTime = 0;
        this.endTime = 0;
    }

    public Group(Group prot) {
        // Copies the members of the other group
        this.members = new HashMap<Integer, GroupMember>();
        for(GroupMember member: prot.members.values()) {
            this.members.put(member.getId(), member.replicate());
        }

        this.startTime = prot.startTime;
        this.endTime = prot.endTime;
    }

    public Group replicate() {
        return new Group(this);
    }

    public Map<Integer, GroupMember> getMembers() {
        return members;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
}
