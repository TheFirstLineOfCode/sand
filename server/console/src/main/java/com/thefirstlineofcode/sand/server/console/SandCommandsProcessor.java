package com.thefirstlineofcode.sand.server.console;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.OperationNotSupportedException;

import org.pf4j.Extension;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlinelinecode.sand.protocols.concentrator.SyncNodes;
import com.thefirstlineofcode.basalt.oxm.coc.PropertyDescriptor;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.ConversionService;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.Converter;
import com.thefirstlineofcode.basalt.oxm.coc.conversion.ConverterFactory;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.granite.framework.core.console.AbstractCommandsProcessor;
import com.thefirstlineofcode.granite.framework.core.console.IConsoleSystem;
import com.thefirstlineofcode.granite.framework.core.console.IConsoleSystemAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirer;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEventFirerAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.lora.dac.Reconfigure;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ResetThing;
import com.thefirstlineofcode.sand.server.actuator.ExecutionEvent;
import com.thefirstlineofcode.sand.server.actuator.IExecutionCallback;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmationDelegator;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmed;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmedEvent;
import com.thefirstlineofcode.sand.server.ibtr.ThingAuthorizationDelegator;
import com.thefirstlineofcode.sand.server.notification.INotificationDispatcher;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@Extension
public class SandCommandsProcessor extends AbstractCommandsProcessor implements IEventFirerAware, IConsoleSystemAware {
	private static final char SEPARATOR_PARAM_NAME_AND_VALUE = '=';
	private static final String SEPARATOR_PARAMS = ",";
	private static final String COMMAND_GROUP_SAND = "sand";
	private static final String COMMANDS_GROUP_INTRODUCTION = "Monitoring and managing sand application.";

	private static final String COMMAND_EXECUTE = "execute";
	private static final String COMMAND_AUTHORIZE = "authorize";
	private static final String COMMAND_THINGS = "things";
	private static final String COMMAND_CONFIRM = "confirm";
	private static final String COMMAND_RESET = "reset";
	private static final String COMMAND_SYNC_NODES = "sync-nodes";
	private static final String COMMAND_HELP = "help";
	private static final String ACTION_NAME_FLASH = "flash";
	private static final String ACTION_NAME_CHANGE_WORKING_MODE = "change-working-mode";
	
	@Dependency("thing.authorization.delegator")
	private ThingAuthorizationDelegator thingAuthorizationDelegator;
	
	@Dependency("node.confirmation.delegator")
	private NodeConfirmationDelegator nodeConfirmationDelegator;
	
	@Dependency("node.confirmed.listener")
	private NodeConfirmedListener nodeConfirmedListener;
	
	@BeanDependency
	private IAccountManager accountManager;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@Dependency(INotificationDispatcher.NAME_APP_COMPONENT_NOTIFICATION_DISPATCHER)
	private INotificationDispatcher notificationDispatcher;
	
	private IEventFirer eventFirer;
	private Map<String, Protocol> actionNameToProtocols;
	
	public SandCommandsProcessor() {
		actionNameToProtocols = createActionNameToProtocols();
	}
	
	private Map<String, Protocol> createActionNameToProtocols() {
		Map<String, Protocol> actionNameToProtocols = new HashMap<>();
		actionNameToProtocols.put(ACTION_NAME_FLASH, new Protocol("urn:leps:things:simple-light", "flash"));
		actionNameToProtocols.put(ACTION_NAME_CHANGE_WORKING_MODE, new Protocol("urn:leps:lora-gateway", "change-working-mode"));
		
		return actionNameToProtocols;
	}
	
	@Override
	public void printHelp(IConsoleSystem consoleSystem) {
		consoleSystem.printTitleLine(String.format("%s Available commands:", getIntroduction()));
		consoleSystem.printContentLine("sand help - Display the help information for sand application management.");
		consoleSystem.printContentLine("sand authorize <THING_ID> <AUTHORIZIER> - Authorize a thing to register.");
		consoleSystem.printContentLine("sand things [START_INDEX] - Display registered things. Twenty items each page.");
		consoleSystem.printContentLine("sand confirm <CONCENTRATOR_THING_NAME> <NODE_THING_ID> - Confirm to add a node to concentrator.");
		consoleSystem.printContentLine("sand execute <THING_LOCATION> <ACTION_NAME> [PARAMS...] - Execute an action on the specified thing.");
		consoleSystem.printContentLine("sand reset <LAN_NODE_LOCATION> - Reset DAC configuration status of a LAN node.");
		consoleSystem.printContentLine("sand sync-nodes <CONCENTRATOR_LOCATION> - Sync LAN nodes of a concentrator with server.");
	}
	
