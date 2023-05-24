package com.thefirstlineofcode.sand.server.sensor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.thefirstlineofcode.granite.framework.core.annotations.AppComponent;

@AppComponent(IReportDispatcher.NAME_APP_COMPONENT_REPORT_DISPATCHER)
public class ReportDispatcher implements IReportDispatcher {
	private Map<Class<?>, List<IDataListener<?>>> dataToListeners;
	
	private IReportDispatcher real;
	
	public ReportDispatcher() {
		dataToListeners = new HashMap<>();
	}
	
	public synchronized void setReal(IReportDispatcher real) {
		this.real = real;
		
		for (Entry<Class<?>, List<IDataListener<?>>> entry : dataToListeners.entrySet()) {
			for (IDataListener<?> dataListener : entry.getValue()) {
				addDataListenerToReal(real, entry, dataListener);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void addDataListenerToReal(IReportDispatcher real, Entry<Class<?>, List<IDataListener<?>>> entry,
				IDataListener<?> dataListener) {
		real.addDataListener((Class<T>)entry.getKey(), (IDataListener<T>)dataListener);
	}

	@Override
	public synchronized <T> void addDataListener(Class<T> dataType, IDataListener<T> dataListener) {
		if (real == null) {
			List<IDataListener<?>> dataListeners = dataToListeners.get(dataType);
			if (dataListeners == null) {
				dataListeners = new ArrayList<>();
				dataToListeners.put(dataType, dataListeners);
			}
			
			if (!dataListeners.contains(dataListener))
				dataListeners.add(dataListener);
		} else {			
			real.addDataListener(dataType, dataListener);
		}
	}

	@Override
	public synchronized <T> boolean removeDataListener(Class<T> dataType, IDataListener<T> dataListener) {
		if (real == null) {
			List<IDataListener<?>> dataListeners = dataToListeners.get(dataType);
			if (dataListeners == null)
				return false;
			
			boolean removed = dataListeners.remove(dataListener);
			if (dataListeners.isEmpty())
				dataToListeners.remove(dataType);
			
			return removed;
		} else {			
			return real.removeDataListener(dataType, dataListener);
		}
	}
}
