package com.thefirstlineofcode.sand.client.concentrator;

import com.thefirstlineofcode.sand.protocols.sensor.LanReport;

public interface ILanReportPreprocessor {
	LanReport process(LanReport report);
}