	@Override
	protected boolean isArgumentsMatched(String command, String[] args) {
		if (COMMAND_EXECUTE.equals(command)) {
			return args.length == 2 || args.length == 3;
		}
		
		return true;
	}
	
	private class ThingLocation {
		public String thingName;
		public Integer lanId;
	}
	
	private ThingLocation parseThingLocation(IConsoleSystem consoleSystem, String sThingLocation) {
		int slashIndex = sThingLocation.indexOf('/');
		
		if (slashIndex == sThingLocation.length() - 1) {
			consoleSystem.printMessageLine("Error: Invalid thing location '%s'.");
			return null;
		}
		
		ThingLocation thingLocation = new ThingLocation();
		if (slashIndex == -1) {
			thingLocation.thingName = sThingLocation;
		} else {
			thingLocation.thingName = sThingLocation.substring(0, slashIndex);
			thingLocation.lanId = Integer.parseInt(sThingLocation.substring(slashIndex + 1, sThingLocation.length()));
		}
		
		thingLocation.thingName = thingLocation.thingName.trim();
		
		return thingLocation;
	}
	
	void processExecute(IConsoleSystem consoleSystem, String[] args) {
		ThingLocation thingLocation = parseThingLocation(consoleSystem, args[0]);
		if (thingLocation == null)
			return;
		
		if (!thingManager.getEdgeThingManager().thingNameExists(thingLocation.thingName)) {
			consoleSystem.printMessageLine(String.format("Error: Edge thing which's thing name is '%s' not existed.", thingLocation.thingName));	
			return;
		}
		
		String actionName = args[1];
		Protocol protocol = actionNameToProtocols.get(actionName);
		if (protocol == null) {
			consoleSystem.printMessageLine(String.format("Error: Unsupported action name '%s'.", actionName));	
			return;
		}
		
		Map<String, String> params = null;
		if (args.length == 3) {
			params = getActionParams(args[2]);
			
			if (params == null) {
				consoleSystem.printMessageLine(String.format("Error: Illegal action parameters '%s' for action '%s'.",
						args[2], actionName));
				return;
			}
		}
		
		String thingId = thingManager.getEdgeThingManager().getThingIdByThingName(thingLocation.thingName);
		if (!thingManager.isRegistered(thingId)) {
			consoleSystem.printMessageLine(String.format("Error: Edge thing which's thing ID is '%s' isn't a registered thing.",
					thingLocation.thingName));
			return;
		}
		
		if (concentratorFactory.isLanNode(thingId)) {
			consoleSystem.printMessageLine(String.format(
					"Error: Thing which's thing ID is '%s' is a LAN node. You should access it by it's concentrator.",
					thingLocation.thingName));
			return;
		}
		
		if (!concentratorFactory.isConcentrator(thingId) && thingLocation.lanId != null &&
				IConcentrator.LAN_ID_CONCENTRATOR != thingLocation.lanId) {
			consoleSystem.printMessageLine(String.format("Error: Try to deliver action by thing '%s', but it isn't a concentrator.",
					thingLocation.thingName));
			return;
		}
		
		if (thingLocation.lanId == null || IConcentrator.LAN_ID_CONCENTRATOR == thingLocation.lanId) {
			executeOnEdgeThing(consoleSystem, thingId, protocol, params);
		} else {
			executeOnLanNode(consoleSystem, thingId, thingLocation.lanId, protocol, params);			
		}
	}
	
