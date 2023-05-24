package com.thefirstlineofcode.sand.client.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thefirstlinelinecode.sand.protocols.concentrator.NodeAdded;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Conflict;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Forbidden;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.InternalServerError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.sand.protocols.operator.ApproveFollow;
import com.thefirstlineofcode.sand.protocols.operator.AuthorizeThing;
import com.thefirstlineofcode.sand.protocols.operator.ConfirmConcentration;

public class Operator implements IOperator {
	private IChatServices chatServices;
	private List<IOperator.Listener> listeners;
	
	public Operator(IChatServices chatServices) {
		this.chatServices = chatServices;
		listeners = new ArrayList<IOperator.Listener>();
	}

	@Override
	public void authorize(final String thingId) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				stream.send(new Iq(Iq.Type.SET, new AuthorizeThing(thingId)));
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
				if (iq.getType() != Iq.Type.RESULT) {
					throw new RuntimeException("Attribute type must be 'result'.");
				}
				
				if (iq.getObject() != null) {
					throw new RuntimeException("Protocol object should be null.");
				}
				
				for (IOperator.Listener listener : listeners) {
					listener.authorized(thingId);
				}
			}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				AuthorizationError authError = getAuthError(error);
				for (IOperator.Listener listener : listeners) {
					listener.occurred(authError, thingId);
				}
				
				return true;
			}

			private AuthorizationError getAuthError(StanzaError error) {
				if (error.getDefinedCondition().equals(NotAcceptable.DEFINED_CONDITION)) {
					return new AuthorizationError(AuthorizationErrorReason.INVALID_THING_ID, error);
				} else if (error.getDefinedCondition().equals(Conflict.DEFINED_CONDITION)) {
					return new AuthorizationError(AuthorizationErrorReason.THING_HAS_REGISTERED, error);
				} else if (error.getDefinedCondition().equals(InternalServerError.DEFINED_CONDITION)) {
					return new AuthorizationError(AuthorizationErrorReason.INTERNAL_SERVER_ERROR, error);
				} else {
					return new AuthorizationError(AuthorizationErrorReason.UNKNOWN, error);
				}
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
				for (IOperator.Listener listener : listeners) {
					listener.occurred(new AuthorizationError(AuthorizationErrorReason.TIMEOUT, null), thingId);
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
		});
	}

	@Override
	public void cancelAuthorization(String thingId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void confirm(final String concentratorThingName, final String nodeThingId) {
		chatServices.getTaskService().execute(new ITask<Iq>() {

			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				stream.send(new Iq(Iq.Type.SET, new ConfirmConcentration(
						concentratorThingName, nodeThingId)));
			}

			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
				if (iq.getType() != Iq.Type.RESULT) {
					throw new RuntimeException("Attribute type must be 'result'.");
				}
				
				if (iq.getObject() == null) {
					throw new RuntimeException("Null node addition object.");
				}
				
				NodeAdded nodeAdded = iq.getObject();
				
				for (IOperator.Listener listener : listeners) {
					listener.confirmed(nodeAdded.getConcentratorThingName(),
							nodeAdded.getNodeThingId(), nodeAdded.getLanId());
				}
			}

			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				ConfirmationError confirmError = getConfirmationError(error);
				for (IOperator.Listener listener : listeners) {
					listener.occurred(confirmError, concentratorThingName, nodeThingId);
				}
				
				return true;
			}

			private ConfirmationError getConfirmationError(StanzaError error) {
				if (error.getDefinedCondition().equals(ItemNotFound.DEFINED_CONDITION)) {
					return new ConfirmationError(ConfirmationErrorReason.INVALID_CONCENTRATOR_THING_NAME, error);
				} else if (error.getDefinedCondition().equals(NotAcceptable.DEFINED_CONDITION)) {
					return new ConfirmationError(ConfirmationErrorReason.NOT_A_CONCENTRATOR, error);
				} else if (error.getDefinedCondition().equals(Conflict.DEFINED_CONDITION)) {
					return new ConfirmationError(ConfirmationErrorReason.REDEPLICATED_NODE_OR_LAN_ID, error);
				} else if (error.getDefinedCondition().equals(UnexpectedRequest.DEFINED_CONDITION)) {
					return new ConfirmationError(ConfirmationErrorReason.NO_NODE_CONFIRMATION_FOUND, error);
				} else if (error.getDefinedCondition().equals(BadRequest.DEFINED_CONDITION)) {
					return new ConfirmationError(ConfirmationErrorReason.MODEL_NOT_BE_DETERMINED, error);
				} else if (error.getDefinedCondition().equals(InternalServerError.DEFINED_CONDITION)) {
					return new ConfirmationError(ConfirmationErrorReason.INTERNAL_SERVER_ERROR, error);
				} else {
					return new ConfirmationError(ConfirmationErrorReason.UNKNOWN, error);
				}
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
				for (IOperator.Listener listener : listeners) {
					listener.occurred(new ConfirmationError(ConfirmationErrorReason.TIMEOUT, null),
							concentratorThingName, nodeThingId);
				}
				
				return true;
			}

			@Override
			public void interrupted() {}
		});
	}

	@Override
	public void cancelConfirmation(String concentratorThingName, String nodeThingId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public boolean removeListener(Listener listener) {
		return listeners.remove(listener);
	}

	@Override
	public List<Listener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}

	@Override
	public void approve(final JabberId friend, final Protocol event, final JabberId follower) {
		chatServices.getTaskService().execute(new ITask<Iq>() {
			@Override
			public void trigger(IUnidirectionalStream<Iq> stream) {
				stream.send(new Iq(Iq.Type.SET, new ApproveFollow(friend, event, follower)));
			}
			
			@Override
			public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
				if (iq.getType() != Iq.Type.RESULT) {
					throw new RuntimeException("Attribute type must be 'result'.");
				}
				
				if (iq.getObject() != null) {
					throw new RuntimeException("Protocol object should be null.");
				}
				
				for (IOperator.Listener listener : listeners) {
					listener.approved(friend, event, follower);
				}
			}
			
			@Override
			public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
				ApprovalError eventSubscriptionError = getEventSubscriptionError(error);
				for (IOperator.Listener listener : listeners) {
					listener.occurred(eventSubscriptionError, friend, event, follower);
				}
				
				return true;
			}
			
			private ApprovalError getEventSubscriptionError(StanzaError error) {
				if (error.getDefinedCondition().equals(Forbidden.DEFINED_CONDITION)) {
					return new ApprovalError(ApprovalErrorReason.NOT_AN_ILLEGAL_APPROVER, error);
				} else if (error.getDefinedCondition().equals(ItemNotFound.DEFINED_CONDITION)) {
					return new ApprovalError(ApprovalErrorReason.FRIEND_OR_FOLLOWER_NOT_EXISTED, error);
				} else if (error.getDefinedCondition().equals(Conflict.DEFINED_CONDITION)) {
					return new ApprovalError(ApprovalErrorReason.REDUPLICATE_FOLLOW, error);
				} else if (error.getDefinedCondition().equals(NotAcceptable.DEFINED_CONDITION)) {
					return new ApprovalError(ApprovalErrorReason.EVENT_NOT_SUPPORTED, error);
				} else if (error.getDefinedCondition().equals(InternalServerError.DEFINED_CONDITION)) {
					return new ApprovalError(ApprovalErrorReason.INTERNAL_SERVER_ERROR, error);
				} else {
					return new ApprovalError(ApprovalErrorReason.UNKOWN, error);					
				}
			}

			@Override
			public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
				for (IOperator.Listener listener : listeners) {
					listener.occurred(new ApprovalError(ApprovalErrorReason.TIMEOUT, null),
							friend, event, follower);
				}
				
				return true;
			}
			
			@Override
			public void interrupted() {}
		});
	}
}
