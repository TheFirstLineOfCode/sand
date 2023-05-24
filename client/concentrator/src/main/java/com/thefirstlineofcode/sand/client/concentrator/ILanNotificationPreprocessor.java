package com.thefirstlineofcode.sand.client.concentrator;

import com.thefirstlineofcode.sand.protocols.thing.tacp.LanNotification;

public interface ILanNotificationPreprocessor {
	LanNotification process(LanNotification notification);
}
