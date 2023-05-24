package com.thefirstlineofcode.sand.server.lite.concentrator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAllowed;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactory;
import com.thefirstlineofcode.granite.framework.core.adf.data.IDataObjectFactoryAware;
import com.thefirstlineofcode.sand.server.concentrator.Concentration;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.NodeAdded;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmation;
import com.thefirstlineofcode.sand.server.things.IThingManager;
import com.thefirstlineofcode.sand.server.things.Thing;

@Component
@Transactional
@Scope("prototype")
public class Concentrator implements IConcentrator, IDataObjectFactoryAware {
	private String thingName;
	private SqlSession sqlSession;
	
	@Autowired
	private IThingManager thingManager;
	
	private IDataObjectFactory dataObjectFactory;
	
	public Concentrator(String thingName, SqlSession sqlSession) {
		this.thingName = thingName;
		this.sqlSession = sqlSession;
	}
	
	@Override
	public Node getNodeByLanId(int lanId) {
		return getConcentrationMapper().selectNodeByConcentratorAndLanId(thingName, lanId);
	}
	
	@Override
	public Node getNodeByThingId(String nodeThingId) {
		return getConcentrationMapper().selectNodeByConcentratorAndNode(thingName, nodeThingId);
	}

	@Override
	public Node[] getNodes() {
		List<Node> nodes = getConcentrationMapper().selectNodesByConcentrator(thingName);
		return nodes.toArray(new Node[nodes.size()]);
	}

	@Override
	public NodeAdded confirm(String nodeThingId) {
		if (!thingManager.isValid(nodeThingId))
			throw new RuntimeException(String.format("Invalid node thing ID '%s'.", nodeThingId));
		
		if (containsNode(nodeThingId))
			throw new ProtocolException(new Conflict(String.format("Reduplicate node which's ID is '%s'.", nodeThingId)));
		
		D_NodeConfirmation confirmation = getNodeConfirmation(thingName, nodeThingId);
		if (confirmation == null) {
			throw new ProtocolException(new UnexpectedRequest("No node confirmation found."));
		}
		
		String model = thingManager.getModel(nodeThingId);
		if (model == null) {
			throw new ProtocolException(new BadRequest(String.format("Can't get model of thing which's thing ID is '%s'.", nodeThingId)));
		}
		
		int lanId = confirmation.getNode().getLanId();
		if (containsLanId(lanId))
			throw new ProtocolException(new Conflict(String.format("Reduplicate LAN ID('%'). The node's ID is '%s'.",
					confirmation.getNode().getLanId(), nodeThingId)));
		
		Date confirmedTime = Calendar.getInstance().getTime();
		getNodeConfirmationMapper().updateConfirmed(confirmation.getId(), confirmedTime);
		
		Thing node = dataObjectFactory.create(Thing.class);
		node.setThingId(nodeThingId);
		node.setModel(model);
		node.setRegistrationTime(Calendar.getInstance().getTime());
		thingManager.create(node);
		
		D_Concentration concentration = dataObjectFactory.create(Concentration.class);
		concentration.setId(UUID.randomUUID().toString());
		concentration.setConcentratorThingName(confirmation.getConcentratorThingName());
		concentration.setNodeThingId(confirmation.getNode().getThingId());
		concentration.setLanId(confirmation.getNode().getLanId());
		concentration.setCommunicationNet(confirmation.getNode().getCommunicationNet());
		concentration.setAddress(confirmation.getNode().getAddress());
		Date addedTime = Calendar.getInstance().getTime();
		concentration.setAdditionTime(addedTime);
		getConcentrationMapper().insert(concentration);
		
		return createNodeAdded(confirmation, node, concentration);
	}

	private NodeAdded createNodeAdded(NodeConfirmation confirmation, Thing node,
			D_Concentration concentration) {
		return new NodeAdded(confirmation.getRequestId(), confirmation.getConcentratorThingName(),
				node.getThingId(), confirmation.getNode().getLanId(), node.getModel(),
				concentration.getAdditionTime(), confirmation.getConfirmedTime());
	}
	
	private D_NodeConfirmation getNodeConfirmation(String concentrator, String node) {
		NodeConfirmation[] confirmations = getNodeConfirmationMapper().selectByConcentratorAndNode(concentrator, node);
		if (confirmations == null || confirmations.length == 0)
			return null;
		
		Date currentTime = Calendar.getInstance().getTime();
		for (NodeConfirmation confirmation : confirmations) {
			if (confirmation.getExpiredTime().after(currentTime) &&
					!confirmation.isCanceled() &&
					confirmation.getConfirmedTime() == null) {
				return (D_NodeConfirmation)confirmation;
			}
		}
		
		return null;
	}

	@Override
	public boolean containsNode(String nodeThingId) {
		return getConcentrationMapper().selectCountByConcentratorAndNode(thingName, nodeThingId) != 0;
	}

	@Override
	public void requestToConfirm(NodeConfirmation confirmation) {
		if (!thingName.equals(confirmation.getConcentratorThingName())) {
			throw new ProtocolException(new NotAllowed("Wrong thing ID of concentrator. Your program maybe has a bug."));
		}
		
		if (containsNode(confirmation.getNode().getThingId())) {
			throw new ProtocolException(new Conflict(String.format("Reduplicate node which's ID is '%s'.", confirmation.getNode().getThingId())));
		}
		
		if (containsLanId(confirmation.getNode().getLanId()))
			throw new ProtocolException(new Conflict(String.format("Reduplicate land ID '%s'.", confirmation.getNode().getLanId())));
		
		getNodeConfirmationMapper().insert(confirmation);
	}
	
	@Override
	public void cancelConfirmation(String nodeThingId) {
		// TODO Auto-generated method stub
		
	}
	
	private NodeConfirmationMapper getNodeConfirmationMapper() {
		return sqlSession.getMapper(NodeConfirmationMapper.class);
	}
	
	private ConcentrationMapper getConcentrationMapper() {
		return sqlSession.getMapper(ConcentrationMapper.class);
	}

	@Override
	public void setDataObjectFactory(IDataObjectFactory dataObjectFactory) {
		this.dataObjectFactory = dataObjectFactory;
	}

	@Override
	public boolean containsLanId(int lanId) {
		return getConcentrationMapper().selectCountByConcentratorAndLanId(thingName, lanId) != 0;
	}

	@Override
	public void removeNode(int lanId) {
		Node node = getNodeByLanId(lanId);
		
		getConcentrationMapper().deleteNode(thingName, lanId);
		
		thingManager.remove(node.getThingId());
	}
	
}
