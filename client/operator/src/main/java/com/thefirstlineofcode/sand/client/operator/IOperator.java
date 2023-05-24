package com.thefirstlineofcode.sand.client.operator;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;

public interface IOperator {
	public enum AuthorizationErrorReason {
		INVALID_THING_ID,
		THING_HAS_REGISTERED,
		INTERNAL_SERVER_ERROR,
		TIMEOUT,
		UNKNOWN
	}
	
	public enum ConfirmationErrorReason {
		INVALID_CONCENTRATOR_THING_NAME,
		NOT_A_CONCENTRATOR,
		REDEPLICATED_NODE_OR_LAN_ID,
		NO_NODE_CONFIRMATION_FOUND,
		MODEL_NOT_BE_DETERMINED,
		INTERNAL_SERVER_ERROR,
		TIMEOUT,
		UNKNOWN
	}
	
	public enum ApprovalErrorReason {
		NOT_AN_ILLEGAL_APPROVER,
		FRIEND_OR_FOLLOWER_NOT_EXISTED,
		EVENT_NOT_SUPPORTED,
		REDUPLICATE_FOLLOW,
		INTERNAL_SERVER_ERROR,
		TIMEOUT,
		UNKOWN
	}
	
	void authorize(String thingId);
	void cancelAuthorization(String thingId);
	void confirm(String concentratorThingName, String nodeThingId);
	void cancelConfirmation(String concentratorThingName, String nodeThingId);
	void approve(JabberId friend, Protocol event, JabberId follower);
	void addListener(Listener listener);
	boolean removeListener(Listener listener);
	List<Listener> getListeners();
	
	public interface Listener {
		void authorized(String thingId);
		void confirmed(String concentratorThingName, String nodeThingId, int lanId);
		void approved(JabberId friend, Protocol event, JabberId follower);
		void canceled(String thingId);
		void canceled(String concentratorThingName, String nodeThingId);
		void occurred(AuthorizationError error, String thingId);
		void occurred(ConfirmationError error, String concentratorThingName, String nodeThingId);
		void occurred(ApprovalError error, JabberId target, Protocol event, JabberId subscriber);
	}
	
	public class AuthorizationError {
		private AuthorizationErrorReason reason;
		private StanzaError error;
		
		public AuthorizationError(AuthorizationErrorReason reason, StanzaError error) {
			this.reason = reason;
			this.error = error;
		}

		public AuthorizationErrorReason getReason() {
			return reason;
		}

		public void setReason(AuthorizationErrorReason reason) {
			this.reason = reason;
		}

		public StanzaError getError() {
			return error;
		}

		public void setError(StanzaError error) {
			this.error = error;
		}
		
	}
	
	public class ConfirmationError {
		private ConfirmationErrorReason reason;
		private StanzaError error;
		
		public ConfirmationError(ConfirmationErrorReason reason, StanzaError error) {
			this.reason = reason;
			this.error = error;
		}

		public ConfirmationErrorReason getReason() {
			return reason;
		}

		public void setReason(ConfirmationErrorReason reason) {
			this.reason = reason;
		}

		public StanzaError getError() {
			return error;
		}

		public void setError(StanzaError error) {
			this.error = error;
		}
		
	}
	
	public class ApprovalError {
		private ApprovalErrorReason reason;
		private StanzaError error;
		
		public ApprovalError(ApprovalErrorReason reason, StanzaError error) {
			this.reason = reason;
			this.error = error;
		}

		public ApprovalErrorReason getReason() {
			return reason;
		}

		public void setReason(ApprovalErrorReason reason) {
			this.reason = reason;
		}

		public StanzaError getError() {
			return error;
		}

		public void setError(StanzaError error) {
			this.error = error;
		}
		
	}
}
