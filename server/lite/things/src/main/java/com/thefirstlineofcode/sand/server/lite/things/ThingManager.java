package com.thefirstlineofcode.sand.server.lite.things;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAuthorized;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.thefirstlineofcode.granite.framework.core.repository.IInitializable;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.server.things.IThingManager;
import com.thefirstlineofcode.sand.server.things.IThingModelsProvider;
import com.thefirstlineofcode.sand.server.things.IThingRegistrationCustomizer;
import com.thefirstlineofcode.sand.server.things.Thing;
import com.thefirstlineofcode.sand.server.things.ThingAuthorization;
import com.thefirstlineofcode.sand.server.things.ThingRegistered;

@Transactional
@Component
public class ThingManager implements IThingManager, IInitializable, IApplicationComponentServiceAware {
	private static final Logger logger = LoggerFactory.getLogger(ThingManager.class);
	
	@Autowired
	private SqlSession sqlSession;
	
	private IThingRegistrationCustomizer registrationCustomizer;
	
	private IApplicationComponentService appComponentService;
	
	private Map<String, IThingModelDescriptor> modelDescriptors;
	
	public ThingManager() {
		modelDescriptors = new HashMap<>();
	}
	
	@Override
	public void init() {
		List<IThingModelsProvider> modelsProviders = appComponentService.getPluginManager().getExtensions(IThingModelsProvider.class);
		if (modelsProviders == null || modelsProviders.size() == 0) {
			if (logger.isWarnEnabled())
				logger.warn("No registration ruler definede.");
			
			return;
		}
		
		for (IThingModelsProvider modelsProvider : modelsProviders) {
			registerModels(modelsProvider);
		}
		
		List<Class<? extends IThingRegistrationCustomizer>> registrationCustomizerClasses =
				appComponentService.getPluginManager().getExtensionClasses(IThingRegistrationCustomizer.class);
		
		if (registrationCustomizerClasses == null || registrationCustomizerClasses.size() == 0) {
			logger.warn("No thing registration customizer found. Default registration rule will be used.");
			return;
		}
		
		if (registrationCustomizerClasses.size() != 1) {
			throw new RuntimeException("Error. Multiple thing registration customizers found.");
		}
		
		registrationCustomizer = appComponentService.createExtension(registrationCustomizerClasses.get(0));
		registrationCustomizer.setThingManager(this);
	}
	
	private void registerModels(IThingModelsProvider modelsProvider) {
		List<IThingModelDescriptor> modelDescriptors = modelsProvider.provide();
		for (IThingModelDescriptor modelDescriptor : modelDescriptors) {
			registerModel(modelDescriptor);
		}
	}
	
	@Override
	public void authorize(String thingId, String authorizer, Date expiredTime) {
		if (thingId == null)
			throw new IllegalArgumentException("Null thing ID.");
		
		if (!isValid(thingId))
			throw new RuntimeException(String.format("Invalid thing ID '%s'.", thingId));
		
		if (thingIdExists(thingId))
			throw new IllegalArgumentException(String.format("Thing which's ID is '%s' has already existed.", thingId));
		
		if (authorizer == null)
			throw new IllegalArgumentException("Null authorizer.");
		
		Date authorizedTime = Calendar.getInstance().getTime();
		
		D_ThingAuthorization authrozation = new D_ThingAuthorization();
		authrozation.setId(UUID.randomUUID().toString());
		authrozation.setThingId(thingId);
		authrozation.setAuthorizer(authorizer);
		authrozation.setAuthorizedTime(authorizedTime);
		authrozation.setExpiredTime(expiredTime);
		
		getThingAuthorizationMapper().insert(authrozation);
	}
	
	@Override
	public void cancelAuthorization(String thingId) {
		getThingAuthorizationMapper().updateCanceled(thingId, true);
	}
	
