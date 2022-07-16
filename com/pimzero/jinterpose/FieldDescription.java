package com.pimzero.jinterpose;

public class FieldDescription implements Comparable<FieldDescription> {
	private String owner;
	private String name;

	public FieldDescription(String owner, String name) {
		this.owner = owner;
		this.name = name;
	}

	public int compareTo(FieldDescription o) {
		int r = this.owner.compareTo(o.owner);
		if (r != 0)
			return r;
		else
			return this.name.compareTo(o.name);
	}

	public String getName() {
		return this.name;
	}

	public String getOwner() {
		return this.owner;
	}
}
