package com.thefirstlineofcode.sand.client.sensor;

public interface IReportService {
	void start();
	void stop();
	boolean isStarted();
	<T> void listen(Class<T> dataType, IDataProcessor<T> dataProcessor);
	void stopListening(Class<?> dataType);
	IReporter getReporter();
}