	@Override
	public ThingRegistered register(String thingId, String registrationKey) {
		if (thingId == null)
			throw new ProtocolException(new BadRequest("Null thing ID."));
		
		if (registrationKey == null)
			throw new ProtocolException(new BadRequest("Null registration key."));
		
		if (!isUnregisteredThing(thingId, registrationKey))
			throw new ProtocolException(new NotAcceptable(
					String.format("Not a unregistered thing. Thing ID: %s. Regisration key: %s.",
							thingId, registrationKey)));
		
		String authorizer = null;
		if (registrationCustomizer == null || registrationCustomizer.isAuthenticationRequired()) {			
			ThingAuthorization authorization = getAuthorization(thingId);
			if (authorization == null || authorization.isCanceled() || isExpired(authorization)) {
				throw new ProtocolException(new NotAuthorized());
			}
			
			authorizer = authorization.getAuthorizer();
		}
		
		D_Thing thing = new D_Thing();
		thing.setId(UUID.randomUUID().toString());
		thing.setThingId(thingId);		
		thing.setModel(getModel(thingId));
		thing.setRegistrationTime(Calendar.getInstance().getTime());
		create(thing);
		
		D_RegisteredThing registeredThing = new D_RegisteredThing();
		registeredThing.setId(UUID.randomUUID().toString());
		registeredThing.setThingId(thingId);
		registeredThing.setThingName(getThingName(thingId));
		registeredThing.setCredentials(createCredentials());
		registeredThing.setSecurityKey(createSecurityKey());
		
		getThingIdentityMapper().insert(registeredThing);
		
		return new ThingRegistered(thingId,
				new RegisteredThing(
						registeredThing.getThingName(),
						registeredThing.getCredentials(),
						registeredThing.getSecurityKey()),
						authorizer,
						thing.getRegistrationTime());
	}
	
	private byte[] createSecurityKey() {
		if (registrationCustomizer != null)
			return registrationCustomizer.createSecurityKey();
		
		byte[] securityKey = new byte[16];
		Random random = new Random(System.currentTimeMillis());
		random.nextBytes(securityKey);
		
		return securityKey;
	}

	private boolean isExpired(ThingAuthorization authorization) {
		Date current = Calendar.getInstance().getTime();
		
		return current.after(authorization.getExpiredTime());
	}

	@Override
	public void create(Thing thing) {
		getThingMapper().insert(thing);
	}

	protected String createCredentials() {
		return generateRandomCredentials(8);
	}

	protected String getThingName(String thingId) {
		return thingId;
	}
	
	@Override
	public ThingAuthorization getAuthorization(String thingId) {
		ThingAuthorization[] authroizations = getThingAuthorizationMapper().selectByThingId(thingId);
		if (authroizations == null || authroizations.length == 0)
			return null;
		
		ThingAuthorization authorization = authroizations[0];
		if (isAuthorizationExpired(authorization) || authorization.isCanceled()) {
			return null;
		}
		
		return authorization;
	}

	private boolean isAuthorizationExpired(ThingAuthorization authorization) {
		return Calendar.getInstance().getTime().after(authorization.getExpiredTime());
	}

	@Override
	public boolean isRegistered(String thingId) {
		return getThingMapper().selectByThingId(thingId) != null;
	}

	@Override
	public void remove(String thingId) {
		getThingMapper().delete(thingId);
	}

	@Override
	public boolean thingIdExists(String thingId) {
		return getThingMapper().selectCountByThingId(thingId) != 0;
	}
	
	@Override
	public boolean thingNameExists(String thingName) {
		return getThingMapper().selectCountByThingName(thingName) != 0;
	}
	
	private ThingAuthorizationMapper getThingAuthorizationMapper() {
		return (ThingAuthorizationMapper)sqlSession.getMapper(ThingAuthorizationMapper.class);
	}
	
	private ThingMapper getThingMapper() {
		return (ThingMapper)sqlSession.getMapper(ThingMapper.class);
	}
	
	private RegisteredThingMapper getThingIdentityMapper() {
		return (RegisteredThingMapper)sqlSession.getMapper(RegisteredThingMapper.class);
	}
	
