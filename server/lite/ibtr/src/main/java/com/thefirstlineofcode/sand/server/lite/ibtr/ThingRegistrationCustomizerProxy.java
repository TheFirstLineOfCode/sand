package com.thefirstlineofcode.sand.server.lite.ibtr;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.thefirstlineofcode.granite.framework.core.repository.IInitializable;
import com.thefirstlineofcode.sand.server.ibtr.IThingRegistrationCustomizer;
import com.thefirstlineofcode.sand.server.ibtr.IThingRegistrationCustomizerProxy;
import com.thefirstlineofcode.sand.server.things.ThingRegistered;

@Component
@Transactional
public class ThingRegistrationCustomizerProxy implements IThingRegistrationCustomizerProxy,
			IApplicationComponentServiceAware, IInitializable {
	private static final Logger logger = LoggerFactory.getLogger(ThingRegistrationCustomizerProxy.class);
	
	private IApplicationComponentService appComponentService;
	private IThingRegistrationCustomizer real;
	
	@Override
	public void registered(ThingRegistered registered) {
		if (real == null)
			return;
		
		real.registered(registered);
	}
	
	@Override
	public void tryToRegisterWithoutAuthorization(String thingId) {
		if (real == null)
			return;
		
		real.tryToRegisterWithoutAuthorization(thingId);
	}
	
	@Override
	public boolean isBinded() {
		return real != null;
	}

	@Override
	public void init() {
		List<Class<? extends IThingRegistrationCustomizer>> registrationCustomizerClasses =
				appComponentService.getExtensionClasses(IThingRegistrationCustomizer.class);
		if (registrationCustomizerClasses == null || registrationCustomizerClasses.size() == 0) {
			logger.info("No registration customizer found.");
			return;
		}
		
		if (registrationCustomizerClasses.size() != 1) {
			logger.warn("Multiple thing registration customizer found. Ignore them all.");
			return;
		}
		
		real = appComponentService.createExtension(registrationCustomizerClasses.get(0));
		
		if (logger.isInfoEnabled()) {
			logger.info("Found a thing registration customizer which's type is {}.", real.getClass().getName());
		}
	}
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		this.appComponentService = appComponentService;
	}

}
