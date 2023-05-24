package com.thefirstlineofcode.sand.client.thing;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;

public class TimeBasedRexStrategy extends AbstractRexStrategy {
	protected long initRexInternal;
	protected long rexTimeout;
	protected RexRunnable rexRunnable;
	
	protected Lock lock;
	
	public TimeBasedRexStrategy(long initRexInternal, long rexTimeout) {
		if (initRexInternal <= 100)
			throw new IllegalArgumentException("Illegal init REX internal. Too small.");
		
		if (rexTimeout <= 1000)
			throw new IllegalArgumentException("Illegal REX timeout. Too small.");
		
		if (rexTimeout <= initRexInternal) {
			throw new IllegalArgumentException("REX timeout must be bigger than init REX internal.");
		}
		
		this.initRexInternal = initRexInternal;
		this.rexTimeout = rexTimeout;
		
		lock = new ReentrantLock();
	}

	@Override
	public void waitAck(IChatServices chatServices, Iq iq) {
		if (rexRunnable != null)
			rexRunnable.stop();
		
		rexRunnable = new RexRunnable(chatServices, iq, initRexInternal, rexTimeout);
		new Thread(rexRunnable).start();
	}
	
	class RexRunnable implements Runnable, IIqListener {
		private IChatServices chatServices;
		private Iq iq;
		private long rexTimeout;
		
		private long startTime;
		private long lastRexTime;
		private long nextRexInternal;
		
		private boolean stop;
		
		public RexRunnable(IChatServices chatServices, Iq iq, long initRexInternal, long rexTimeout) {
			this.chatServices = chatServices;
			this.iq = iq;
			this.rexTimeout = rexTimeout;
			
			startTime = System.currentTimeMillis();
			nextRexInternal = initRexInternal;
			
			stop = false;
		}

		@Override
		public void run() {
			lastRexTime = startTime;
			
			listenAck(chatServices);
			
			while (true) {				
				if (stop)
					break;
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				long currentTime = System.currentTimeMillis();
				
				if (currentTime - startTime > rexTimeout) {
					try {
						lock.lock();
						
						if (stop)
							break;
						
						noAck();
						stop = true;
						break;
					} finally {
						lock.unlock();
					}
				}
				
				if (currentTime - lastRexTime > nextRexInternal) {
					try {
						lock.lock();
						
						if (stop)
							break;
						
						retransmit();
						continue;
					} finally {
						lock.unlock();
					}
				}
			}
		}
		
		public void stop() {
			chatServices.getIqService().removeListener(this);
			
			stop = true;
		}
		
		public boolean isStopped() {
			return stop;
		}
		
		private void noAck() {
			for (IRexListener rexListener : rexListeners) {
				rexListener.abandon(iq);
			}
			
			for (IAckListener ackListener : ackListeners) {
				ackListener.noAck(iq);
			}
		}
		
		private void retransmit() {
			nextRexInternal = caculateNextRexTime(nextRexInternal);
			
			for (IRexListener rexListener : rexListeners) {
				rexListener.retransmit(iq);
			}
			
			lastRexTime = System.currentTimeMillis();
		}
		
		private long caculateNextRexTime(long nextRexTime) {
			return nextRexTime * 2;
		}
		
		@Override
		public void received(Iq iq) {
			if (iq.getType() != Iq.Type.RESULT)
				return;
			
			if (!iq.getId().equals(RexRunnable.this.iq.getId())) {
				return;
			}
								
			try {
				lock.lock();
				if (rexRunnable.isStopped())
					return;
				
				acked();
				rexRunnable.stop();
			} finally {
				lock.unlock();
			}
		}
		
		private void listenAck(IChatServices chatServices) {
			chatServices.getIqService().addListener(this);
		}
			

		
		protected void acked() {
			for (IRexListener rexListener : rexListeners) {
				rexListener.acked(iq);
			}
			
			for (IAckListener ackListener : ackListeners) {
				ackListener.acked(iq);
			}
			
			rexRunnable.stop();
		}
	}
}