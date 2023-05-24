package com.thefirstlineofcode.sand.client.things.simple.camera;

import java.util.Properties;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.client.thing.ThingPlugin;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakePhoto;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakeVideo;

public class SimpleCameraPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(ThingPlugin.class);
		
		chatSystem.registerParser(new IqProtocolChain(Execution.PROTOCOL).next(TakePhoto.PROTOCOL),
				new CocParserFactory<>(TakePhoto.class));
		chatSystem.registerParser(new IqProtocolChain(TakePhoto.PROTOCOL),
				new CocParserFactory<>(TakePhoto.class));
		chatSystem.registerTranslator(TakePhoto.class,
				new CocTranslatorFactory<>(TakePhoto.class));
		
		chatSystem.registerParser(new IqProtocolChain(Execution.PROTOCOL).next(TakeVideo.PROTOCOL),
				new CocParserFactory<>(TakeVideo.class));
		chatSystem.registerParser(new IqProtocolChain(TakeVideo.PROTOCOL),
				new CocParserFactory<>(TakeVideo.class));
		chatSystem.registerTranslator(TakeVideo.class,
				new CocTranslatorFactory<>(TakeVideo.class));
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterTranslator(TakeVideo.class);
		chatSystem.unregisterParser(new IqProtocolChain(Execution.PROTOCOL).next(TakeVideo.PROTOCOL));
				
		chatSystem.unregisterTranslator(TakePhoto.class);
		chatSystem.unregisterParser(new IqProtocolChain(TakePhoto.PROTOCOL));
		chatSystem.unregisterParser(new IqProtocolChain(Execution.PROTOCOL).next(TakePhoto.PROTOCOL));
		
		chatSystem.unregister(ThingPlugin.class);
	}

}
