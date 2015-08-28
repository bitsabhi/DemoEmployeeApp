package com.sample.employees;

public class Employee {
	private String mName;
	private String mDepartment;
	private Long mAge;
	private String mDesignation;
	private Long mEmpId;
	
	public Employee(String name, Long age, String department, String designation, Long id) {
		mName = name;
		mAge = age;
		mDepartment = department;
		mDesignation = designation;
		mEmpId = id;
	}
	
	public String getName() {
		return mName;
	}
	public String getDepartment() {
		return mDepartment;
	}
	public String getDesignation() {
		return mDesignation;
	}
	public Long getAge() {
		return mAge;
	}
	public Long getId() {
		return mEmpId;
	}
	
}