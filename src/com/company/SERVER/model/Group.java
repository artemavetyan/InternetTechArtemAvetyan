package com.company.SERVER.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Group of users
 */
public class Group {

    private List<String> members;

    private List<String> banned;

    private String admin;

    private String groupName;

    public Group(String admin, String groupName) {

        this.members = new ArrayList<>();
        this.banned = new ArrayList<>();
        this.admin = admin;
        this.groupName = groupName;
    }


    public List<String> getMembers() {
        return members;
    }

    public String getAdmin() {
        return admin;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getBanned() {
        return banned;
    }

    public void addMember(String newMember) {
        this.members.add(newMember);
    }

    /**
     * Kicks a member out of a group
     * Add this user to banned list
     *
     * @param member member to kick out
     */
    public void kickMember(String member) {
        this.removeMember(member);
        this.banned.add(member);
    }

    public void removeMember(String userLogin) {
        this.members.remove(userLogin);
    }

    @Override
    public String toString() {
        return
                "Group name='" + groupName + '\'' +
                        ", admin='" + admin + '\'' +
                        "members=" + members +
                        ", banned=" + banned;
    }
}
