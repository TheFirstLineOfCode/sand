package com.thefirstlineofcode.sand.demo.client;

import java.util.Properties;

import com.thefirstlinelinecode.sand.protocols.concentrator.RemoveNode;
import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedThings;
import com.thefirstlineofcode.sand.demo.protocols.DeliverTemperatureToOwner;
import com.thefirstlineofcode.sand.demo.protocols.NodeAddition;
import com.thefirstlineofcode.sand.demo.protocols.NodeConfirmationRequest;
import com.thefirstlineofcode.sand.demo.protocols.NotAuthorizedEdgeThingRegistration;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideos;
import com.thefirstlineofcode.sand.demo.protocols.EdgeThingRegistration;

public class DemoPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.registerParser(new IqProtocolChain(NotAuthorizedEdgeThingRegistration.PROTOCOL),
				new CocParserFactory<>(NotAuthorizedEdgeThingRegistration.class));
		chatSystem.registerParser(new IqProtocolChain(EdgeThingRegistration.PROTOCOL),
				new CocParserFactory<>(EdgeThingRegistration.class));
		chatSystem.registerParser(new IqProtocolChain(NodeConfirmationRequest.PROTOCOL),
				new CocParserFactory<>(NodeConfirmationRequest.class));
		chatSystem.registerParser(new IqProtocolChain(NodeAddition.PROTOCOL),
				new CocParserFactory<>(NodeAddition.class));
		chatSystem.registerApi(INetConfigService.class, NetConfigService.class);
		
		chatSystem.registerParser(new IqProtocolChain(AccessControlList.PROTOCOL),
				new CocParserFactory<>(AccessControlList.class));
		chatSystem.registerTranslator(AccessControlList.class,
				new CocTranslatorFactory<>(AccessControlList.class));
		chatSystem.registerApi(IAclService.class, AclService.class);
		
		chatSystem.registerParser(new IqProtocolChain(AuthorizedThings.PROTOCOL),
				new CocParserFactory<>(AuthorizedThings.class));
		chatSystem.registerTranslator(AuthorizedThings.class,
				new CocTranslatorFactory<>(AuthorizedThings.class));
		chatSystem.registerApi(IAuthorizedThingsService.class, AuthorizedThingsService.class);
		
		chatSystem.registerTranslator(RemoveNode.class,
				new CocTranslatorFactory<>(RemoveNode.class));
		
		chatSystem.registerParser(new IqProtocolChain(RecordedVideos.PROTOCOL),
				new CocParserFactory<>(RecordedVideos.class));
		chatSystem.registerTranslator(RecordedVideos.class,
				new CocTranslatorFactory<>(RecordedVideos.class));
		chatSystem.registerApi(IRecordedVideosService.class, RecordedVideosService.class);
		
		chatSystem.registerTranslator(DeliverTemperatureToOwner.class, new CocTranslatorFactory<>(DeliverTemperatureToOwner.class));
	}
	
	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(DeliverTemperatureToOwner.class);
		
		chatSystem.unregisterApi(IRecordedVideosService.class);
		chatSystem.unregisterTranslator(RecordedVideos.class);
		chatSystem.unregisterParser(new IqProtocolChain(RecordedVideos.PROTOCOL));
		
		chatSystem.unregisterTranslator(RemoveNode.class);
		
		chatSystem.unregisterApi(IAuthorizedThingsService.class);
		chatSystem.unregisterTranslator(AuthorizedThings.class);
		chatSystem.unregisterParser(new IqProtocolChain(AuthorizedThings.PROTOCOL));
		
		chatSystem.unregisterApi(IAclService.class);
		chatSystem.unregisterTranslator(AccessControlList.class);
		chatSystem.unregisterParser(new IqProtocolChain(AccessControlList.PROTOCOL));
		
		chatSystem.unregisterApi(INetConfigService.class);
		chatSystem.unregisterParser(new IqProtocolChain(NodeAddition.PROTOCOL));
		chatSystem.unregisterParser(new IqProtocolChain(NodeConfirmationRequest.PROTOCOL));
		chatSystem.unregisterParser(new IqProtocolChain(EdgeThingRegistration.PROTOCOL));
		chatSystem.unregisterParser(new IqProtocolChain(NotAuthorizedEdgeThingRegistration.PROTOCOL));
	}

}
