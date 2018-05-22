package ua.nure.dlubovskyi.Practice8;

import java.sql.SQLException;
import java.util.List;

import ua.nure.dlubovskyi.Practice8.db.DBManager;
import ua.nure.dlubovskyi.Practice8.db.entity.Group;
import ua.nure.dlubovskyi.Practice8.db.entity.User;

public class Demo {

	private static <T> void printList(List<T> list) {
		for (T element : list) {
			System.out.println(element);
		}
	}

	public static void main(String[] args) throws SQLException {
		// users ==> [ivanov]; groups ==> [teamA]

		DBManager dbManager = DBManager.getInstance();

		System.out.println("===========================");

		// Part 1

		dbManager.insertUser(User.createUser("petrov")); // Warning! User.createUser
		// just create new user instance with given login

		dbManager.insertUser(User.createUser("obama"));
		printList(dbManager.findAllUsers()); // users ==> [ivanov, petrov, obama]

		System.out.println("===========================");

		// Part 2

		dbManager.insertGroup(Group.createGroup("teamB")); // Warning!
		// Group.createGroup just create a new group instance with given login

		dbManager.insertGroup(Group.createGroup("teamC"));
		printList(dbManager.findAllGroups()); // groups ==> [teamA, teamB, teamC]

		System.out.println("===========================");

		// Part 3

		User userPetrov = dbManager.getUser("petrov");
		User userIvanov = dbManager.getUser("ivanov");
		User userObama = dbManager.getUser("obama");

		Group teamA = dbManager.getGroup("teamA");
		Group teamB = dbManager.getGroup("teamB");
		Group teamC = dbManager.getGroup("teamC");

		// method setGroupsForUser must implement transaction!
		dbManager.setGroupsForUser(userIvanov, teamA);
		dbManager.setGroupsForUser(userPetrov, teamA, teamB);
		dbManager.setGroupsForUser(userObama, teamA, teamB, teamC);

		for (User user : dbManager.findAllUsers()) {
			printList(dbManager.getUserGroups(user));
			System.out.println("~~~~~");
		}
		// teamA // teamA teamB // teamA teamB teamC

		System.out.println("===========================");

		// Part 4

		// on delete cascade!
		dbManager.deleteGroup(teamA);
		System.out.println("===========================");

		// Part 5

		teamC.setName("teamX");
		dbManager.updateGroup(teamC);

		printList(dbManager.findAllGroups());
		// groups ==> [teamB, teamX]
	}
}