	private Map<String, String> getActionParams(String sParams) {
		Map<String, String> params = new HashMap<>();
		
		StringTokenizer tokenizer = new StringTokenizer(sParams, SEPARATOR_PARAMS);
		while (tokenizer.hasMoreTokens()) {
			String param = tokenizer.nextToken();
			int equalMarkIndex = param.indexOf(SEPARATOR_PARAM_NAME_AND_VALUE);
			
			if (equalMarkIndex == -1) {
				return null;
			}
			
			if (equalMarkIndex == param.length() - 1) {
				return null;
			}
			
			String paramName = param.substring(0, equalMarkIndex).trim();
			String paramValue = param.substring(equalMarkIndex + 1, param.length()).trim();
			params.put(paramName, paramValue);
		}
		
		return params;
	}

	private boolean isActionSupported(IConsoleSystem consoleSystem, String thingId, String model, Protocol protocol) {
		if (!thingManager.isActionSupported(model, protocol)) {
			consoleSystem.printMessageLine(String.format("Error: Action which's protocol is '%s' isn't supported by thing which's thing ID is '%s'.",
					protocol, thingId));
			return false;
		}
		
		return true;
	}
	
	private void executeOnEdgeThing(IConsoleSystem consoleSystem, String thingId, Protocol protocol,
			Map<String, String> params) {
		String model = thingManager.getModel(thingId);
		if (!isActionSupported(consoleSystem, thingId, model, protocol)) {
			return;
		}
		
		Object actionObject;
		try {
			actionObject = createActionObject(consoleSystem, model, protocol, params);
		} catch (Exception e) {
			throw new RuntimeException("Can't create action object.", e);
		}
		eventFirer.fire(new ExecutionEvent(thingId, null, new Execution(actionObject),
				new ExecutionCallback(thingId, protocol, consoleSystem)));
	}
	
	private void executeOnLanNode(IConsoleSystem consoleSystem, String concentratorThingId, int lanId, Protocol protocol, Map<String, String> params) {
		String nodeThingId = getNodeThingId(concentratorThingId, lanId);
		if (nodeThingId == null) {
			consoleSystem.printMessageLine(String.format("Error: Node not existed. Concentrator's thing ID is '%s'. Lan ID is '%s'.\n",
					concentratorThingId, lanId));
			return;
		}
		
		String model = thingManager.getModel(nodeThingId);
		if (!isActionSupported(consoleSystem, nodeThingId, model, protocol)) {
			return;
		}
		
		Object actionObject = createActionObject(consoleSystem, model, protocol, params);
		
		int lanTimeout = guessLanExecutionTimeout(protocol, actionObject, params);
		
		eventFirer.fire(new ExecutionEvent(concentratorThingId, lanId, createExecution(actionObject, lanTimeout) ,
				new ExecutionCallback(concentratorThingId + "/" + lanId, protocol, consoleSystem)));
	}
	
	protected int guessLanExecutionTimeout(Protocol protocol, Object action, Map<String, String> params) {
		String actionName = getActionName(protocol);
		
		if (ACTION_NAME_FLASH.equals(actionName)) {
			String sRepeat = params.get("repeat");
			int repeat = (sRepeat == null) ? 1 : Integer.parseInt(sRepeat);
				
			return (repeat * 2 + 2) * 1000;
		}
		
		throw new RuntimeException(String.format("Calculate LAN execution timeout for action '%s' not supported.",
				action.getClass().getName()), new OperationNotSupportedException());
	}
	
	private String getActionName(Protocol protocol) {
		for (String actionName : actionNameToProtocols.keySet()) {
			if (actionNameToProtocols.get(actionName).equals(protocol))
				return actionName;
		}
		
		return null;
	}

	private Execution createExecution(Object actionObject, int lanTimeout) {
		Execution execution = new Execution(actionObject, true);
		execution.setLanTimeout(lanTimeout);
		
		return execution;
	}

	private class ExecutionCallback implements IExecutionCallback {
		private String thingLocation;
		private Protocol protocol;
		private IConsoleSystem consoleSystem;
		
		public ExecutionCallback(String thingLocation, Protocol protocol, IConsoleSystem consoleSystem) {
			this.thingLocation = thingLocation;
			this.protocol = protocol;
			this.consoleSystem = consoleSystem;
		}

