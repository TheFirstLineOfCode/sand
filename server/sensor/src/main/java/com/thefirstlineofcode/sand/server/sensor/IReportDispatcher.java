package com.thefirstlineofcode.sand.server.sensor;


public interface IReportDispatcher {
	public static final String NAME_APP_COMPONENT_REPORT_DISPATCHER = "report.dispatcher";
	
	<T> void addDataListener(Class<T> dataType, IDataListener<T> dataListener);
	<T> boolean removeDataListener(Class<T> dataType, IDataListener<T> dataListener);
}
