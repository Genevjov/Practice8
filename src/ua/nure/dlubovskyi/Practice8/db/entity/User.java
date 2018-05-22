package ua.nure.dlubovskyi.Practice8.db.entity;

public class User {
	private String login;
	private int id;

	public void setId(int id) {
		this.id = id;
	}

	public User() {
	}

	public User(String name) {
		this.login = name;
	}

	public static User createUser(String name) {
		return new User(name);
	}

	public void setLogin(String string) {
		login = string;
	}

	public String getLogin() {
		return login;
	}

	@Override
	public String toString() {
		return login + " ";
	}

	public int getId() {
		return id;
	}

}