		@Override
		public boolean processResult(IProcessingContext context, Iq result) {
			consoleSystem.printBlankLine();
			consoleSystem.printBlankLine();
			consoleSystem.printMessageLine(String.format(
					"Action(protocol: %s) executed successfully on the thing which's location is '%s'.",
					protocol, thingLocation));
			consoleSystem.printBlankLine();
			consoleSystem.printPrompt();
			
			return true;
		}

		@Override
		public boolean processError(IProcessingContext context, StanzaError error) {
			consoleSystem.printBlankLine();
			consoleSystem.printBlankLine();
			consoleSystem.printMessageLine(String.format(
					"Failed to execute an action(protocol: %s) on the thing which's location is '%s'. %s",
					protocol, thingLocation, getErrorDescrption(error)));
			consoleSystem.printBlankLine();
			consoleSystem.printPrompt();
			
			return true;
		}

		private String getErrorDescrption(StanzaError error) {
			if (error.getDefinedCondition() != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("Error description: ").
					append("Defined condition = ").
					append(error.getDefinedCondition()).
					append(".");
				
				if (error.getText() != null) {
					sb.append(" Error text = ").
						append(error.getText().getText());
					
					if (sb.charAt(sb.length() - 1) != '.') {						
						sb.append(".");
					}
				}
				
				return sb.toString();
			} else if (error.getApplicationSpecificCondition() != null) {
				return String.format("Error description: Application specific condition = %s.", error.getApplicationSpecificCondition().toString());
			} else {
				return "Unknown error.";
			}
			
		}
		
	}
	
	private String getNodeThingId(String concentratorThingId, int lanId) {
		IConcentrator concentrator = concentratorFactory.getConcentrator(concentratorThingId);
		Node node = concentrator.getNodeByLanId(lanId);
		
		if (node != null) {
			return node.getThingId();
		}
		
		return null;
	}

	private Object createActionObject(IConsoleSystem consoleSystem, String model, Protocol protocol, Map<String, String> params) {		
		Class<?> actionType = thingManager.getActionType(model, protocol);
		try {
			Object action = actionType.getDeclaredConstructor().newInstance();
			if (params != null && !params.isEmpty()) {				
				populateProperties(action, params);
			}
			
			return action;
		} catch (InstantiationException | IllegalAccessException e) {
			consoleSystem.printMessageLine(String.format("Error: Can't initialize action object. Action type is %s.\n", actionType));
		} catch (IllegalArgumentException e) {
			consoleSystem.printMessageLine(String.format("Error: Can't populate action's properties. Detail info is: %s.\n", e.getMessage()));
		} catch(Exception e) {
			consoleSystem.printMessageLine(String.format("Error: Can't create action object. Detail info is: %s.\n", e.getMessage()));			
		}
		
		return null;
	}
	
	private void populateProperties(Object action, Map<String, String> params) {
		for (String paramName : params.keySet()) {
			try {
				PropertyDescriptor pd = new PropertyDescriptor(paramName, action.getClass());
				if (pd.getWriteMethod() != null) {
					Object value = getParam(action, pd.getName(), pd.getPropertyType(), params.get(paramName));
					if (value == null)
						continue;
					
					pd.getWriteMethod().invoke(action, value);
				}
			} catch (Exception e) {
				continue;
			}
		}
	}

	private Object getParam(Object object, String propertyName, Class<?> propertyType, String paramValue) {
		try {
			Field field = object.getClass().getDeclaredField(propertyName);
			Annotation[] annotations = field.getDeclaredAnnotations();
			
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().getAnnotation(ConverterFactory.class) != null ||
						annotation.annotationType().getAnnotation(Converter.class) != null) {
					return ConversionService.from(annotation, paramValue);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Can't fetch field.", e);
		}
		
		if (!isPrimitiveType(propertyType))
			throw new IllegalArgumentException(String.format("Unsupported property type: %s", propertyType.getName()));
		
		return convertStringToPrimitiveType(propertyType, paramValue);
	}
	
