package ua.nure.dlubovskyi.Practice8.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ua.nure.dlubovskyi.Practice8.db.entity.Group;
import ua.nure.dlubovskyi.Practice8.db.entity.User;

public class DBManager {
	// connection link
	private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/practice8"
			+ "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
	///////////////////////////
	// queries

	private static final String SQL_FIND_ALL_USERS = "SELECT * FROM users";
	private static final String SQL_FIND_ALL_GROUPS = "SELECT * FROM groups";
	private static final String SQL_FIND_GROUP_BY_ID = "SELECT * FROM groups WHERE id = ?";
	private static final String SQL_FIND_USER_BY_LOGIN = "SELECT * FROM users WHERE login=?";
	private static final String SQL_FIND_GROUP_BY_NAME = "SELECT * FROM groups WHERE name=?";

	private static final String SQL_INSERT_USER = "INSERT INTO users VALUES (DEFAULT, ?)";
	private static final String SQL_INSERT_GROUP = "INSERT INTO groups VALUES (DEFAULT, ?)";
	private static final String SQL_FIND_USER_GROUPS = "SELECT * FROM users_groups WHERE user_id =?";
	private static final String SQL_UPDATE_GROUP = "UPDATE groups SET name = ? WHERE id = ?";
	private static final String SQL_DELETE_GROUP = "DELETE FROM groups WHERE id = ?";
	private static final String SQL_INSERT_USER_GROUP = "INSERT INTO users_groups VALUES(?,?)";

	/////////////////
	/// singleton ///
	/////////////////
	private static DBManager instance; // == null

	public static synchronized DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}
		return instance;
	}

	private DBManager() {
	}

	////////////////////////////////
	// logic

	// get all users
	public List<User> findAllUsers() {
		List<User> users = new ArrayList<>();
		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_FIND_ALL_USERS)) {
			while (rs.next()) {
				User user = extractUser(rs);
				users.add(user);
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return users;
	}

	// get user by login
	public User findUserByLogin(String login) {
		try (Connection con = getConnection(); PreparedStatement pstmt = con.prepareStatement(SQL_FIND_USER_BY_LOGIN)) {
			int k = 1;
			pstmt.setString(k++, login);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return extractUser(rs);
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return null;
	}

	/////////////////////////
	// util methods

	// create connection
	public Connection getConnection() throws SQLException {
		Connection con = DriverManager.getConnection(CONNECTION_URL, "root", "root");
		return con;
	}

	// get user from ResultSet
	private User extractUser(ResultSet rs) throws SQLException {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setLogin(rs.getString("login"));
		return user;
	}

	// add new user to db
	public boolean insertUser(User user) {
		try (Connection con = getConnection();
				PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, user.getLogin());
			ResultSet resultSet;
			if (pstmt.executeUpdate() > 0) {
				resultSet = pstmt.getGeneratedKeys();
				if (resultSet.next()) {
					user.setId(resultSet.getInt(1));
					return true;
				}
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return false;
	}

	// add new group to db
	public boolean insertGroup(Group group) {
		try (Connection con = getConnection();
				PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_GROUP, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, group.getName());
			ResultSet resultSet;
			if (pstmt.executeUpdate() > 0) {
				resultSet = pstmt.getGeneratedKeys();
				if (resultSet.next()) {
					group.setId(resultSet.getInt(1));
					return true;
				}
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return false;

	}

	// get all groups
	public List<Group> findAllGroups() {
		List<Group> groups = new ArrayList<>();
		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(SQL_FIND_ALL_GROUPS)) {
			while (rs.next()) {
				groups.add(extractGroup(rs));
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return groups;
	}

	// get group from ResultSet
	private Group extractGroup(ResultSet rs) throws SQLException {
		Group group = new Group();
		group.setId(rs.getInt(1));
		group.setName(rs.getString(2));
		return group;
	}

	// util method for transaction
	private void rollBack(Connection connection) {
		if (Objects.isNull(connection)) {
			throw new NullPointerException();
		} else {
			try {
				connection.rollback();
			} catch (SQLException e) {
				System.err.println(e.getLocalizedMessage());
			}

		}
	}

	// set groups
	public void setGroupsForUser(User userIvanov, Group... groups) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_USER_GROUP);
			preparedStatement.setInt(1, userIvanov.getId());
			for (Group group : groups) {
				preparedStatement.setInt(2, group.getId());
				preparedStatement.executeUpdate();
			}
			connection.commit();
			connection.close();
			preparedStatement.close();
		} catch (SQLException e) {
			rollBack(connection);
		}

	}

	// get group by name
	public Group getGroup(String name) {
		Group group = new Group();
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_GROUP_BY_NAME)) {
			preparedStatement.setString(1, name);
			ResultSet rSet = preparedStatement.executeQuery();
			while (rSet.next()) {
				group.setId(rSet.getInt("id"));
				group.setName(rSet.getString("name"));
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return group;
	}

	// get user by login
	public User getUser(String login) {
		User user = new User();
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_USER_BY_LOGIN)) {
			preparedStatement.setString(1, login);
			ResultSet rSet = preparedStatement.executeQuery();
			while (rSet.next()) {
				user.setId(rSet.getInt("id"));
				user.setLogin(rSet.getString("login"));
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return user;
	}

	// delete group
	public void deleteGroup(Group teamA) throws SQLException {
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_DELETE_GROUP)) {
			preparedStatement.setInt(1, teamA.getId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	// update group name
	public void updateGroup(Group teamC) {
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_UPDATE_GROUP)) {
			preparedStatement.setString(1, teamC.getName());
			preparedStatement.setInt(2, teamC.getId());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	// find group by id
	private Group getGroupById(int id) {
		Group group = new Group();
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_GROUP_BY_ID)) {
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				group.setId(id);
				group.setName(resultSet.getString("name"));
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return group;
	}

	// find user groups
	public List<Group> getUserGroups(User user) {
		List<Group> result = new ArrayList<>();
		try (Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_FIND_USER_GROUPS)) {
			preparedStatement.setInt(1, user.getId());
			ResultSet rSet = preparedStatement.executeQuery();
			while (rSet.next()) {
				result.add(getGroupById(rSet.getInt("group_id")));
			}
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}
		return result;
	}

}
