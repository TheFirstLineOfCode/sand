package com.thefirstlineofcode.sand.demo.server;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactory;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.auth.Account;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.granite.framework.core.console.AbstractCommandsProcessor;
import com.thefirstlineofcode.granite.framework.core.console.IConsoleSystem;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;
import com.thefirstlineofcode.sand.server.notification.IEventListener;
import com.thefirstlineofcode.sand.server.notification.INotificationDispatcher;

@Extension
public class SandDemoCommandsProcessor extends AbstractCommandsProcessor implements IDataObjectFactoryAware {
	public static final String[] TEST_USERS = {
			"dongger",
			"sand-demo"
	};
	
	private static final String COMMAND_GROUP_SAND_DEMO = "sand-demo";
	private static final String COMMANDS_GROUP_INTRODUCTION = "Commands for sand demo.";
	
	@BeanDependency
	private IAccountManager accountManager;
	private IDataObjectFactory dataObjectFactory;
	@Dependency(INotificationDispatcher.NAME_APP_COMPONENT_NOTIFICATION_DISPATCHER)
	private INotificationDispatcher notificationDispatcher;
	
	private IEventListener<SwitchStateChanged> lightSwtichStateListener;
	
	@Override
	public void printHelp(IConsoleSystem consoleSystem) {
		consoleSystem.printTitleLine(String.format("%s Available commands:", getIntroduction()));
		consoleSystem.printContentLine("sand-demo help - Display the help information for sand demo command group.");
		consoleSystem.printContentLine("sand-demo create-test-users - Create test users into system.");
		consoleSystem.printContentLine("sand-demo listen-light-switch-on - Start to listen light switch state changed event.");
		consoleSystem.printContentLine("sand-demo listen-light-switch-off - Stop to listen light switch state changed event.");
	}
	
	public void processListenLightSwitchOn(IConsoleSystem consoleSystem) {
		if (lightSwtichStateListener == null) {
			lightSwtichStateListener = new LightSwitchStateListener(consoleSystem);
			notificationDispatcher.addEventListener(SwitchStateChanged.class, lightSwtichStateListener);			
		}
		
		consoleSystem.printContentLine("Started to listen.");
	}
	
	public void processListenLightSwitchOff(IConsoleSystem consoleSystem) {
		if (lightSwtichStateListener != null) {
			notificationDispatcher.removeEventListener(SwitchStateChanged.class, lightSwtichStateListener);
			lightSwtichStateListener = null;
		}
		
		consoleSystem.printContentLine("Stopped to listen.");
	}
	
	private class LightSwitchStateListener implements IEventListener<SwitchStateChanged> {
		private IConsoleSystem consoleSystem;
		
		public LightSwitchStateListener(IConsoleSystem consoleSystem) {
			this.consoleSystem = consoleSystem;
		}
		
		@Override
		public void eventReceived(IProcessingContext context, JabberId notifier, SwitchStateChanged event) {
			consoleSystem.printContentLine(String.format(
					"Light which's location is '%s/%s' changed it's switch state to '%s'.",
					notifier.getNode(), notifier.getResource(), event.getCurrent()));
		}
		
	}
	
	public void processCreateTestUsers(IConsoleSystem consoleSystem) {
		boolean allTestUsersHasExisted = true;
		for (String user : TEST_USERS) {
			if (!accountManager.exists(user)) {
				accountManager.add(createAccount(user));
				allTestUsersHasExisted = false;
			}
		}
		
		if (allTestUsersHasExisted) {			
			consoleSystem.printMessageLine("All predefined test users for sand demo has already existed in system. Ignore to execute the command.");
		} else {			
			consoleSystem.printMessageLine("The predefined test users for sand demo has created.");
		}
		
	}
	
	private Account createAccount(String userName) {
		Account account = dataObjectFactory.create(Account.class);
		account.setName(userName);
		account.setPassword(userName);
		
		return account;
	}
	
	@Override
	public String getGroup() {
		return COMMAND_GROUP_SAND_DEMO;
	}
	
	@Override
	public String[] getCommands() {
		return new String[] {
			"help", "create-test-users", "listen-light-switch-on", "listen-light-switch-off"
		};
	}

	@Override
	public String getIntroduction() {
		return COMMANDS_GROUP_INTRODUCTION;
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}
}