	private Object convertStringToPrimitiveType(Class<?> type, String value) {
		if (type.equals(boolean.class) || type.equals(Boolean.class)) {
			return Boolean.valueOf(value);
		} else if (type.equals(int.class) || type.equals(Integer.class)) {
			return Integer.valueOf(value);
		} else if (type.equals(long.class) || type.equals(Long.class)) {
			return Long.valueOf(value);
		} else if (type.equals(float.class) || type.equals(Float.class)) {
			return Float.valueOf(value);
		} else if (type.equals(double.class) || type.equals(Double.class)) {
			return Double.valueOf(value);
		} else if (type.equals(BigInteger.class)) {
			return new BigInteger(value);
		} else if (type.equals(BigDecimal.class)) {
			return new BigDecimal(value);
		} else if (type.isEnum()) {
			Object[] constants = type.getEnumConstants();
			for (Object constant : constants) {
				if (((Enum<?>)constant).toString().equalsIgnoreCase(value)) {
					return constant;
				}
			}
			
			throw new RuntimeException(String.format("Can't convert string '%s' to instance of enum type %s.", type, value));
		} else {
			return value;
		}
	}
	
	private boolean isPrimitiveType(Class<?> fieldType) {
		return fieldType.equals(String.class) ||
				fieldType.equals(boolean.class) ||
				fieldType.equals(Boolean.class) ||
				fieldType.equals(int.class) ||
				fieldType.equals(Integer.class) ||
				fieldType.equals(long.class) ||
				fieldType.equals(Long.class) ||
				fieldType.equals(float.class) ||
				fieldType.equals(Float.class) ||
				fieldType.equals(double.class) ||
				fieldType.equals(Double.class) ||
				fieldType.equals(BigInteger.class) ||
				fieldType.equals(BigDecimal.class) ||
				fieldType.isEnum();
	}
	
	void processAuthorize(IConsoleSystem consoleSystem, String thingId, String authorizer) {
		if (!thingManager.isValid(thingId)) {
			consoleSystem.printMessageLine(String.format("Error: Invalid thing ID '%s'.", thingId));
			return;
		}
		
		if (thingManager.thingIdExists(thingId)) {
			consoleSystem.printMessageLine(String.format("Error: Thing which's ID is '%s' has already registered.", thingId));
			return;
		}
		
		if (!accountManager.exists(authorizer)) {
			consoleSystem.printMessageLine(String.format("Error: '%s' isn't a valid authorizer.", authorizer));
			return;
		}
		
		thingAuthorizationDelegator.authorize(thingId, authorizer);
		if (authorizer != null) {
			consoleSystem.printMessageLine(String.format("Thing which's ID is '%s' has authorized by '%s' in server console.", thingId, authorizer));
		} else {
			consoleSystem.printMessageLine(String.format("Thing which's ID is '%s' has authorized by unknown user in server console.", thingId));
		}
	}
	
	void processConfirm(IConsoleSystem consoleSystem, String concentratorThingId, String nodeThingId) {
		if (!thingManager.thingIdExists(concentratorThingId)) {
			consoleSystem.printMessageLine(String.format("Error: Concentrator which's thing ID is '%s' not existed.", concentratorThingId));	
			return;
		}
		
		if (!concentratorFactory.isConcentrator(concentratorThingId)) {
			consoleSystem.printMessageLine(String.format("Error: Thing which's thing ID is '%s' isn't a concentrator.", concentratorThingId));
			return;
		}
		
		if (!thingManager.isValid(nodeThingId)) {
			consoleSystem.printMessageLine(String.format("Error: Invalid node thing ID '%s'.", nodeThingId));
			return;
		}
		
		nodeConfirmedListener.addConfirmedConcentration(new ConfirmedConcentration(concentratorThingId, nodeThingId));
		
		NodeConfirmed nodeConfirmed = nodeConfirmationDelegator.confirm(concentratorThingId, nodeThingId);
		eventFirer.fire(new NodeConfirmedEvent(nodeConfirmed));
		consoleSystem.printMessageLine(String.format("Concentrator thing which's ID is '%s' has been confirmed to add thing which's ID is '%s' as it's node in server console.",
				nodeConfirmed.getNodeAdded().getConcentratorThingName(), nodeConfirmed.getNodeAdded().getNodeThingId()));
	}
	
	@Override
	public void setEventFirer(IEventFirer evenetFirer) {
		this.eventFirer = evenetFirer;
	}
	
	@Override
	public String getGroup() {
		return COMMAND_GROUP_SAND;
	}

