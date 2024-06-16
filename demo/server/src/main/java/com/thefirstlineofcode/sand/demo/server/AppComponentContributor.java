package com.thefirstlineofcode.sand.demo.server;

import org.pf4j.Extension;

import com.thefirstlineofcode.granite.framework.core.adf.IAppComponentsContributor;

@Extension
public class AppComponentContributor implements IAppComponentsContributor {

	@Override
	public Class<?>[] getAppComponentClasses() {
		return new Class<?>[] {
			NotAuthorizedEdgeThingRegistrations.class
		};
	}

}
