package com.thefirstlineofcode.sand.server.lite.concentrator;

import java.util.Date;

import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmation;

public interface NodeConfirmationMapper {
	void insert(NodeConfirmation confirmation);
	void updateCanceled(String thingId, boolean canceled);
	NodeConfirmation[] selectByConcentratorAndNode(String concentratorThingName, String nodeThingId);
	void updateConfirmed(String id, Date confirmedTime);
}
