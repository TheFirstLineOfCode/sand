package com.thefirstlineofcode.sand.demo.server;

import org.pf4j.Extension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thefirstlineofcode.granite.framework.adf.core.ISpringConfiguration;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;
import com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter.CelsiusDegree;
import com.thefirstlineofcode.sand.server.notification.INotificationDispatcher;
import com.thefirstlineofcode.sand.server.sensor.IReportDispatcher;

@Extension
@Configuration
public class SandDemoServerConfiguration implements ISpringConfiguration, IApplicationComponentServiceAware {
	@Dependency(INotificationDispatcher.NAME_APP_COMPONENT_NOTIFICATION_DISPATCHER)
	private INotificationDispatcher notificationDispatcher;
	
	@Dependency(IReportDispatcher.NAME_APP_COMPONENT_REPORT_DISPATCHER)
	private IReportDispatcher reportDispatcher;
	
	private CelsiusDegreeListener celsiusDegreeListener = new CelsiusDegreeListener();
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		notificationDispatcher.addEventListener(VideoRecorded.class, appComponentService.inject(new VideoRecoredListener()));		
		reportDispatcher.addDataListener(CelsiusDegree.class, appComponentService.inject(celsiusDegreeListener));
	}
	
	@Bean
	CelsiusDegreeListener celsiusDegreeListener() {
		return celsiusDegreeListener;
	}
}