	private String generateRandomCredentials(int length) {
		if (length <= 16) {
			return String.format("%016X", java.util.UUID.randomUUID().getLeastSignificantBits()).substring(16 - length, 16);
		}
		
		if (length > 32) {
			length = 32;
		}
		
		UUID uuid = UUID.randomUUID();
		String uuidHexString = String.format("%016X%016X", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
				
		return uuidHexString.substring(32 - length, 32); 
	}

	@Override
	public void registerModel(IThingModelDescriptor modelDescriptor) {
		if (modelDescriptors.containsKey(modelDescriptor.getModelName()))
			throw new IllegalArgumentException("Reduplicate model name.");
		
		modelDescriptors.put(modelDescriptor.getModelName(), modelDescriptor);
	}

	@Override
	public IThingModelDescriptor unregisterMode(String model) {
		return modelDescriptors.remove(model);
	}

	@Override
	public boolean isConcentrator(String model) {
		return getModelDescriptor(model).isConcentrator();
	}
	
	@Override
	public IThingModelDescriptor getModelDescriptor(String model) {
		IThingModelDescriptor modelDescriptor = modelDescriptors.get(model);
		if (modelDescriptor == null)
			throw new IllegalArgumentException(String.format("Unsupported model: %s.", model));
		
		return modelDescriptor;
	}

	@Override
	public boolean isActuator(String model) {
		return getModelDescriptor(model).isActuator();
	}

	@Override
	public boolean isSensor(String model) {
		return getModelDescriptor(model).isSensor();
	}

	@Override
	public Thing getByThingId(String thingId) {
		return getThingMapper().selectByThingId(thingId);
	}

	@Override
	public Thing getByThingName(String thingName) {
		D_RegisteredThing identity = (D_RegisteredThing)getThingIdentityMapper().selectByThingName(thingName);
		if (identity == null)
			return null;
		
		return getThingMapper().selectByThingId(identity.getThingId());
	}

	@Override
	public boolean isValid(String thingId) {
		if (thingId == null || thingId.length() == 0)
			return false;
		
		for  (IThingModelDescriptor modelDescriptor : modelDescriptors.values()) {
			if (thingId.length() > modelDescriptor.getModelName().length() &&
					thingId.startsWith(modelDescriptor.getModelName() + "-") &&
					thingId.substring(modelDescriptor.getModelName().length(), thingId.length()).length() == 9)
				return true;
		}
		
		return false;
	}

	@Override
	public String getModel(String thingId) {
		if (registrationCustomizer != null)
			return registrationCustomizer.guessModel(thingId);
		
		for (IThingModelDescriptor modelDescriptor : modelDescriptors.values()) {
			if (thingId.startsWith(modelDescriptor.getModelName()))
				return modelDescriptor.getModelName();
		}
		
		return null;
	}

	@Override
	public boolean isActionSupported(String model, Protocol protocol) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		if (!modelDescriptor.isActuator())
			return false;
		
		for (Protocol supportedActionProtocol : modelDescriptor.getSupportedActions().keySet()) {
			if (protocol.equals(supportedActionProtocol))
				return true;
		}
		
		return false;
	}

	@Override
	public boolean isEventSupported(String model, Protocol protocol) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		for (Protocol supportedEventProtocol : modelDescriptor.getSupportedEvents().keySet()) {
			if (protocol.equals(supportedEventProtocol))
				return true;
		}
		
		return false;
	}

	@Override
	public Class<?> getActionType(String model, Protocol protocol) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		if (!modelDescriptor.isActuator())
			throw new RuntimeException(String.format("Thing which's model is '%s' isn't an actuator.", model));
		
		return modelDescriptor.getSupportedActions().get(protocol);
	}
	
	@Override
	public String getThingNameByThingId(String thingId) {
		RegisteredThing registeredThing = getThingIdentityMapper().selectByThingId(thingId);
		if (registeredThing != null)
			return registeredThing.getThingName();
		
		return null;
	}
	
	@Override
	public String getThingIdByThingName(String thingName) {
		return getThingIdentityMapper().selectThingIdByThingName(thingName);
	}

	@Override
	public boolean isActionSupported(String mode, Class<?> actionType) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(mode);
		
		if (!modelDescriptor.isActuator())
			throw new RuntimeException(String.format("Thing which's model is '%s' isn't an actuator.", mode));
		
		return modelDescriptor.getSupportedActions().containsValue(actionType);
	}

	@Override
	public boolean isEventSupported(String model, Class<?> eventType) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		return modelDescriptor.getSupportedEvents().containsValue(eventType);
	}

	@Override
	public String[] getModels() {
		return modelDescriptors.keySet().toArray(new String[0]);
	}

	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

	@Override
	public boolean isEventFollowed(String model, Protocol protocol) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		return modelDescriptor.getFollowedEvents().containsKey(protocol);
	}

	@Override
	public boolean isEventFollowed(String model, Class<?> eventType) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		return modelDescriptor.getFollowedEvents().containsValue(eventType);
	}

	@Override
	public Class<?> getSupportedEventType(String model, Protocol protocol) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		return modelDescriptor.getSupportedEvents().get(protocol);
	}

	@Override
	public Class<?> getFollowedEventType(String model, Protocol protocol) {
		IThingModelDescriptor modelDescriptor = getModelDescriptor(model);
		
		return modelDescriptor.getFollowedEvents().get(protocol);
	}
	
	@Override
	public boolean isUnregisteredThing(String thingId, String registrationCode) {
		if (registrationCustomizer != null) {			
			return registrationCustomizer.isUnregisteredThing(thingId, registrationCode);
		} else {
			if (isRegistered(thingId)) {
				throw new ProtocolException(new Conflict());
			} 
		}	
		
		return isValid(thingId);
	}

	@Override
	public IThingModelDescriptor[] getModelDescriptors() {
		return modelDescriptors.values().toArray(new IThingModelDescriptor[modelDescriptors.size()]);
	}
}
