package com.thefirstlineofcode.sand.demo.server.lite;

import org.apache.ibatis.type.EnumTypeHandler;

import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;

public class RoleTypeHandler extends EnumTypeHandler<Role> {
	public RoleTypeHandler() {
		super(Role.class);
	}
}
