package ua.nure.dlubovskyi.Practice8.db.entity;

public class Group {
	private int id;
	private String name;

	public Group() {

	}

	public Group(String name) {
		this.name = name;
	}

	public static Group createGroup(String name) {
		return new Group(name);
	}

	public void setName(String string) {
		name = string;
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}
}