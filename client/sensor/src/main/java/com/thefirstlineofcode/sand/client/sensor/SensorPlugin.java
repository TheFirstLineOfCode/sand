package com.thefirstlineofcode.sand.client.sensor;

import java.util.Properties;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.chalk.core.IChatSystem;
import com.thefirstlineofcode.chalk.core.IPlugin;
import com.thefirstlineofcode.sand.client.thing.ThingPlugin;
import com.thefirstlineofcode.sand.protocols.sensor.LanReport;
import com.thefirstlineofcode.sand.protocols.sensor.Report;
import com.thefirstlineofcode.sand.protocols.sensor.oxm.LanReportParserFactory;
import com.thefirstlineofcode.sand.protocols.sensor.oxm.ReportParserFactory;
import com.thefirstlineofcode.sand.protocols.sensor.oxm.ReportTranslatorFactory;

public class SensorPlugin implements IPlugin {

	@Override
	public void init(IChatSystem chatSystem, Properties properties) {
		chatSystem.register(ThingPlugin.class);
		
		chatSystem.registerParser(new IqProtocolChain(LanReport.PROTOCOL),
				new LanReportParserFactory());
		chatSystem.registerParser(new IqProtocolChain(Report.PROTOCOL),
				new ReportParserFactory());
		chatSystem.registerTranslator(Report.class,
				new ReportTranslatorFactory());
		chatSystem.registerApi(IReportService.class, ReportService.class);
	}

	@Override
	public void destroy(IChatSystem chatSystem) {
		chatSystem.unregisterApi(IReportService.class);
		chatSystem.unregisterTranslator(Report.class);
		chatSystem.unregisterParser(new IqProtocolChain(Report.PROTOCOL));
		chatSystem.unregisterParser(new IqProtocolChain(LanReport.PROTOCOL));
		
		chatSystem.unregister(ThingPlugin.class);
	}
}
