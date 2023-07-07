package com.thefirstlineofcode.sand.server.lite.concentrator;

import java.util.List;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlineofcode.sand.server.concentrator.Concentration;

public interface ConcentrationMapper {
	void insert(Concentration concentration);
	int selectCountByConcentratorAndLanId(String concentratorThingName, int lanId);
	Node selectNodeByConcentratorAndLanId(String concentratorThingName, int lanId);
	int selectCountByConcentratorAndNode(String concentratorThingName, String nodeThingId);
	Node selectNodeByConcentratorAndNode(String concentratorThingName, String nodeThingId);
	int selectCountByNode(String nodeThingId);
	D_Concentration selectConcentrationByNode(String nodeThingId);
	List<Node> selectNodesByConcentrator(String concentratorThingId);
	void deleteNode(String concentratorThingName, int lanId);
}
