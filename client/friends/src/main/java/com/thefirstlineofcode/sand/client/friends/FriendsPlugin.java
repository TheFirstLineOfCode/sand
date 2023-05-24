package com.thefirstlineofcode.sand.client.friends;

import java.util.Properties;

import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.client.thing.ThingPlugin;

public class FriendsPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(ThingPlugin.class);
		chatSystem.registerApi(IFollowService.class, FollowService.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IFollowService.class);
		chatSystem.unregister(ThingPlugin.class);
	}

}