	@Override
	public String[] getCommands() {
		return new String[] {
			COMMAND_AUTHORIZE, COMMAND_THINGS, COMMAND_CONFIRM, COMMAND_EXECUTE, COMMAND_RESET, COMMAND_SYNC_NODES, COMMAND_HELP
		};
	}

	@Override
	public String getIntroduction() {
		return COMMANDS_GROUP_INTRODUCTION;
	}
	
	void processSyncNodes(IConsoleSystem consoleSystem, String sThingLocation) {
		ThingLocation thingLocation = parseThingLocation(consoleSystem, sThingLocation);
		if (thingLocation == null)
			return;
		
		if (thingLocation.lanId != null && IConcentrator.LAN_ID_CONCENTRATOR != thingLocation.lanId) {
			consoleSystem.printMessageLine(String.format("Error: Thing located in '%s' isn't a concentrator.", sThingLocation));
			return;
		}
		
		if (!thingManager.getEdgeThingManager().thingNameExists(thingLocation.thingName)) {
			consoleSystem.printMessageLine(String.format("Error: Edge thing which's name is '%s' not existed.", thingLocation.thingName));
			return;
		}
		
		String thingId = thingManager.getEdgeThingManager().getThingIdByThingName(thingLocation.thingName);
		if (!thingManager.getModelDescriptor(thingManager.getModel(thingId)).isConcentrator())
			consoleSystem.printMessageLine(String.format("Error: Edge thing which's name is '%s' isn't a concentrator.", thingLocation.thingName));
		
		eventFirer.fire(new ExecutionEvent(thingId, null, new Execution(new SyncNodes()) ,
				new ExecutionCallback(thingLocation.thingName, SyncNodes.PROTOCOL, consoleSystem)));
	}
	
	void processReset(IConsoleSystem consoleSystem, String sThingLocation) {
		ThingLocation thingLocation = parseThingLocation(consoleSystem, sThingLocation);
		if (thingLocation == null)
			return;
		
		if (thingLocation.lanId == null || thingLocation.lanId.equals(IConcentrator.LAN_ID_CONCENTRATOR)) {
			consoleSystem.printMessageLine(String.format("Error: Thing located in '%s' isn't a LAN node.", sThingLocation));
			return;
		}
		
		if (!thingManager.getEdgeThingManager().thingNameExists(thingLocation.thingName)) {
			consoleSystem.printMessageLine(String.format("Error: Thing which's name is '%s' not existed.", thingLocation.thingName));
			return;
		}
		
		String concentratorThingId = thingManager.getEdgeThingManager().getThingIdByThingName(thingLocation.thingName);
		if (!concentratorFactory.isConcentrator(concentratorThingId)) {
			consoleSystem.printMessageLine(String.format("Error: Edge thing which's name is '%s' isn't a concentrator.", thingLocation.thingName));
			return;
		}
		
		IConcentrator concentrator = concentratorFactory.getConcentrator(concentratorThingId);
		if (!concentrator.containsLanId(thingLocation.lanId)) {
			consoleSystem.printMessageLine(String.format("Error: Thing located in %s not existed.", sThingLocation));
			return;
		}
		
		eventFirer.fire(new ExecutionEvent(concentratorThingId, thingLocation.lanId,
				createExecution(new ResetThing(), 5000) ,
				new ExecutionCallback(concentratorThingId + "/" + thingLocation.lanId, ResetThing.PROTOCOL, consoleSystem)));
	}

	@Override
	public void setConsoleSystem(IConsoleSystem consoleSystem) {
		notificationDispatcher.addEventListener(Reconfigure.class, new ReconfigureListener(consoleSystem));
	}
	
	private class ReconfigureListener implements com.thefirstlineofcode.sand.server.notification.IEventListener<Reconfigure> {		
		private IConsoleSystem consoleSystem;
		
		public ReconfigureListener(IConsoleSystem consoleSystem) {
			this.consoleSystem = consoleSystem;
		}
		
		@Override
		public void eventReceived(IProcessingContext context, JabberId notifier, Reconfigure event) {
			consoleSystem.printContentLine(String.format("Node which's thing ID is %s try to reconfigure itself.",
					event.getThingId()));
		}
		
	}	
}
