package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IServerConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.im.IResource;
import com.thefirstlineofcode.granite.framework.im.IResourcesService;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded.RecordingReason;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;
import com.thefirstlineofcode.sand.server.location.ILocationService;
import com.thefirstlineofcode.sand.server.notification.IEventListener;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class VideoRecoredListener implements IEventListener<VideoRecorded>, IServerConfigurationAware {
	@BeanDependency
	private IRecordedVideoManager recordedVideoManager;
	
	@BeanDependency
	private ILocationService locationService;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IResourcesService resourcesService;
	
	private String domainName;
	
	@Override
	public void eventReceived(IProcessingContext context, JabberId notifier, VideoRecorded videoRecorded) {
		String notifierThingId = locationService.getThingIdByJid(notifier);
		if (notifierThingId == null)
			throw new ProtocolException(new ItemNotFound());
		
		String model = thingManager.getModel(notifierThingId);
		if (!thingManager.isEventSupported(model, VideoRecorded.class)) {
			throw new ProtocolException(new ServiceUnavailable());
		}
		
		if (videoRecorded.getVideoUrl() == null ||
				videoRecorded.getRecordingTime() == null ||
				videoRecorded.getRecordingReason() == null)
			throw new ProtocolException(new BadRequest());
		
		recordedVideoManager.add(notifier, videoRecorded);
		
		if (videoRecorded.getRecordingReason() == RecordingReason.IOT_EVENT)
			notifyTestUsers(context, notifier, videoRecorded);
	}

	private void notifyTestUsers(IProcessingContext context, JabberId notifier, VideoRecorded videoRecorded) {
		for (String user : SandDemoCommandsProcessor.TEST_USERS) {
			JabberId bareJid = new JabberId(user, domainName);
			IResource[] resources = resourcesService.getResources(bareJid);
			
			if (resources == null || resources.length == 0)
				continue;
			
			Iq iq = new Iq(Iq.Type.SET, new Notification(videoRecorded));
			iq.setFrom(notifier);
			for (IResource resource : resources) {
				context.write(resource.getJid(), iq);
			}
		}
	}

	@Override
	public void setServerConfiguration(IServerConfiguration serverConfiguration) {
		domainName = serverConfiguration.getDomainName();
	}

}
