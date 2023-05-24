package com.thefirstlineofcode.sand.server.ibtr;

import java.util.Calendar;
import java.util.Date;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stream.error.Conflict;
import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@AppComponent("thing.authorization.delegator")
public class ThingAuthorizationDelegator implements IConfigurationAware {
	private static final String CONFIGURATION_KEY_THING_AUTHORIZATION_VALIDITY_TIME = "thing.authorization.validity.time";	
	private static final int DEFAULT_THING_AUTHORIZATION_VALIDITY_TIME = 60 * 5;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IAccountManager accountManager;
	
	private int thingAuthorizationValidityTime;
	
	public void authorize(String thingId, String authorizer) {
		if (thingId == null)
			throw new IllegalArgumentException("Null thing ID.");
		
		if (!thingManager.isValid(thingId)) {
			throw new ProtocolException(new BadRequest(String.format(
					"Can't authorize the thing. '%s' isn't a valid thing ID.", thingId)));
		}
		
		if (thingManager.thingIdExists(thingId))
			throw new ProtocolException(new Conflict());
		
		if (authorizer == null)
			throw new IllegalArgumentException("Null authorizer.");
		
		if (!accountManager.exists(authorizer)) {
			throw new ProtocolException(new BadRequest(String.format(
					"Can't authorize the thing. '%s' isn't a valid authorizer.", authorizer)));
		}
		
		thingManager.authorize(thingId, authorizer, getExpiredTime(Calendar.getInstance().getTime().getTime(),
				thingAuthorizationValidityTime));
	}
	
	private Date getExpiredTime(long currentTime, int validityTime) {
		Calendar expiredTime = Calendar.getInstance();
		expiredTime.setTimeInMillis(currentTime + validityTime * 1000);
		
		return expiredTime.getTime();
	}
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		thingAuthorizationValidityTime = configuration.getInteger(CONFIGURATION_KEY_THING_AUTHORIZATION_VALIDITY_TIME,
				DEFAULT_THING_AUTHORIZATION_VALIDITY_TIME);
	}
}
