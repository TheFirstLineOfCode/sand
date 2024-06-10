package com.thefirstlineofcode.sand.client.lpwanconcentrator;

import com.thefirstlineofcode.sand.protocols.sensor.LanReport;

public interface ILanReportPreprocessor {
	LanReport process(LanReport report);
}
