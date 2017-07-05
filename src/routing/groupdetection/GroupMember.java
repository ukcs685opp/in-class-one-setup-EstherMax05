package routing.groupdetection;


public class GroupMember {

    private int id;
    private Status status;
    private int responseCounter;
    private int noResponseCounter;

    /**
     * Constructor.
     * @param id Identifier of the node associated with the group.
     */
    public GroupMember(int id) {
        this.id = id;
        this.status = Status.Inactive;
        this.responseCounter = 0;
    }

    /**
     * Copy constructor.
     * @param prot Prototype.
     */
    public GroupMember(GroupMember prot) {
        this.id = prot.id;
        this.status = prot.status;
        this.responseCounter = prot.responseCounter;
    }

    public GroupMember replicate() {
        return new GroupMember(this);
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getResponseCounter() {
        return responseCounter;
    }

    public void setResponseCounter(int responseCounter) {
        this.responseCounter = responseCounter;
    }

    public int getNoResponseCounter() {
        return noResponseCounter;
    }

    public void setNoResponseCounter(int noResponseCounter) {
        this.noResponseCounter = noResponseCounter;
    }


    public void incrementResponseCounter() {
        this.responseCounter++;
        this.noResponseCounter = 0;
    }

    public void incrementNoResponseCounter() {
        this.noResponseCounter++;
        this.responseCounter = 0;
    }


    /**
     * Specifies the current status of a group member.
     */
    public enum Status {
        Active,
        Inactive
    }
}
